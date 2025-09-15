package ru.practicum.core.event.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.core.event.model.Category;

/**
 * Репозиторий для работы с сущностью {@link Category}.
 * <p>
 * Реализует базовые CRUD-операции через JpaRepository.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Проверяет, существует ли категория с указанным именем.
     *
     * @param name Имя категории для проверки.
     * @return true, если категория с таким именем существует, иначе false.
     */
    boolean existsByName(
            @NotBlank(message = "Название категории не может быть пустым")
            @Size(min = 1, max = 50, message = "Название категории должно быть от 1 до 50 символов") String name
    );
}
