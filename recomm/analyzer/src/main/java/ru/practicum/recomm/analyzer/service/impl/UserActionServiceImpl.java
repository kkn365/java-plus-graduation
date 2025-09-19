package ru.practicum.recomm.analyzer.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.recomm.analyzer.model.UserAction;
import ru.practicum.recomm.analyzer.repository.UserActionRepository;
import ru.practicum.recomm.analyzer.service.api.UserActionService;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.UserActionAvro;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Реализация сервиса для обработки пользовательских действий.
 * <p>
 * Обрабатывает действия, полученные из Kafka в формате Avro, и сохраняет их в репозитории.
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final UserActionRepository userActionRepository;

    /**
     * Обрабатывает действие пользователя с мероприятием.
     * <p>
     * Если такое действие уже существует и новый вес больше текущего — обновляет его.
     * Если такого действия нет — создаёт новое.
     *
     * @param userActionAvro объект с данными о пользовательском действии в формате Avro
     */
    @Override
    public void handleUserAction(UserActionAvro userActionAvro) {
        if (userActionAvro == null) {
            log.warn("Получен null-запрос о пользовательском действии");
            return;
        }

        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double newScore = calcInteractionScore(userActionAvro.getActionType());

        log.debug("Обработка действия пользователя {}: {}", userId, userActionAvro.getActionType());

        // Определяем локальное время из timestamp Avro (UTC)
        LocalDateTime interactAt = LocalDateTime.ofInstant(
                userActionAvro.getTimestamp(), ZoneId.of("UTC"));

        userActionRepository.findByUserIdAndEventId(userId, eventId)
                .ifPresentOrElse(
                        existingAction -> {
                            if (existingAction.getScore() < newScore) {
                                updateExistingAction(existingAction, newScore, interactAt);
                            } else {
                                log.debug("Действие пользователя {} с мероприятием {} не обновлено: текущий вес {} >= {}",
                                        userId, eventId, existingAction.getScore(), newScore);
                            }
                        },
                        () -> {
                            saveNewAction(userId, eventId, newScore, interactAt);
                            log.info("Создано новое действие пользователя {} с мероприятием {}", userId, eventId);
                        }
                );
    }

    /**
     * Обновляет существующее действие пользователя с мероприятием.
     *
     * @param existingAction старое действие, которое требуется обновить
     * @param newScore       новый вес взаимодействия
     * @param interactAt     время взаимодействия
     */
    private void updateExistingAction(UserAction existingAction, double newScore, LocalDateTime interactAt) {
        existingAction.setScore(newScore);
        existingAction.setInteractAt(interactAt);
        userActionRepository.save(existingAction);
        log.info("Обновлено действие пользователя {} с мероприятием {}: вес {}",
                existingAction.getUserId(), existingAction.getEventId(), newScore);
    }

    /**
     * Создаёт новое действие пользователя с мероприятием.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор мероприятия
     * @param newScore    вес взаимодействия
     * @param interactAt  время взаимодействия
     */
    private void saveNewAction(long userId, long eventId, double newScore, LocalDateTime interactAt) {
        UserAction newUserAction = UserAction.builder()
                .userId(userId)
                .eventId(eventId)
                .score(newScore)
                .interactAt(interactAt)
                .build();
        userActionRepository.save(newUserAction);
    }

    /**
     * Рассчитывает вес взаимодействия на основе типа действия.
     *
     * @param type тип действия (VIEW, REGISTER, LIKE и т.д.)
     * @return значение веса (от 0.0 до 1.0)
     */
    private double calcInteractionScore(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> {
                log.warn("Неизвестный тип действия: {}", type);
                yield 0.0; // По умолчанию
            }
        };
    }
}