package ru.practicum.core.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Сущность пользователя.
 * <p>
 * Представляет пользователя в системе с идентификатором, именем и электронной почтой.
 */
@Entity
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}