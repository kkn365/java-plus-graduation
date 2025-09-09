package ru.practicum.explorewithme.categories.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.categories.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
