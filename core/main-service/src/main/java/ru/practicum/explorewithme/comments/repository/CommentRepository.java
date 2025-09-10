package ru.practicum.explorewithme.comments.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;
import ru.practicum.explorewithme.comments.dto.AdminCommentParams;
import ru.practicum.explorewithme.comments.model.Comment;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с сущностью Comment.
 * Расширяет JpaRepository и JpaSpecificationExecutor для поддержки сложных запросов.
 */
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    Optional<Comment> findById(long id);

    Optional<Comment> findByIdAndStatus(long id, CommentStatus status);

    Optional<Comment> findByAuthorIdAndEventId(long userId, long eventId);

    List<Comment> findByAuthorIdAndStatus(long userId, CommentStatus status);

    List<Comment> findByEventIdAndStatus(long eventId, CommentStatus status);

    boolean existsByAuthorIdAndEventId(Long id, Long id1);

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