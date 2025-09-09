package ru.practicum.explorewithme.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.users.model.EventRequestCount;
import ru.practicum.explorewithme.users.model.ParticipationRequest;
import ru.practicum.explorewithme.users.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findParticipationRequestByRequester_Id(long userId);

    long countParticipationRequestByRequesterIdAndEvent_Id(long userId, long eventId);

    List<ParticipationRequest> findParticipationRequestByRequesterIdAndEventId(long userId, long eventId);

    @Query("SELECT NEW ru.practicum.explorewithme.users.model.EventRequestCount(r.event.id, count(r.id)) FROM ParticipationRequest r WHERE r.status = :status AND r.event.id in :eventIds GROUP BY r.event ORDER BY count(r.id) ASC")
    List<EventRequestCount> findEventsCountByStatus(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);

    Optional<ParticipationRequest> findParticipationRequestByIdAndRequester_Id(long requestId, long userId);

    List<ParticipationRequest> findParticipationRequestByEvent_IdAndEvent_Initiator_Id(long eventId, long userId);

    long countParticipationRequestByEvent_IdAndStatus(long eventId, RequestStatus status);
}
