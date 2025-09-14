package ru.practicum.core.event.repository;

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
}
