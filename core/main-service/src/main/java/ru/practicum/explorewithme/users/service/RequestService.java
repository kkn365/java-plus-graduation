package ru.practicum.explorewithme.users.service;

import ru.practicum.explorewithme.users.dto.ChangeRequestStatusDto;
import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.dto.UserParticipationRequestDto;

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
     * @throws ru.practicum.explorewithme.exception.NotFoundException если пользователь не найден
     */
    List<ParticipationRequestDto> getAllRequestsByUser(long userId);

    /**
     * Создаёт новую заявку на участие в событии.
     *
     * @param userId  Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return DTO созданной заявки
     * @throws ru.practicum.explorewithme.exception.NotFoundException  если пользователь или событие не найдены
     * @throws ru.practicum.explorewithme.exception.ConflictException  если пользователь является инициатором события,
     *                                                                   уже подал заявку, событие не опубликовано
     *                                                                   или достигло лимита участников
     */
    ParticipationRequestDto createRequest(long userId, long eventId);

    /**
     * Отменяет заявку пользователя на событие.
     *
     * @param userId     Идентификатор пользователя
     * @param requestId  Идентификатор заявки
     * @return DTO обновлённой заявки
     * @throws ru.practicum.explorewithme.exception.NotFoundException если заявка не найдена
     */
    ParticipationRequestDto cancelRequest(long userId, long requestId);

    /**
     * Возвращает заявки пользователя на конкретное событие.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return Список DTO заявок
     * @throws ru.practicum.explorewithme.exception.NotFoundException если пользователь или событие не найдены
     */
    List<ParticipationRequestDto> getUserRequestsForEvent(long userId, long eventId);

    /**
     * Изменяет статус заявок на событие.
     *
     * @param changeRequestStatusDto Данные для изменения статуса
     * @param userId                 Идентификатор пользователя (инициатор события)
     * @param eventId                Идентификатор события
     * @return DTO с результатами обработки заявок
     * @throws ru.practicum.explorewithme.exception.NotFoundException  если событие не найдено
     * @throws ru.practicum.explorewithme.exception.ConflictException  если заявки принадлежат разным событиям,
     *                                                                   имеют неподходящий статус или событие полностью забронировано
     */
    UserParticipationRequestDto updateRequestStatus(
            ChangeRequestStatusDto changeRequestStatusDto,
            long userId,
            long eventId);
}