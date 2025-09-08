package ru.practicum.explorewithme.events.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.hit.HitClient;
import ru.practicum.client.stats.StatsClient;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.service.CategoryService;
import ru.practicum.explorewithme.events.dto.AdminEventParams;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.events.enumeration.EventStateAction;
import ru.practicum.explorewithme.events.mapper.EventMapper;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.model.EventRequestCount;
import ru.practicum.explorewithme.users.model.RequestStatus;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.RequestRepository;
import ru.practicum.explorewithme.users.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.events.repository.EventRepository.AdminEventSpecification.withAdminEventParams;
import static ru.practicum.explorewithme.events.repository.EventRepository.UserEventSpecification.withUserEventParams;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;
    private final StatsClient statsClient;

    @Override
    public EventDto addEvent(NewEventDto newEventDto, Long userId) {
        User user = userService.getUser(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        EventDto eventDto = eventMapper.convertShortDto(newEventDto);
        Event event = eventMapper.toModel(eventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());

        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }

        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }

        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        Event savedEvent = eventRepository.save(event);
        EventDto savedEventDto = eventMapper.toDto(savedEvent);

        savedEventDto.setViews(0L);
        savedEventDto.setConfirmedRequests(0);

        return savedEventDto;
    }

    @Override
    public EventDto updateEventByUser(Long eventId, NewEventDto newEventDto, Long userId) {
        Event event = findEventById(eventId);
        checkInitiatorId(event, userId);

        if (!List.of(EventState.CANCELED, EventState.PENDING).contains(event.getState())) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        return doUpdateEvent(event, newEventDto);
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, NewEventDto newEventDto) {
        Event event = findEventById(eventId);

        if (!Objects.isNull(newEventDto.getStateAction())) {
            if (!List.of(EventStateAction.PUBLISH_EVENT, EventStateAction.REJECT_EVENT).contains(newEventDto.getStateAction())) {
                throw new ConflictException("Invalid action: " + newEventDto.getStateAction());
            }

            if (newEventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
                throw new ConflictException("Cannot change state the event because it's not in the right state: PENDING");
            }

            if (newEventDto.getStateAction().equals(EventStateAction.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Cannot change state the event because it's not in the right state: PUBLISHED");
            }

            if (newEventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                LocalDateTime checkPublishDate = LocalDateTime.now().plusHours(1L);
                if (
                        (!Objects.isNull(newEventDto.getEventDate()) && checkPublishDate.isAfter(newEventDto.getEventDate()))
                                || checkPublishDate.isAfter(event.getEventDate())
                ) {
                    throw new ConflictException("The start date of the modified event must be no earlier than one hour from the publication date");
                }
            }
        }

        return doUpdateEvent(event, newEventDto);
    }

    @Override
    public List<EventDto> findAllByParams(Long userId, Integer from, Integer size) {
        UserDto userDto = userService.getById(userId);
        int page = from / size;

        List<Event> events = eventRepository.findAllByInitiatorId(userDto.getId(), PageRequest.of(page, size)).stream().toList();

        List<EventDto> eventDtos = eventMapper.toDto(events);

        loadViews(
                eventDtos,
                eventDtos.stream().map(EventDto::getCreatedOn).min(LocalDateTime::compareTo).orElse(LocalDateTime.now()),
                eventDtos.stream().map(EventDto::getEventDate).min(LocalDateTime::compareTo).orElse(null)
        );
        loadConfirmedRequests(eventDtos);

        return eventDtos;
    }

    @Override
    public EventDto findUserEvent(Long userId, Long eventId) {
        Event event = findEventById(eventId);
        checkInitiatorId(event, userId);

        EventDto eventDto = eventMapper.toDto(event);

        loadViews(List.of(eventDto), event.getCreatedOn(), event.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    @Override
    public List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams) {
        PageRequest pageRequest = PageRequest.of(
                adminEventParams.getFrom() / adminEventParams.getSize(),
                adminEventParams.getSize(),
                Sort.by("eventDate").ascending()
        );

        Page<Event> events = eventRepository.findAll(withAdminEventParams(adminEventParams), pageRequest);

        List<EventDto> eventDtos = eventMapper.toDto(events.stream().toList());
        loadViews(eventDtos, adminEventParams.getRangeStart(), adminEventParams.getRangeEnd());
        loadConfirmedRequests(eventDtos);

        return eventDtos;
    }

    @Override
    public List<EventDto> findAllByUserParams(UserEventParams userEventParams) {
        if (userEventParams.getRangeStart() != null && userEventParams.getRangeEnd() != null && !userEventParams.getRangeEnd().isAfter(userEventParams.getRangeStart())) {
            throw new ValidationException("rangeEnd must be after rangeStart");
        }

        PageRequest pageRequest = PageRequest.of(
                userEventParams.getFrom() / userEventParams.getSize(),
                userEventParams.getSize(),
                Sort.by("eventDate").ascending()
        );

        Page<Event> events = eventRepository.findAll(withUserEventParams(userEventParams), pageRequest);

        List<EventDto> eventDtos = eventMapper.toDto(events.stream().toList());
        loadViews(eventDtos, userEventParams.getRangeStart(), userEventParams.getRangeEnd());
        loadConfirmedRequests(eventDtos);

        if (userEventParams.getSort().isPresent() && userEventParams.getSort().get().equals(UserEventParams.EventSortEnum.VIEWS)) {
            eventDtos.sort(Comparator.comparing(EventDto::getViews).reversed());
        }

        return eventDtos;
    }

    @Override
    public void sendHit(HttpServletRequest request) {
        hitClient.hit(CreateHitDTO
                .builder()
                .app("main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Override
    public EventDto findPublishedEvent(Long eventId) {
        Event event = findEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        EventDto eventDto = eventMapper.toDto(event);

        loadViews(List.of(eventDto), event.getCreatedOn(), event.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
    }

    private void checkInitiatorId(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Event with id " + event.getId() + "don't compare with initiator " + userId);
        }
    }

    private void loadConfirmedRequests(List<EventDto> events) {
        List<Long> eventIds = events
                .stream()
                .map(EventDto::getId)
                .toList();

        List<EventRequestCount> eventsCountByStatus = requestRepository.findEventsCountByStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> requests = eventsCountByStatus
                .stream()
                .collect(Collectors.toMap(
                        EventRequestCount::getEventId,
                        EventRequestCount::getRequestsCount
                ));

        events.forEach(event -> {
            Long requestCount = requests.get(event.getId());
            requestCount = Objects.isNull(requestCount) ? 0L : requestCount;
            event.setConfirmedRequests(Math.toIntExact(requestCount));
        });
    }

    private void loadViews(List<EventDto> events, LocalDateTime start, LocalDateTime end) {
        Map<Long, String> eventIds = events
                .stream()
                .collect(Collectors.toMap(
                        EventDto::getId,
                        event -> "/events/" + event.getId()
                ));

        Optional<Collection<HitsStatDTO>> hitsStatDTOS = statsClient.getAll(
                start,
                end,
                eventIds.values().stream().toList(),
                true
        );

        if (hitsStatDTOS.isPresent() && !hitsStatDTOS.get().isEmpty()) {
            Map<String, Long> hitsStats = hitsStatDTOS.get()
                    .stream()
                    .collect(Collectors.toMap(HitsStatDTO::getUri, HitsStatDTO::getHits));

            events.forEach(event -> {
                Long hit = hitsStats.get(eventIds.get(event.getId()));
                event.setViews(Objects.isNull(hit) ? 0L : hit);
            });

        } else {
            events.forEach(event -> event.setViews(0L));
        }
    }

    private EventDto doUpdateEvent(Event currentEvent, NewEventDto newEventDto) {
        if (!Objects.isNull(newEventDto.getAnnotation())) {
            currentEvent.setAnnotation(newEventDto.getAnnotation());
        }

        if (!Objects.isNull(newEventDto.getCategory())) {
            currentEvent.setCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        }

        if (!Objects.isNull(newEventDto.getDescription())) {
            currentEvent.setDescription(newEventDto.getDescription());
        }

        if (!Objects.isNull(newEventDto.getEventDate())) {
            currentEvent.setEventDate(newEventDto.getEventDate());
        }

        if (!Objects.isNull(newEventDto.getLocation())) {
            currentEvent.setLocationLat(newEventDto.getLocation().getLat());
            currentEvent.setLocationLon(newEventDto.getLocation().getLon());
        }

        if (!Objects.isNull(newEventDto.getPaid())) {
            currentEvent.setPaid(newEventDto.getPaid());
        }

        if (!Objects.isNull(newEventDto.getParticipantLimit())) {
            currentEvent.setParticipantLimit(newEventDto.getParticipantLimit());
        }

        if (!Objects.isNull(newEventDto.getRequestModeration())) {
            currentEvent.setRequestModeration(newEventDto.getRequestModeration());
        }

        if (!Objects.isNull(newEventDto.getTitle())) {
            currentEvent.setTitle(newEventDto.getTitle());
        }

        if (!Objects.isNull(newEventDto.getStateAction())) {
            switch (newEventDto.getStateAction()) {
                case REJECT_EVENT, CANCEL_REVIEW -> currentEvent.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> {
                    currentEvent.setState(EventState.PUBLISHED);
                    currentEvent.setPublishedOn(LocalDateTime.now());
                }
                case SEND_TO_REVIEW -> currentEvent.setState(EventState.PENDING);
            }
        }

        eventRepository.save(currentEvent);

        EventDto eventDto = eventMapper.toDto(currentEvent);

        loadViews(List.of(eventDto), currentEvent.getCreatedOn(), currentEvent.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
    }
}