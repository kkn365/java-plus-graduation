package ru.practicum.ewm.comments.service;

import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.ewm.comments.dto.AdminCommentParams;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentStatus;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.ewm.users.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.comments.repository.CommentRepository.AdminCommentSpecification.withAdminCommentParams;
import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден!";

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final EventService eventService;

    private final UserClient userClient;

    /**
     * Получает список опубликованных комментариев к событию.
     *
     * @param eventId идентификатор события
     * @return список DTO-объектов комментариев
     */
    @Override
    public List<CommentDto> findComments(long eventId) {
        eventService.findEventById(eventId);
        return commentMapper.toDto(commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED));
    }

    /**
     * Получает конкретный комментарий по его ID и ID события.
     *
     * @param eventId    идентификатор события
     * @param commentId  идентификатор комментария
     * @return DTO-объект комментария
     */
    @Override
    public CommentDto findComment(long eventId, long commentId) {
        eventService.findEventById(eventId);

        return commentMapper.toDto(
                commentRepository.findByIdAndStatus(commentId, CommentStatus.APPROVED)
                        .orElseThrow(notFoundException("Комментарий с ID {0} не найден", commentId))
        );
    }

    /**
     * Получает комментарий по его ID.
     *
     * @param commentId идентификатор комментария
     * @return DTO-объект комментария
     */
    @Override
    public CommentDto findCommentById(long commentId) {
        return commentMapper.toDto(getCommentById(commentId));
    }

    /**
     * Создаёт новый комментарий от пользователя к событию.
     *
     * @param userId         идентификатор пользователя
     * @param eventId        идентификатор события
     * @param newCommentDto  DTO с текстом комментария
     * @return DTO-объект созданного комментария
     */
    @Override
    public CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto) {
        validateUserExists(userId);
        Event event = eventService.findEventById(eventId);

        // Проверка, что пользователь участвует в событии
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID {0} не участвует в событии с ID {1}", userId, eventId);
        }

        // Проверка, что пользователь не оставил другой комментарий на это же событие
        if (commentRepository.existsByAuthorIdAndEventId(userId, event.getId())) {
            throw new DataAlreadyExistException("Пользователь с ID {0} уже оставил комментарий к событию с ID {1}",
                    userId, eventId);
        }

        LocalDateTime timestamp = LocalDateTime.now();

        Comment newComment = Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .authorId(userId)
                .createdDate(timestamp)
                .updatedDate(timestamp)
                .status(CommentStatus.PENDING)
                .build();

        Comment savedComment = commentRepository.save(newComment);
        log.info("Создан комментарий с ID {} к событию с ID {} от пользователя с ID {}",
                savedComment.getId(), eventId, userId);
        return commentMapper.toDto(savedComment);
    }

    /**
     * Удаляет комментарий по его ID.
     *
     * @param commentId идентификатор комментария
     */
    @Override
    public void deleteComment(long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.deleteById(comment.getId());
        log.info("Комментарий с ID {} удалён", commentId);
    }

    /**
     * Обновляет статус комментария.
     *
     * @param commentId идентификатор комментария
     * @param status    новый статус комментария
     * @return обновлённый DTO-объект комментария
     */
    @Override
    public CommentDto patchCommentStatus(long commentId, CommentStatus status) {
        Comment comment = getCommentById(commentId);

        comment.setStatus(status);
        if (status == CommentStatus.APPROVED) {
            comment.setPublishedDate(LocalDateTime.now());
        }
        comment.setUpdatedDate(LocalDateTime.now());
        log.info("Статус комментария с ID {} обновлён на {}", commentId, status);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    /**
     * Получает список опубликованных комментариев пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO-объектов комментариев
     */
    @Override
    public List<CommentDto> findApprovedCommentsOnUserId(long userId) {
        validateUserExists(userId);
        return commentMapper.toDto(commentRepository.findByAuthorIdAndStatus(userId, CommentStatus.APPROVED));
    }

    /**
     * Получает список комментариев по параметрам фильтрации для администратора.
     *
     * @param params параметры фильтрации
     * @return список DTO-объектов комментариев
     */
    @Override
    public List<CommentDto> findAllByAdminParams(AdminCommentParams params) {
        validateDateRanges(params.getCreatedDateStart(), params.getCreatedDateEnd(), "createdDate");
        validateDateRanges(params.getPublishedDateStart(), params.getPublishedDateEnd(), "publishedDate");

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by("createdDate").descending()
        );

        Page<Comment> commentsPage = commentRepository.findAll(withAdminCommentParams(params), pageRequest);
        return commentMapper.toDto(commentsPage.stream().toList());
    }

    /**
     * Вспомогательный метод для проверки корректности временных диапазонов.
     *
     * @param start дата начала
     * @param end   дата окончания
     * @param type  тип даты (например: createdDate, publishedDate)
     * @throws ValidationException если end <= start
     */
    private void validateDateRanges(LocalDateTime start, LocalDateTime end, String type) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException(type + "End must be after " + type + "Start");
        }
    }

    /**
     * Вспомогательный метод для получения комментария по его ID с обработкой исключения.
     *
     * @param commentId идентификатор комментария
     * @return объект комментария
     */
    private Comment getCommentById(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(notFoundException("Комментарий с ID {0} не найден", commentId));
    }

    /**
     * Проверяет существование пользователя с указанным идентификатором.
     * <p>
     * Выполняет запрос к пользовательскому сервису через Feign-клиент.
     * Если пользователь не найден или произошла ошибка, выбрасывается {@link NotFoundException}.
     *
     * @param userId уникальный идентификатор пользователя
     * @throws NotFoundException если пользователь не найден или произошла ошибка при получении данных
     */
    private void validateUserExists(long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Пользовательский сервис вернул статус {}: {}",
                        response.getStatusCode(),
                        response.getHeaders());
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

            if (response.getBody() == null) {
                log.warn("Ответ от пользовательского сервиса не содержит тело для пользователя ID={}", userId);
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

        } catch (FeignException fe) {
            log.error("Ошибка при проверке существования пользователя ID={}: HTTP {} - {}",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId), fe);
        }
    }
}