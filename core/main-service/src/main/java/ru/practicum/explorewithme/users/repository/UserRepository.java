package ru.practicum.explorewithme.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.users.model.User;

/**
 * Репозиторий для работы с сущностью User.
 * <p>
 * Предоставляет методы для поиска и проверки существования пользователей.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет, существует ли пользователь с указанным email.
     * <p>
     * Метод автоматически генерирует JPQL-запрос на основе названия метода.
     *
     * @param email адрес электронной почты пользователя
     * @return true, если пользователь с таким email существует, иначе false
     */
    boolean existsByEmail(String email);
}