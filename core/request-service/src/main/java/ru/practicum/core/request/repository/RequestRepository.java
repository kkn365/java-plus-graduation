package ru.practicum.core.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.core.api.util.enums.RequestStatus;
import ru.practicum.core.request.dto.EventRequestsCount;
import ru.practicum.core.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заявками на участие в событиях.
 * <p>
 * Содержит методы для поиска, фильтрации и подсчёта заявок.
 */
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    /**
     * Возвращает все заявки пользователя.
     *
     * @param requesterId Идентификатор пользователя (заявитель)
     * @return Список заявок
     */
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    /**
     * Проверяет, существует ли заявка от пользователя на конкретное событие.
     *
     * @param requesterId Идентификатор пользователя (заявитель)
     * @param eventId Идентификатор события
     * @return true, если заявка существует
     */
    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    /**
     * Возвращает заявку по её идентификатору и идентификатору пользователя.
     *
     * @param requestId Идентификатор заявки
     * @param requesterId Идентификатор пользователя (заявитель)
     * @return Опциональная заявка
     */
    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    /**
     * Возвращает заявки на событие.
     *
     * @param eventId Идентификатор события
     * @return Список заявок
     */
    List<ParticipationRequest> findAllByEventId(Long eventId);

    /**
     * Подсчитывает количество заявок на событие с указанным статусом.
     *
     * @param eventId Идентификатор события
     * @param status  Статус заявки
     * @return Количество заявок
     */
    @Query("""
            SELECT COUNT(r)
            FROM ParticipationRequest r
            WHERE r.eventId = :id
              AND r.status = :status
            """)
    Long countByEventIdAndStatus(
            @Param("id") Long eventId,
            @Param("status") RequestStatus status);

    /**
     * Получает количество заявок на события по статусу.
     * <p>
     * Группирует результаты по идентификатору события.
     *
     * @param eventIds Список идентификаторов событий
     * @param status   Статус заявки
     * @return Список объектов EventRequestCount
     */
    @Query("""
            SELECT NEW ru.practicum.core.request.dto.EventRequestsCount(r.eventId, COUNT(r.id))
            FROM ParticipationRequest r
            WHERE r.status = :status
              AND r.eventId IN :eventIds
            GROUP BY r.eventId
            ORDER BY COUNT(r.id) ASC
            """)
    List<EventRequestsCount> countByStatusForEvents(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RequestStatus status);
}