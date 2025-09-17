package ru.practicum.recomm.aggregator.model;

/**
 * Модель для хранения информации о схожести двух событий.
 * <p>
 * Представляет собой пару событий (sourceEventId, targetEventId) и значение их сходства (similarityScore).
 *
 * @param sourceEventId   Идентификатор первого события в паре.
 *                        Обычно используется как исходное событие для сравнения.
 * @param targetEventId   Идентификатор второго события в паре.
 *                        Сравнивается с первым событием.
 * @param similarityScore Степень сходства между событиями.
 *                        Значение находится в диапазоне [0.0, 1.0], где:
 *                        - 0.0 означает полное отсутствие сходства,
 *                        - 1.0 означает полное совпадение.
 */
public record EventSimilarity(Long sourceEventId, Long targetEventId, Double similarityScore) {
}