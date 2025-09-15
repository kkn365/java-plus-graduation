package ru.practicum.core.event.model.enums.events;

import lombok.Getter;

/**
 * Перечисление для определения критериев сортировки событий.
 * <p>
 * Используется в методах получения событий для указания порядка сортировки результата.
 */
@Getter
public enum EventSort {
    /**
     * Сортировка по дате события (по возрастанию).
     * <p>
     * Соответствует полю event_date в модели данных.
     */
    EVENT_DATE("eventDate"),

    /**
     * Сортировка по количеству просмотров (по убыванию).
     * <p>
     * Соответствует статистике просмотров из внешнего сервиса.
     */
    VIEWS("views");

    /**
     * Поле, которое будет использоваться как имя поля для сортировки.
     */
    private final String sortField;

    /**
     * Конструктор перечисления.
     *
     * @param sortField строковое представление имени поля для сортировки
     */
    EventSort(String sortField) {
        this.sortField = sortField;
    }
}