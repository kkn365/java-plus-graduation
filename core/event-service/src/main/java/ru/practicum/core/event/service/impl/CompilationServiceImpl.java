package ru.practicum.core.event.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.dto.compilations.NewCompilationDto;
import ru.practicum.core.event.dto.compilations.UpdateCompilationRequest;
import ru.practicum.core.event.mapper.CompilationMapper;
import ru.practicum.core.event.model.Compilation;
import ru.practicum.core.event.repository.CompilationRepository;
import ru.practicum.core.event.model.Event;
import ru.practicum.core.event.repository.EventRepository;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.event.service.api.CompilationService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса управления подборками событий.
 * <p>
 * Класс предоставляет методы для создания, удаления, обновления и получения подборок событий,
 * а также валидации входных данных и взаимодействия с репозиторием.
 *
 * @see CompilationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private static final String COMP_NOT_FOUND_MESSAGE = "Подборка с ID=%d не найдена";
    private static final String EVENTS_NOT_FOUND_MESSAGE = "Следующие события не найдены: %s";

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    /**
     * Метод создания новой подборки событий.
     *
     * @param dto DTO с данными для создания подборки
     * @return DTO созданной подборки событий
     * @throws ValidationException если указаны несуществующие события
     */
    @Transactional
    @Override
    public CompilationDto create(NewCompilationDto dto) {
        // Получаем события, если они указаны в DTO. Если нет — устанавливаем пустое множество.
        Set<Event> events = (dto.getEvents() != null && !dto.getEvents().isEmpty())
                ? validateAndFetchEvents(dto.getEvents())
                : Collections.emptySet();

        // Строим объект подборки событий с учётом всех переданных данных
        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(events)
                .build();

        // Сохраняем подборку и возвращаем её в виде DTO
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    /**
     * Удаляет подборку событий по её идентификатору.
     *
     * @param compId Идентификатор подборки
     * @throws NotFoundException если подборка с указанным ID не найдена
     */
    @Transactional
    @Override
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format(COMP_NOT_FOUND_MESSAGE, compId));
        }
        compilationRepository.deleteById(compId);
    }

    /**
     * Обновляет данные подборки событий.
     *
     * @param compId идентификатор подборки
     * @param dto    DTO с новыми данными подборки
     * @return DTO обновлённой подборки
     * @throws NotFoundException   если подборка с указанным ID не найдена
     * @throws ValidationException если указаны несуществующие события
     */
    @Transactional
    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        // Получаем подборку из БД или выбрасываем исключение, если её нет
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(COMP_NOT_FOUND_MESSAGE, compId)));

        // Обновляем заголовок, только если он не null и не пустой
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            compilation.setTitle(dto.getTitle());
        }

        // Обновляем флаг закрепления, если он указан в запросе
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        // Обновляем события, если они переданы
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            // Валидируем, что все события существуют
            Set<Event> events = validateAndFetchEvents(dto.getEvents());
            compilation.setEvents(events);
        }

        // Сохраняем обновлённую подборку и возвращаем DTO
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    /**
     * Получает список подборок с фильтрацией по закреплению и пагинацией.
     *
     * @param pinned Флаг закрепления (true — только закреплённые)
     * @param from   Начальная позиция (смещение) для пагинации. Должно быть &gt;= 0.
     * @param size   Количество событий на странице. Должно быть &gt; 0.
     * @return список DTO подборок
     * @throws ValidationException если параметры пагинации некорректны
     */
    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        int page = from / size;

        return (pinned != null)
                ? compilationRepository.findAllByPinned(pinned, PageRequest.of(page, size)).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList())
                : compilationRepository.findAll(PageRequest.of(page, size)).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получает подборку событий по её идентификатору.
     *
     * @param compId Идентификатор подборки
     * @return DTO подборки
     * @throws NotFoundException если подборка с указанным ID не найдена
     */
    @Override
    public CompilationDto getById(Long compId) {
        return compilationMapper.toDto(
                compilationRepository.findById(compId)
                        .orElseThrow(() -> new NotFoundException(String.format(COMP_NOT_FOUND_MESSAGE, compId))));
    }

    /**
     * Валидирует и получает события по списку идентификаторов.
     * Проверяет, что все переданные события существуют в системе.
     *
     * @param eventIds список идентификаторов событий
     * @return множество событий
     * @throws ValidationException если хотя бы одно событие не найдено
     */
    private Set<Event> validateAndFetchEvents(Set<Long> eventIds) {
        // Проверяем, что список событий не пуст
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<Event> foundEvents = eventRepository.findAllById(eventIds);

        // Проверяем, что количество найденных событий совпадает с ожидаемым
        if (foundEvents.size() != eventIds.size()) {
            Set<Long> existingEventIds = foundEvents.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(eventIds);
            missingIds.removeAll(existingEventIds);

            throw new ValidationException(String.format(EVENTS_NOT_FOUND_MESSAGE,missingIds));
        }

        return new HashSet<>(foundEvents);
    }
}