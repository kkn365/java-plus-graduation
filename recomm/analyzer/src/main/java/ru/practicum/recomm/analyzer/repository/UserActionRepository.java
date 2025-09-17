package ru.practicum.recomm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.recomm.analyzer.model.UserAction;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link UserAction}.
 * <p>
 * Предоставляет методы для поиска, сохранения и обновления записей о взаимодействии пользователей с мероприятиями.
 */
@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    /**
     * Получение всех действий пользователя.
     * @param userId  идентификатор пользователя
     * @return        List<UserAction> — список действий пользователя
     */
    List<UserAction> findByUserId(Long userId);

    /**
     * Возвращает список всех действий, связанных с заданным списком идентификаторов мероприятий.
     * <p>
     * Метод используется для получения информации о взаимодействии пользователей с несколькими мероприятиями.
     *
     * @param eventIds список идентификаторов мероприятий
     * @return список объектов {@link UserAction}, соответствующих переданным идентификаторам
     */
    List<UserAction> findByEventIdIn(List<Long> eventIds);

    /**
     * Возвращает список всех действий, связанных с конкретным мероприятием.
     * <p>
     * Метод используется для получения информации о взаимодействии пользователей с одним мероприятием.
     *
     * @param eventId идентификатор мероприятия
     * @return список объектов {@link UserAction}, соответствующих указанному мероприятию
     */
    List<UserAction> findByEventId(Long eventId);

    /**
     * Ищет запись о взаимодействии пользователя с мероприятием по их идентификаторам.
     *
     * @param userId   идентификатор пользователя
     * @param eventId  идентификатор мероприятия
     * @return         {@link Optional} объект {@link UserAction}, если запись найдена, иначе пустой Optional
     */
    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);
}