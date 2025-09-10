package ru.practicum.explorewithme.compilations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.compilations.model.Compilation;

/**
 * Репозиторий для работы с сущностью {@link Compilation}.
 * <p>
 * Предоставляет методы для поиска и управления подборками событий.
 */
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    /**
     * Возвращает страницу подборок событий, отфильтрованных по флагу pinned.
     *
     * @param pinned   флаг, указывающий, должны ли быть возвращены закреплённые (true) или незакреплённые (false) подборки
     * @param pageable параметры пагинации (номер страницы, размер страницы и сортировка)
     * @return страница объектов {@link Compilation}
     */
    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);
}