package ru.practicum.explorewithme.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для представления геолокации события.
 * <p>
 * Содержит координаты широты и долготы места проведения события.
 */
@Data
@Builder
public class LocationDto {
    /**
     * Широта места события.
     * <p>
     * Обязательное поле. Должно быть числом в диапазоне от -90 до 90.
     */
    @NotNull(message = "Широта обязательна и должна быть числом")
    private Float lat;

    /**
     * Долгота места события.
     * <p>
     * Обязательное поле. Должно быть числом в диапазоне от -180 до 180.
     */
    @NotNull(message = "Долгота обязательна и должна быть числом")
    private Float lon;
}