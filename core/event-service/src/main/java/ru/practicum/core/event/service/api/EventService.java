package ru.practicum.core.event.service.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.model.Event;

import java.util.List;

/**
 * Интерфейс сервиса для работы с событиями.
 * <p>
 * Определяет методы для создания, обновления, поиска и управления событиями.
 */
public interface EventService {
    /**
     * Создаёт новое событие для указанного пользователя.
     *
     * @param newEventDto данные нового события
     * @param userId      идентификатор пользователя (инициатор события)
     * @return DTO созданного события
     * @throws NotFoundException если пользователь или категория не найдены
     */
    EventDto addEvent(NewEventDto newEventDto, Long userId);

    /**
     * Обновляет событие, инициированное пользователем.
     * <p>
     * Проверяет, что событие принадлежит пользователю и находится в состоянии ОЖИДАНИЕ или ОТМЕНЕНО.
     *
     * @param eventId     идентификатор события
     * @param newEventDto данные для обновления события
     * @param userId      идентификатор пользователя (инициатор)
     * @return DTO обновлённого события
     * @throws ConflictException если событие не принадлежит пользователю или находится в недопустимом состоянии
     * @throws NotFoundException если событие не найдено
     */
    EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId);

    /**
     * Обновляет событие администратором.
     * <p>
     * Позволяет изменять состояние события (ОПУБЛИКОВАТЬ, ОТКЛОНИТЬ) и другие параметры.
     *
     * @param eventId     идентификатор события
     * @param newEventDto данные для обновления события
     * @return DTO обновлённого события
     * @throws ConflictException   если действие или состояние события недопустимы
     * @throws ValidationException если данные запроса некорректны
     * @throws NotFoundException   если событие не найдено
     */
    EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest newEventDto);

    /**
     * Получает список событий пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param from   начальная позиция для пагинации
     * @param size   размер страницы для пагинации
     * @return список DTO событий
     * @throws NotFoundException если пользователь не найден
     */
    List<EventDto> findAllByParams(Long userId, Integer from, Integer size);

    /**
     * Получает событие пользователя по его ID.
     * <p>
     * Проверяет, что событие принадлежит указанному пользователю.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return DTO события
     * @throws ConflictException если событие не принадлежит пользователю
     * @throws NotFoundException если событие или пользователь не найдены
     */
    EventDto findUserEvent(Long userId, Long eventId);

    /**
     * Получает событие по его ID.
     *
     * @param eventId идентификатор события
     * @return модель события
     * @throws NotFoundException если событие не найдено
     */
    Event findEventById(Long eventId);

    /**
     * Получает список событий по параметрам администратора.
     * <p>
     * Поддерживает фильтрацию по пользователям, категориям, статусу и временному диапазону.
     *
     * @param adminEventParams параметры фильтрации
     * @return список DTO событий
     * @throws ValidationException если параметры фильтрации некорректны
     */
    List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams);

    /**
     * Получает список событий по параметрам пользователя.
     * <p>
     * Поддерживает фильтрацию по тексту, категории, платности, доступности и временному диапазону.
     *
     * @param userEventParams параметры фильтрации
     * @return список DTO событий
     * @throws ValidationException если параметры фильтрации некорректны
     */
    List<EventDto> findAllByUserParams(UserEventParams userEventParams);

    EventDto findPublishedEvent(Long eventId, Long userId);

    /**
     * Получает событие по идентификатору.
     * <p>
     * Если событие не найдено, выбрасывается NotFoundException.
     *
     * @param eventId идентификатор события
     * @return DTO события
     * @throws NotFoundException если событие не найдено
     */
    EventDto findEventDtoById(Long eventId);

    /**
     * Получает список событий, инициированных указанным пользователем.
     *
     * @param initiatorId идентификатор пользователя-инициатора
     * @return список DTO событий
     * @throws NotFoundException если пользователь не найден
     */
    List<EventDto> findAllEventsByInitiatorId(Long initiatorId);

    List<EventDto> getRecommendations(Long userId);

    void addLike(Long eventId, Long userId);
}