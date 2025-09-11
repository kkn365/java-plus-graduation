package ru.practicum.explorewithme.comments.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.comments.dto.AdminCommentParams;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.mapper.CommentMapper;
import ru.practicum.explorewithme.comments.model.Comment;
import ru.practicum.explorewithme.comments.model.CommentStatus;
import ru.practicum.explorewithme.comments.repository.CommentRepository;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.service.EventService;
import ru.practicum.explorewithme.exception.DataAlreadyExistException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.RequestRepository;
import ru.practicum.explorewithme.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.comments.repository.CommentRepository.AdminCommentSpecification.withAdminCommentParams;
import static ru.practicum.explorewithme.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

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
        User user = userService.getUser(userId);
        Event event = eventService.findEventById(eventId);

        // Проверка, что пользователь участвует в событии
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID {0} не участвует в событии с ID {1}", userId, eventId);
        }

        // Проверка, что пользователь не оставил другой комментарий на это же событие
        if (commentRepository.existsByAuthorIdAndEventId(user.getId(), event.getId())) {
            throw new DataAlreadyExistException("Пользователь с ID {0} уже оставил комментарий к событию с ID {1}",
                    userId, eventId);
        }

        LocalDateTime timestamp = LocalDateTime.now();

        Comment newComment = Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .author(user)
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
        userService.getUser(userId);
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
}