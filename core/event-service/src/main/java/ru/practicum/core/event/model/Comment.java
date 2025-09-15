package ru.practicum.core.event.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность комментария.
 * <p>
 * Представляет собой текстовый комментарий пользователя к событию.
 * Может находиться в одном из состояний: PENDING, APPROVED, REJECTED.
 */
@Entity
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", schema = "public")
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Текст комментария.
     * <p>
     * Обязательное поле. Длина от 1 до 2000 символов.
     */
    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * Событие, к которому относится комментарий.
     * <p>
     * Связь с моделью {@link Event}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event event;

    /**
     * Статус комментария.
     * <p>
     * Определяет жизненный цикл комментария (ожидает модерации, опубликован, отклонён).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    /**
     * Дата и время создания комментария.
     * <p>
     * Устанавливается автоматически при сохранении.
     */
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Дата и время последнего обновления комментария.
     * <p>
     * Устанавливается автоматически при каждом сохранении сущности.
     */
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    /**
     * Дата и время публикации комментария.
     * <p>
     * Заполняется только если статус = APPROVED.
     */
    @UpdateTimestamp
    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Comment comment = (Comment) o;
        return getId() != null && Objects.equals(getId(), comment.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
