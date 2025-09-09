package ru.practicum.explorewithme.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.explorewithme.events.dto.AdminEventParams;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.model.Event;

import java.util.List;

public interface EventService {
    EventDto addEvent(NewEventDto newEventDto, Long userId);

    EventDto updateEventByUser(Long eventId, NewEventDto newEventDto, Long userId);

    EventDto updateEventByAdmin(Long eventId, NewEventDto newEventDto);

    List<EventDto> findAllByParams(Long userId, Integer from, Integer to);

    EventDto findUserEvent(Long userId, Long eventId);

    Event findEventById(Long eventId);

    List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams);

    List<EventDto> findAllByUserParams(UserEventParams userEventParams);

    void sendHit(HttpServletRequest request);

    EventDto findPublishedEvent(Long eventId);
}
