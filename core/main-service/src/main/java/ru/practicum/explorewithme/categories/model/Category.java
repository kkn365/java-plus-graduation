package ru.practicum.explorewithme.categories.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность категории событий.
 * <p>
 * Категория используется для классификации событий по темам или типам.
 */
@Entity
@Table(name = "categories", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Category {

    /**
     * Уникальный идентификатор категории.
     * Генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название категории.
     * Обязательное поле, должно быть уникальным в системе.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}