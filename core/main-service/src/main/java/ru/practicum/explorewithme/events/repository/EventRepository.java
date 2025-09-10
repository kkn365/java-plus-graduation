package ru.practicum.explorewithme.events.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import ru.practicum.explorewithme.events.dto.AdminEventParams;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.users.model.ParticipationRequest;
import ru.practicum.explorewithme.users.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findAllByInitiatorId(@Param("initiator_id") Long initiator, Pageable pageable);

    /**
     * Спецификация для фильтра событий администратора.
     */
    class AdminEventSpec {
        public static Specification<Event> withAdminParams(AdminEventParams params) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Фильтр по инициаторам
                if (params.getUsers() != null) {
                    predicates.add(root.get("initiator").get("id").in(params.getUsers()));
                }

                // Фильтр по статусам
                if (params.getStates() != null) {
                    predicates.add(root.get("state").in(params.getStates()));
                }

                // Фильтр по категориям
                if (params.getCategories() != null) {
                    predicates.add(root.get("category").get("id").in(params.getCategories()));
                }

                // Диапазон дат
                addDatePredicates(cb, root, predicates, params.getRangeStart(), params.getRangeEnd());

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }

    /**
     * Спецификация для фильтра событий пользователя.
     */
    class UserEventSpec {
        public static Specification<Event> withUserParams(UserEventParams params) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Основной фильтр: только опубликованные события
                predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

                // Поиск по тексту
                if (StringUtils.hasText(params.getText())) {
                    String pattern = "%" + params.getText().toLowerCase() + "%";
                    predicates.add(
                            cb.or(
                                    cb.like(cb.lower(root.get("annotation")), pattern),
                                    cb.like(cb.lower(root.get("description")), pattern)
                            )
                    );
                }

                // Фильтр по категориям
                if (params.getCategories() != null) {
                    predicates.add(root.get("category").get("id").in(params.getCategories()));
                }

                // Платные/бесплатные
                if (params.getPaid() != null) {
                    predicates.add(cb.equal(root.get("paid"), params.getPaid()));
                }

                // Доступность по лимиту участников
                if (params.getOnlyAvailable() != null && params.getOnlyAvailable()) {
                    Subquery<Long> subquery = query != null ? query.subquery(Long.class) : null;
                    Root<ParticipationRequest> reqRoot = Objects.requireNonNull(subquery).from(ParticipationRequest.class);
                    subquery.select(cb.count(reqRoot.get("id")))
                            .where(
                                    cb.equal(reqRoot.get("event").get("id"), root.get("id")),
                                    cb.equal(reqRoot.get("status"), RequestStatus.CONFIRMED)
                            )
                            .groupBy(reqRoot.get("event").get("id"));

                    predicates.add(
                            cb.or(
                                    cb.isNull(subquery),
                                    cb.lessThan(subquery, root.get("participantLimit"))
                            )
                    );
                }

                // Диапазон дат
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = params.getRangeStart() != null ? params.getRangeStart() : now;

                addDatePredicates(cb, root, predicates, start, params.getRangeEnd());

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }

    /**
     * Вспомогательный метод для добавления условий по диапазону дат.
     */
    private static void addDatePredicates(CriteriaBuilder cb, Root<Event> root,
                                          List<Predicate> predicates,
                                          LocalDateTime start, LocalDateTime end) {
        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        }
        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
        }
    }
}