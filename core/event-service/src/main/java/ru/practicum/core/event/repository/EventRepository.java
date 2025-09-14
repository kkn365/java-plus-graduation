package ru.practicum.core.event.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.event.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Event}.
 * <p>
 * Предоставляет методы для сохранения, поиска, обновления и удаления событий в базе данных.
 * Реализует интерфейсы {@link JpaRepository} и {@link JpaSpecificationExecutor},
 * что позволяет использовать стандартные операции CRUD и сложные условия фильтрации.
 */
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Метод для получения списка событий, инициированных пользователем с указанным ID.
     * Результат возвращается в виде объекта Page<Event>, что позволяет использовать пагинацию.
     *
     * @param initiator ID пользователя-инициатора событий
     * @param pageable  параметры пагинации (номер страницы, размер страницы, сортировка)
     * @return объект Page<Event> — события, инициированные пользователем
     */
    Page<Event> findAllByInitiatorId(
            @Param("initiator_id") Long initiator,
            Pageable pageable);

    /**
     * Метод для получения всех событий, инициированных пользователем с указанным ID.
     * В отличие от метода с Pageable, этот метод возвращает полный список событий без пагинации.
     *
     * @param initiatorId ID пользователя-инициатора событий
     * @return список событий, инициированных пользователем
     */
    List<Event> findAllByInitiatorId(Long initiatorId);

    /**
     * Спецификация для фильтра событий администратора.
     */
    class AdminEventSpec {
        public static Specification<Event> withAdminParams(AdminEventParams params) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Фильтр по инициаторам
                if (params.getUsers() != null) {
                    predicates.add(root.get("initiatorId").in(params.getUsers()));
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