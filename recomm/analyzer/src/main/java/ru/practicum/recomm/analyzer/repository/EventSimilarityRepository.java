package ru.practicum.recomm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.recomm.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Репозиторий для работы с сущностью {@link EventSimilarity}.
 * <p>
 * Предоставляет методы для поиска, сохранения и обновления записей о схожести между мероприятиями.
 */
@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    /**
     * Находит схожесть между двумя мероприятиями.
     * @param sourceEventId идентификатор первого мероприятия
     * @param targetEventId идентификатор второго мероприятия
     * @return Optional<EventSimilarity> — запись схожести, если найдена
     */
    Optional<EventSimilarity> findBySourceEventIdAndTargetEventId(Long sourceEventId, Long targetEventId);

    /**
     * Находит все записи схожести, связанные с указанными мероприятиями.
     * @param sourceEventId идентификатор первого мероприятия
     * @param targetEventId идентификатор второго мероприятия
     * @return List<EventSimilarity> — список связанных записей
     */
    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE es.sourceEventId = :sourceEventId OR es.targetEventId = :targetEventId
            """)
    List<EventSimilarity> findAllBySourceEventIdOrTargetEventId(Long sourceEventId, Long targetEventId);

    /**
     * Находит все записи схожести для заданных списков мероприятий.
     * @param sourceEventIds список идентификаторов первых мероприятий
     * @param targetEventIds список идентификаторов вторых мероприятий
     * @return List<EventSimilarity> — список связанных записей
     */
    @Query("""
            SELECT DISTINCT es
            FROM EventSimilarity es
            WHERE es.sourceEventId IN :sourceEventIds OR es.targetEventId IN :targetEventIds
            """)
    List<EventSimilarity> findAllBySourceEventIdInOrTargetEventIdIn(Set<Long> sourceEventIds, Set<Long> targetEventIds);
}