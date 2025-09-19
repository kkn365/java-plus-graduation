package ru.practicum.recomm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Модель данных для хранения информации о схожести между двумя мероприятиями.
 * <p>
 * Представляет собой сущность, которая отражает степень сходства между двумя мероприятиями.
 * Используется в системе анализа и рекомендаций для поиска похожих мероприятий на основе пользовательских действий.
 */
@Entity
@Table(name = "event_similarity", schema = "public")
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarity {

    /**
     * Уникальный идентификатор записи в таблице.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * Идентификатор первого мероприятия в паре.
     * Обычно используется как исходное мероприятие для сравнения.
     */
    @Column(name = "source_event_id")
    private Long sourceEventId;

    /**
     * Идентификатор второго мероприятия в паре.
     * Сравнивается с первым мероприятием.
     */
    @Column(name = "target_event_id")
    private Long targetEventId;

    /**
     * Степень сходства между двумя мероприятиями.
     * Значение находится в диапазоне [0.0, 1.0], где:
     * - 0.0 означает полное отсутствие сходства,
     * - 1.0 означает полное совпадение.
     */
    @Column(name = "similarity_score")
    private Double similarityScore;

    /**
     * Время расчёта сходства между мероприятиями.
     * Хранит информацию о дате и времени, когда была вычислена степень схожести.
     */
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
}