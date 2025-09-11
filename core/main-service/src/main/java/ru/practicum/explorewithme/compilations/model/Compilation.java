package ru.practicum.explorewithme.compilations.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explorewithme.events.model.Event;

import java.util.Set;

/**
 * Модель сущности подборки событий.
 * <p>
 * Содержит заголовок, флаг закрепления и коллекцию связанных событий.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "events")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilations", schema = "public")
public class Compilation {

    /**
     * Уникальный идентификатор подборки.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заголовок подборки.
     * <p>
     * Обязательное поле при создании.
     */
    private String title;

    /**
     * Флаг закрепления подборки.
     * <p>
     * True — подборка отображается на главной странице, false — нет.
     */
    private Boolean pinned;

    /**
     * Связь с событиями.
     * <p>
     * Подборка может включать несколько событий.
     * Таблица связи: compilation_events.
     */
    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;
}