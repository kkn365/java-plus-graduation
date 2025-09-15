package ru.practicum.core.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.core.api.util.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Сущность заявки на участие в событии.
 * <p>
 * Представляет собой запрос пользователя на участие в событии.
 * Содержит информацию о событии, пользователе, дате подачи и статусе заявки.
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
     * <p>
     * Генерируется автоматически с помощью стратегии GenerationType.IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор события, на которое подана заявка.
     * <p>
     * Не может быть null.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Идентификатор пользователя, подавшего заявку.
     * <p>
     * Не может быть null.
     */
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    /**
     * Дата и время создания заявки.
     * <p>
     * Не может быть null.
     */
    @Column(nullable = false)
    private LocalDateTime created;

    /**
     * Текущий статус заявки (ОЖИДАНИЕ, ПОДТВЕРЖДЁН, ОТКЛОНЁН, ОТМЕНЁН).
     * <p>
     * Хранится в виде строки в БД.
     * Не может быть null.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
}