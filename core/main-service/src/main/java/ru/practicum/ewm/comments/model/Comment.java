package ru.practicum.ewm.comments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.practicum.ewm.events.model.Event;

import java.time.LocalDateTime;

/**
 * Сущность комментария.
 * <p>
 * Представляет собой текстовый комментарий пользователя к событию.
 * Может находиться в одном из состояний: PENDING, APPROVED, REJECTED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
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
}
