package ru.practicum.core.event.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;
import ru.practicum.core.event.dto.comments.AdminCommentParams;
import ru.practicum.core.event.model.Comment;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Comment}.
 * <p>
 * Предоставляет методы для сохранения, поиска, обновления и удаления комментариев в базе данных.
 * Реализует интерфейсы {@link JpaRepository} и {@link JpaSpecificationExecutor},
 * что позволяет использовать стандартные операции CRUD и сложные условия фильтрации.
 */
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    /**
     * Находит комментарий по его идентификатору.
     *
     * @param id идентификатор комментария
     * @return Optional, содержащий найденный комментарий или пустой Optional, если комментарий не найден
     */
    Optional<Comment> findById(Long id);

    /**
     * Находит комментарий по его идентификатору и статусу.
     *
     * @param id     идентификатор комментария
     * @param status статус комментария
     * @return Optional, содержащий найденный комментарий или пустой Optional, если комментарий не найден
     */
    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    /**
     * Находит список комментариев, написанных определённым пользователем и имеющих заданный статус.
     *
     * @param userId идентификатор пользователя
     * @param status статус комментариев
     * @return список комментариев, удовлетворяющих условиям
     */
    List<Comment> findByAuthorIdAndStatus(Long userId, CommentStatus status);

    /**
     * Находит список комментариев, оставленных на конкретное событие и имеющих заданный статус.
     *
     * @param eventId идентификатор события
     * @param status  статус комментариев
     * @return список комментариев, удовлетворяющих условиям
     */
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    /**
     * Проверяет, существует ли комментарий, оставленный пользователем на указанное событие.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true, если комментарий существует, иначе false
     */
    Boolean existsByAuthorIdAndEventId(Long userId, Long eventId);

    /**
     * Спецификация для фильтрации комментариев по параметрам администратора.
     * Позволяет гибко строить запросы на основе переданных критериев.
     */
    class AdminCommentSpecification {
        public static Specification<Comment> withAdminCommentParams(AdminCommentParams params) {
            return (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Фильтр по списку ID комментариев
                if (params.getComments() != null && !params.getComments().isEmpty()) {
                    predicates.add(root.get("id").in(params.getComments()));
                }

                // Поиск по тексту (чувствительность к регистру отключена)
                if (StringUtils.hasText(params.getText())) {
                    String likePattern = "%" + params.getText().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("text")),
                            likePattern
                    ));
                }

                // Фильтр по событиям
                if (params.getEvents() != null && !params.getEvents().isEmpty()) {
                    predicates.add(root.get("event").in(params.getEvents()));
                }

                // Фильтр по авторам
                if (params.getAuthors() != null && !params.getAuthors().isEmpty()) {
                    predicates.add(root.get("author").in(params.getAuthors()));
                }

                // Фильтр по статусам
                if (params.getStatus() != null && !params.getStatus().isEmpty()) {
                    predicates.add(root.get("status").in(params.getStatus()));
                }

                // Диапазон даты создания
                if (params.getCreatedDateStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("createdDate"),
                            params.getCreatedDateStart()
                    ));
                }
                if (params.getCreatedDateEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("createdDate"),
                            params.getCreatedDateEnd()
                    ));
                }

                // Диапазон даты публикации
                if (params.getPublishedDateStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("publishedDate"),
                            params.getPublishedDateStart()
                    ));
                }
                if (params.getPublishedDateEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("publishedDate"),
                            params.getPublishedDateEnd()
                    ));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
        }
    }
}