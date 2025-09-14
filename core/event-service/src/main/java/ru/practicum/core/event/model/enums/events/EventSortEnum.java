package ru.practicum.core.event.model.enums.events;

/**
 * Перечисление для определения критериев сортировки событий.
 * <p>
 * Используется в методах получения событий для указания порядка сортировки результата.
 */
public enum EventSortEnum {
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

    private final String sortField;

    EventSortEnum(String sortField) {
        this.sortField = sortField;
    }

    /**
     * Возвращает строковое представление поля для сортировки.
     *
     * @return имя поля, используемое в запросах к БД или API
     */
    public String getSortField() {
        return sortField;
    }
}