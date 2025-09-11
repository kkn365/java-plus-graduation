package ru.practicum.explorewithme.users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;

/**
 * Сущность пользователя.
 * <p>
 * Представляет пользователя в системе с идентификатором, именем и электронной почтой.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Entity
@Table(name = "users", schema = "public")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Уникальный идентификатор пользователя")
    private Long id;

    /**
     * Имя пользователя.
     * <p>
     * Обязательное поле. Максимальная длина — 250 символов.
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 250, message = "Максимальная длина имени — 250 символов")
    @Column(length = 250, nullable = false)
    @Comment("Имя пользователя")
    private String name;

    /**
     * Электронная почта пользователя.
     * <p>
     * Обязательное поле. Формат: корректный email. Максимальная длина — 254 символа. Уникальна в системе.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Size(max = 254, message = "Максимальная длина email — 254 символа")
    @Email(message = "Некорректный формат email")
    @Column(length = 254, nullable = false, unique = true)
    @Comment("Электронная почта пользователя (уникальна)")
    private String email;
}