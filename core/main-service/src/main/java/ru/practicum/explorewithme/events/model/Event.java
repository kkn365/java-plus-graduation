package ru.practicum.explorewithme.events.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.users.model.User;

import java.time.LocalDateTime;

/**
 * Модель события.
 * <p>
 * Представляет событие в системе, включая его описание, категорию, дату, лимит участников и статус.
 */
@Data
@Entity
@Table(name = "events", schema = "public")
public class Event {
    /**
     * Уникальный идентификатор события.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название события. Не может быть пустым и не должно превышать 120 символов.
     */
    @Column(nullable = false, length = 120)
    private String title;

    /**
     * Краткое описание события (аннотация). Не может быть пустым и не должно превышать 2000 символов.
     */
    @Column(nullable = false, length = 2000)
    private String annotation;

    /**
     * Полное описание события. Не может быть пустым и не должно превышать 7000 символов.
     */
    @Column(nullable = false, length = 7000)
    private String description;

    /**
     * Категория события. Связь с моделью Category.
     */
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Инициатор события (пользователь). Связь с моделью User.
     */
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    /**
     * Дата и время начала события. Обязательное поле.
     */
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    /**
     * Дата и время создания события. Заполняется автоматически при создании.
     */
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    /**
     * Дата и время публикации события. Может быть null, если событие ещё не опубликовано.
     */
    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    /**
     * Широта местоположения события. Обязательное поле.
     */
    @Column(name = "location_lat", nullable = false)
    private Float locationLat;

    /**
     * Долгота местоположения события. Обязательное поле.
     */
    @Column(name = "location_lon", nullable = false)
    private Float locationLon;

    /**
     * Лимит участников события. По умолчанию 0.
     */
    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit = 0;

    /**
     * Признак платности события. По умолчанию false.
     */
    @Column(nullable = false)
    private Boolean paid = false;

    /**
     * Признак необходимости модерации заявок. По умолчанию false.
     */
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration = false;

    /**
     * Текущее состояние события. По умолчанию PENDING.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;

    /**
     * Возвращает значение флага requestModeration.
     *
     * @return true, если требуется модерация заявок, иначе false
     */
    public Boolean isRequestModeration() {
        return this.requestModeration;
    }
}