package ru.practicum.ewm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Сущность Hit (просмотр).
 * <p>
 * Представляет собой запись о том, что какой-то пользователь посетил конкретный URI.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hits", schema = "public")
public class Hit {

    /**
     * Уникальный идентификатор просмотра.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название приложения, которое было просмотрено.
     * Обязательное поле.
     */
    @Column(nullable = false)
    private String app;

    /**
     * URI, который был посещён.
     * Обязательное поле.
     */
    @Column(nullable = false)
    private String uri;

    /**
     * IP-адрес пользователя, совершившего запрос.
     * Обязательное поле, максимальная длина — 45 символов.
     */
    @Column(nullable = false, length = 45)
    private String ip;

    /**
     * Дата и время, когда произошёл просмотр.
     * Обязательное поле.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
}