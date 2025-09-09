package ru.practicum.explorewithme.comments;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.service.EventService;
import ru.practicum.explorewithme.exception.DataAlreadyExistException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.model.ParticipationRequest;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.RequestRepository;
import ru.practicum.explorewithme.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.comments.CommentRepository.AdminCommentSpecification.withAdminCommentParams;
import static ru.practicum.explorewithme.exception.NotFoundException.notFoundException;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public List<CommentDto> findComments(long eventId) {

        eventService.findEventById(eventId);
        return commentMapper.toDto(commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED));
    }

    @Override
    public CommentDto findComment(long eventId, long commentId) {

        eventService.findEventById(eventId);
        Comment comment = commentRepository
                .findByIdAndStatus(commentId, CommentStatus.APPROVED)
                .orElseThrow(
                        notFoundException("Комментарий пользователя с идентификатором {0} к событию c идентификатором {1} уже существует!", commentId));

        return commentMapper.toDto(comment);
    }

    @Override
    public CommentDto findCommentById(long commentId) {
        return commentMapper.toDto(getCommentById(commentId));
    }

    @Override
    public CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto) {

        User user = userService.getUser(userId);
        Event event = eventService.findEventById(eventId);

        List<ParticipationRequest> participationRequests =
                requestRepository.findParticipationRequestByRequesterIdAndEventId(userId, eventId);

        if (participationRequests.isEmpty()) {
            throw new NotFoundException("Нет запросов пользователя с ID: {0} к событию с ID: {1}", userId, eventId);
        }

        commentRepository.findByAuthorIdAndEventId(user.getId(), event.getId())
                .ifPresent(value -> {
                    throw new DataAlreadyExistException("Комментарий пользователя с идентификатором {0} к событию c идентификатором {1} уже существует!",
                            user.getId(), event.getId());
                });

        final LocalDateTime timestamp = LocalDateTime.now();

        final Comment newComment = Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .author(user)
                .createdDate(timestamp)
                .status(CommentStatus.PENDING)
                .build();

        commentRepository.save(newComment);

        return commentMapper.toDto(newComment);
    }

    @Override
    public void deleteComment(long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.deleteById(comment.getId());
    }

    @Override
    public CommentDto patchCommentStatus(long commentId, CommentStatus status) {

        Comment comment = getCommentById(commentId);

        comment.setStatus(status);
        if (status == CommentStatus.APPROVED) {
            comment.setPublishedDate(LocalDateTime.now());
            comment.setUpdatedDate(LocalDateTime.now());
        } else if (status == CommentStatus.REJECTED) {
            comment.setUpdatedDate(LocalDateTime.now());
        }

        commentRepository.save(comment);

        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> findApprovedCommentsOnUserId(long userId) {

        userService.getUser(userId);
        List<Comment> comments = commentRepository.findByAuthorIdAndStatus(userId, CommentStatus.APPROVED);

        return commentMapper.toDto(comments);
    }

    @Override
    public List<CommentDto> findAllByAdminParams(AdminCommentParams params) {
        if (params.getCreatedDateStart() != null && params.getCreatedDateEnd() != null && !params.getCreatedDateEnd().isAfter(params.getCreatedDateStart())) {
            throw new ValidationException("createdDateEnd must be after createdDateStart");
        }

        if (params.getPublishedDateStart() != null && params.getPublishedDateEnd() != null && !params.getPublishedDateEnd().isAfter(params.getPublishedDateStart())) {
            throw new ValidationException("publishedDateEnd must be after publishedDateStart");
        }

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by("createdDate").descending()
        );

        Page<Comment> events = commentRepository.findAll(withAdminCommentParams(params), pageRequest);

        return commentMapper.toDto(events.stream().toList());
    }

    private Comment getCommentById(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(notFoundException("Комментарий с идентификатором {0} не найден!", commentId));
    }
}