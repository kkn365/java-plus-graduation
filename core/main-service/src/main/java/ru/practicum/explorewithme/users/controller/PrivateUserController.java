package ru.practicum.explorewithme.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comments.service.CommentService;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.RequestMethod;
import ru.practicum.explorewithme.events.service.EventService;
import ru.practicum.explorewithme.users.dto.ChangeRequestStatusDto;
import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.dto.UserParticipationRequestDto;
import ru.practicum.explorewithme.users.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateUserController {

    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findAllRequests(@PathVariable long userId) {
        log.info("Выполняется получение всех запросов пользователя: {}", userId);
        return requestService.findAllRequestsByUserId(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto save(@PathVariable long userId,
                                        @RequestParam("eventId") @NotNull long eventId) {
        log.info("Выполняется добавление запроса от пользователя {} на участие в событии: {}", userId, eventId);
        return requestService.save(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.info("Выполняется отмена запроса пользователя {} на участие по запросу: {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/events")
    public List<EventDto> getEvents(
            @Valid @NotNull @PathVariable Long userId,
            @Valid @RequestParam(value = "from", defaultValue = "0") Integer from,
            @Valid @RequestParam(value = "to", defaultValue = "10") Integer to
    ) {

        return eventService.findAllByParams(userId, from, to);
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(
            @Valid @NotNull @PathVariable Long userId,
            @Validated({ru.practicum.explorewithme.events.dto.RequestMethod.Create.class}) @RequestBody NewEventDto eventDto
    ) {
        return eventService.addEvent(eventDto, userId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventDto getUserEvent(
            @Valid @NotNull @PathVariable Long userId,
            @Valid @NotNull @PathVariable Long eventId
    ) {

        return eventService.findUserEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventDto updateUserEvent(
            @Valid @NotNull @PathVariable Long userId,
            @Valid @NotNull @PathVariable Long eventId,
            @Validated({RequestMethod.Update.class}) @RequestBody NewEventDto eventDto
    ) {

        return eventService.updateEventByUser(eventId, eventDto, userId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findUserRequestsOnEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.findUserRequestsOnEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public UserParticipationRequestDto patchRequestStatus(@RequestBody ChangeRequestStatusDto changeRequestStatusDto,
                                                          @PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.patchRequestStatus(changeRequestStatusDto, userId, eventId);
    }

    @PostMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody NewCommentDto newCommentDto
    ) {
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> findApprovedCommentsOnUser(@PathVariable Long userId) {
        return commentService.findApprovedCommentsOnUserId(userId);
    }
}