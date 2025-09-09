package ru.practicum.explorewithme.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.comments.CommentService;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventsController {

    private final EventService eventService;
    private final CommentService commentService;

    @GetMapping
    public List<EventDto> getEvents(
            HttpServletRequest request,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam Optional<UserEventParams.EventSortEnum> sort,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "to", defaultValue = "10") Integer size
    ) {
        UserEventParams userEventParams = UserEventParams
                .builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        eventService.sendHit(request);
        return eventService.findAllByUserParams(userEventParams);
    }

    @GetMapping("/{eventId}")
    public EventDto getPublishedEvent(
            HttpServletRequest request,
            @NotNull @PathVariable Long eventId
    ) {
        eventService.sendHit(request);
        return eventService.findPublishedEvent(eventId);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getComments(@PathVariable Long eventId) {
        return commentService.findComments(eventId);
    }

    @GetMapping("/{eventId}/comments/{commentId}")
    public CommentDto getComment(@PathVariable Long eventId, @PathVariable Long commentId) {
        return commentService.findComment(eventId, commentId);
    }
}
