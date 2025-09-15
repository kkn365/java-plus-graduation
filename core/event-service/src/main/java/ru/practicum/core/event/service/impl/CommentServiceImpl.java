package ru.practicum.core.event.service.impl;

import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.internal.request.client.RequestClient;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.event.dto.comments.AdminCommentParams;
import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.dto.comments.NewCommentDto;
import ru.practicum.core.event.mapper.CommentMapper;
import ru.practicum.core.event.model.Comment;
import ru.practicum.core.event.model.enums.comments.CommentStatus;
import ru.practicum.core.event.repository.CommentRepository;
import ru.practicum.core.event.model.Event;
import ru.practicum.core.event.service.api.EventService;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.event.service.api.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.event.repository.CommentRepository.AdminCommentSpecification.withAdminCommentParams;
import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

/**
 * Реализация сервиса для работы с комментариями к событиям.
 * <p>
 * Класс предоставляет методы для создания, удаления, получения и изменения статуса комментариев,
 * а также проверки участия пользователей в событиях и валидации входных данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с ID=%d не найден";
    private static final String COMMENT_NOT_FOUND_MESSAGE = "Комментарий с ID=%d не найден";
    private static final String USER_NOT_PARTICIPANT_MESSAGE = "Пользователь с ID=%d не участвует в событии с ID=%d";
    private static final String COMMENT_EXIST_MESSAGE = "Пользователь с ID=%d уже оставил комментарий к событию с ID=%d";
    private static final String COMMENT_STATUS_ALREADY_SET_MESSAGE = "Статус комментария с ID=%d уже установлен на %s";

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final EventService eventService;

    private final UserClient userClient;
    private final RequestClient requestClient;

    /**
     * Метод получения всех одобренных комментариев для конкретного события.
     *
     * @param eventId идентификатор события
     * @return список DTO комментариев
     * @throws NotFoundException если событие не найдено
     */
    @Override
    public List<CommentDto> findComments(long eventId) {
        eventService.findEventById(eventId);
        return commentMapper.toDto(commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED));
    }

    /**
     * Метод получения конкретного комментария по его идентификатору и идентификатору события.
     *
     * @param eventId   идентификатор события
     * @param commentId идентификатор комментария
     * @return DTO комментария
     * @throws NotFoundException если событие или комментарий не найдены
     */
    @Override
    public CommentDto findComment(long eventId, long commentId) {
        return commentMapper.toDto(
                commentRepository.findByIdAndStatus(commentId, CommentStatus.APPROVED)
                        .orElseThrow(notFoundException(COMMENT_NOT_FOUND_MESSAGE, commentId))
        );
    }

    /**
     * Метод получения комментария по его идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return DTO комментария
     * @throws NotFoundException если комментарий не найден
     */
    @Override
    public CommentDto findCommentById(long commentId) {
        return commentMapper.toDto(getCommentById(commentId));
    }

    /**
     * Метод создания нового комментария.
     *
     * @param userId        идентификатор пользователя, оставляющего комментарий
     * @param eventId       идентификатор события, к которому оставляется комментарий
     * @param newCommentDto DTO с текстом комментария
     * @return DTO созданного комментария
     * @throws NotFoundException         если пользователь или событие не найдены
     * @throws DataAlreadyExistException если пользователь уже оставил комментарий на это событие
     */
    @Override
    public CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto) {
        validateUserExists(userId);
        Event event = eventService.findEventById(eventId);

        // Проверка, что пользователь участвует в событии
        boolean isParticipant = checkUserParticipation(userId, eventId);

        if (!isParticipant) {
            throw new NotFoundException(USER_NOT_PARTICIPANT_MESSAGE, userId, eventId);
        }

        // Проверка, что пользователь не оставил другой комментарий на это же событие
        if (commentRepository.existsByAuthorIdAndEventId(userId, event.getId())) {
            throw new DataAlreadyExistException(COMMENT_EXIST_MESSAGE, userId, eventId);
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
        log.info("Создан комментарий с ID={} к событию с ID={} от пользователя с ID={}",
                savedComment.getId(), eventId, userId);
        return commentMapper.toDto(savedComment);
    }

    /**
     * Метод удаления комментария.
     *
     * @param commentId идентификатор комментария
     * @throws NotFoundException если комментарий не найден
     */
    @Override
    public void deleteComment(long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.deleteById(comment.getId());
        log.info("Комментарий с ID={} удалён", commentId);
    }

    /**
     * Метод обновления статуса комментария.
     * <p>
     * Позволяет изменить текущий статус комментария на новый. Если новый статус совпадает с текущим,
     * генерируется исключение {@link DataAlreadyExistException}.
     *
     * @param commentId идентификатор комментария, для которого необходимо обновить статус
     * @param status    новый статус комментария (например: APPROVED, REJECTED, PENDING)
     * @return DTO обновлённого комментария
     * @throws DataAlreadyExistException если комментарий уже имеет указанный статус
     * @throws NotFoundException         если комментарий с указанным ID не найден
     */
    @Override
    public CommentDto patchCommentStatus(long commentId, CommentStatus status) {
        Comment comment = getCommentById(commentId);

        // Проверка, что комментарий уже имеет указанный статус
        if (comment.getStatus().equals(status)) {
            throw new DataAlreadyExistException(COMMENT_STATUS_ALREADY_SET_MESSAGE, commentId, status);
        }

        comment.setStatus(status);
        if (CommentStatus.APPROVED.equals(status)) {
            comment.setPublishedDate(LocalDateTime.now());
        }
        comment.setUpdatedDate(LocalDateTime.now());
        log.info("Статус комментария с ID={} обновлён на {}", commentId, status);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    /**
     * Метод получения всех одобренных комментариев определённого пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO комментариев
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<CommentDto> findApprovedCommentsOnUserId(long userId) {
        validateUserExists(userId);
        return commentMapper.toDto(commentRepository.findByAuthorIdAndStatus(userId, CommentStatus.APPROVED));
    }

    /**
     * Метод получения комментариев с фильтрацией по параметрам администратора.
     *
     * @param params объект с параметрами фильтрации
     * @return список DTO комментариев
     * @throws ValidationException если переданные временные диапазоны некорректны
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
     * Метод проверки корректности временного диапазона: конечная дата должна быть позже начальной.
     *
     * @param start начальная дата
     * @param end   конечная дата
     * @param type  тип даты (например, createdDate, publishedDate)
     * @throws ValidationException если конечная дата раньше начальной
     */
    private void validateDateRanges(LocalDateTime start, LocalDateTime end, String type) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException(type + " должна быть позже начальной " + start);
        }
    }

    /**
     * Метод получения комментария по его идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return сущность комментария
     * @throws NotFoundException если комментарий не найден
     */
    private Comment getCommentById(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(notFoundException(COMMENT_NOT_FOUND_MESSAGE, commentId));
    }

    /**
     * Метод проверки существования пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     * @throws FeignException    при ошибке обращения к пользовательскому сервису
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

    /**
     * Проверяет, участвует ли пользователь в указанном событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true, если пользователь участвует в событии
     */
    private boolean checkUserParticipation(long userId, long eventId) {
        try {
            ResponseEntity<Boolean> response = requestClient.hasRequest(userId, eventId);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return Boolean.TRUE.equals(response.getBody());
            }
            return false;
        } catch (FeignException fe) {
            log.error("Ошибка при проверке участия пользователя ID={} в событии ID={}: HTTP {} - {}",
                    userId, eventId, fe.status(), fe.getMessage(), fe);
            return false;
        }
    }
}