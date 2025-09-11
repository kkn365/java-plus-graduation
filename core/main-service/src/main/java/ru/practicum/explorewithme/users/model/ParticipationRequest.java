package ru.practicum.explorewithme.users.model;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explorewithme.events.model.Event;

import java.time.LocalDateTime;

/**
 * Сущность заявки на участие в событии.
 * <p>
 * Содержит идентификатор, ссылку на событие, пользователя, дату создания и статус заявки.
 */
@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participation_requests", schema = "public")
public class ParticipationRequest {

    /**
     * Уникальный идентификатор заявки.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Событие, на которое подана заявка.
     * <p>
     * Используется FetchType.LAZY для оптимизации производительности.
     * Связь с таблицей событий по полю event_id.
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * Пользователь, подавший заявку.
     * <p>
     * FetchType.LAZY используется для оптимизации загрузки данных.
     * Связь с таблицей пользователей по полю requester_id.
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * Дата и время создания заявки.
     * <p>
     * Обязательное поле.
     */
    @Column(nullable = false)
    private LocalDateTime created;

    /**
     * Статус заявки.
     * <p>
     * Хранится как строка в БД для удобства чтения и отладки.
     * Обязательное поле.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
}