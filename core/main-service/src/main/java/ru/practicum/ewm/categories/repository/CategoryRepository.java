package ru.practicum.ewm.categories.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.categories.model.Category;

/**
 * Репозиторий для работы с категориями событий.
 * <p>
 * Реализует базовые CRUD-операции через JpaRepository.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
