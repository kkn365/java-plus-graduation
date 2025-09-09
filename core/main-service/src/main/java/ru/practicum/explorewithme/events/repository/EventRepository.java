package ru.practicum.explorewithme.events.repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findAllByInitiatorId(@Param("initiator_id") Long initiator, Pageable pageable);

    interface AdminEventSpecification {
        static Specification<Event> withAdminEventParams(AdminEventParams params) {
            return (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Фильтр по пользователям (initiator)
                if (params.getUsers() != null && !params.getUsers().isEmpty()) {
                    predicates.add(root.get("initiator").get("id").in(params.getUsers()));
                }

                // Фильтр по состояниям
                if (params.getStates() != null && !params.getStates().isEmpty()) {
                    predicates.add(root.get("state").in(params.getStates()));
                }

                // Фильтр по категориям
                if (params.getCategories() != null && !params.getCategories().isEmpty()) {
                    predicates.add(root.get("category").get("id").in(params.getCategories()));
                }

                // Фильтр по диапазону дат
                if (params.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));
                }
                if (params.getRangeEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));
                }

                // Возвращаем объединение всех условий через AND
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
        }
    }

    interface UserEventSpecification {
        static Specification<Event> withUserEventParams(UserEventParams params) {
            return (root, query, criteriaBuilder) -> {


                List<Predicate> predicates = new ArrayList<>();

                predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

                // Поиск по annotation или description
                if (StringUtils.hasText(params.getText())) {
                    String likePattern = "%" + params.getText().toLowerCase() + "%";
                    predicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), likePattern),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
                            )
                    );
                }

                // Фильтр по категориям
                if (params.getCategories() != null && !params.getCategories().isEmpty()) {
                    predicates.add(root.get("category").get("id").in(params.getCategories()));
                }

                // Только платные события
                if (params.getPaid() != null) {
                    if (params.getPaid()) {
                        predicates.add(criteriaBuilder.isTrue(root.get("paid")));
                    } else {
                        predicates.add(criteriaBuilder.isFalse(root.get("paid")));

                    }
                }

                // Проверка лимита участников
                if (params.getOnlyAvailable() != null && params.getOnlyAvailable()) {
                    // Создаем подзапрос для подсчета подтвержденных запросов
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ParticipationRequest> requestRoot = subquery.from(ParticipationRequest.class);
                    subquery.select(criteriaBuilder.count(requestRoot.get("id")))
                            .where(
                                    criteriaBuilder.equal(requestRoot.get("event").get("id"), root.get("id")),
                                    criteriaBuilder.equal(requestRoot.get("status"), RequestStatus.CONFIRMED)
                            )
                            .groupBy(requestRoot.get("event").get("id"));

                    predicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.isNull(subquery),
                                    criteriaBuilder.lessThan(subquery, root.get("participantLimit"))
                            )
                    );
                }

                // Фильтр по диапазону дат
                if (params.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));
                } else {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
                }

                if (params.getRangeEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));
                }

                // Возвращаем объединение всех условий через AND
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
        }
    }
}
