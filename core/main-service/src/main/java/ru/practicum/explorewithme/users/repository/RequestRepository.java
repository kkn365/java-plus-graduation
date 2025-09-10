package ru.practicum.explorewithme.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.users.model.EventRequestCount;
import ru.practicum.explorewithme.users.model.ParticipationRequest;
import ru.practicum.explorewithme.users.model.RequestStatus;

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
    List<ParticipationRequest> findByRequesterId(long requesterId);

    /**
     * Возвращает заявки пользователя на конкретное событие.
     *
     * @param requesterId Идентификатор пользователя (заявитель)
     * @param eventId     Идентификатор события
     * @return Список заявок, связанных с пользователем и событием
     */
    List<ParticipationRequest> findByRequesterIdAndEventId(long requesterId, long eventId);

    /**
     * Проверяет, существует ли заявка от пользователя на конкретное событие.
     *
     * @param requesterId Идентификатор пользователя (заявитель)
     * @param eventId Идентификатор события
     * @return true, если заявка существует
     */
    boolean existsByRequesterIdAndEventId(long requesterId, long eventId);

    /**
     * Возвращает заявку по её идентификатору и идентификатору пользователя.
     *
     * @param requestId Идентификатор заявки
     * @param requesterId Идентификатор пользователя (заявитель)
     * @return Опциональная заявка
     */
    Optional<ParticipationRequest> findByIdAndRequesterId(long requestId, long requesterId);

    /**
     * Возвращает заявки, связанные с событием и его инициатором.
     *
     * @param eventId Идентификатор события
     * @param initiatorId Идентификатор пользователя (инициатора события)
     * @return Список заявок
     */
    List<ParticipationRequest> findByEventIdAndEventInitiatorId(long eventId, long initiatorId);

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
            WHERE r.event.id = :id
              AND r.status = :status
            """)
    long countByEventIdAndStatus(
            @Param("id") long eventId,
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
            SELECT NEW ru.practicum.explorewithme.users.model.EventRequestCount(r.event.id, COUNT(r.id))
            FROM ParticipationRequest r
            WHERE r.status = :status
              AND r.event.id IN :eventIds
            GROUP BY r.event.id
            ORDER BY COUNT(r.id) ASC
            """)
    List<EventRequestCount> countByStatusForEvents(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RequestStatus status);
}