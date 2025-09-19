package ru.practicum.recomm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Модель данных для хранения информации о пользовательских действиях.
 * <p>
 * Представляет собой сущность, которая отражает взаимодействие пользователя с мероприятием.
 * Используется в системе анализа и рекомендаций для обработки и хранения данных.
 */
@Entity
@Table(name = "user_actions",
        schema = "public",
        indexes = {
                @Index(name = "idx_user_event",
                        columnList = "user_id, event_id",
                        unique = true)
        })
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {

    /**
     * Уникальный идентификатор записи в таблице.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * Идентификатор пользователя, совершившего действие.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Идентификатор мероприятия, с которым произошло взаимодействие.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Вес или уровень интереса пользователя к мероприятию.
     * Может использоваться для расчёта схожести между мероприятиями.
     */
    @Column(name = "score", nullable = false)
    private Double score;

    /**
     * Время совершения действия (в формате LocalDateTime).
     * Хранит информацию о дате и времени взаимодействия.
     */
    @Column(name = "interact_at", nullable = false)
    private LocalDateTime interactAt;
}