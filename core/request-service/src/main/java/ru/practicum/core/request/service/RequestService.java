package ru.practicum.core.request.service;

import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;

import java.util.List;

/**
 * Интерфейс сервиса для работы с заявками на участие в событиях.
 * <p>
 * Определяет методы для создания, получения, отмены и изменения статуса заявок.
 */
public interface RequestService {
    /**
     * Возвращает все заявки пользователя.
     *
     * @param userId Идентификатор пользователя
     * @return Список DTO заявок
     * @throws NotFoundException если пользователь не найден
     */
    List<ParticipationRequestDto> getAllRequestsByUser(Long userId);

    /**
     * Создаёт новую заявку на участие в событии.
     *
     * @param userId  Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return DTO созданной заявки
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConflictException если пользователь является инициатором события,
     *                                уже подал заявку, событие не опубликовано
     *                                или достигло лимита участников
     */
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    /**
     * Отменяет заявку пользователя на событие.
     *
     * @param userId    Идентификатор пользователя
     * @param requestId Идентификатор заявки
     * @return DTO обновлённой заявки
     * @throws ru.practicum.core.api.exception.NotFoundException если заявка не найдена
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    /**
     * Получает список заявок на конкретное событие, принадлежащее пользователю.
     *
     * @param userId  Идентификатор пользователя
     * @return Список DTO заявок
     * @throws NotFoundException если пользователь или событие не найдены
     */
    List<ParticipationRequestDto> getUserRequestsForEvent(Long userId, Long eventId);

    /**
     * Изменяет статус заявок на событие.
     *
     * @param changeRequestStatusDto Данные для изменения статуса
     * @param userId                 Идентификатор пользователя (инициатор события)
     * @param eventId                Идентификатор события
     * @return DTO с результатами обработки заявок
     * @throws ru.practicum.core.api.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.core.api.exception.ConflictException если заявки принадлежат разным событиям,
     *                                                           имеют неподходящий статус или событие
     *                                                           полностью забронировано
     */
    UserParticipationRequestDto updateRequestStatus(
            ChangeRequestStatusDto changeRequestStatusDto,
            Long userId,
            Long eventId);

    /**
     * Возвращает количество заявок на участие в мероприятиях по их идентификаторам.
     * <p>
     * Метод позволяет получить статистику по количеству поданных заявок для каждого из указанных событий.
     * Результат возвращается в виде списка DTO, где каждая запись содержит идентификатор события и количество заявок.
     *
     * @param eventIds Список идентификаторов событий, для которых нужно получить количество заявок
     * @return Список DTO, где каждый элемент содержит идентификатор события и количество заявок
     */
    List<EventRequestsCountDto> getEventRequestsCount(List<Long> eventIds);

    /**
     * Проверяет, существует ли заявка на участие пользователя в событии.
     * @param userId Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return true, если заявка существует, иначе false
     */
    Boolean hasRequest(Long userId, Long eventId);
}