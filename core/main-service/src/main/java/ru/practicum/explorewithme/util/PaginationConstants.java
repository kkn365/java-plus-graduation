package ru.practicum.explorewithme.util;

/**
 * Константы для параметров пагинации.
 * <p>
 * Определяет значения по умолчанию для параметров 'from' и 'size'.
 */
public class PaginationConstants {

    /**
     * Значение по умолчанию для параметра 'from' (смещение).
     * <p>
     * Обозначает начальную позицию выборки данных.
     */
    public static final String DEFAULT_FROM = "0";

    /**
     * Значение по умолчанию для параметра 'size' (количество элементов на странице).
     * <p>
     * Минимальное значение — 1, максимальное — зависит от реализации.
     */
    public static final String DEFAULT_SIZE = "10";
}