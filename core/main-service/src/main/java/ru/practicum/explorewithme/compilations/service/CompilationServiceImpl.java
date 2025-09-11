package ru.practicum.explorewithme.compilations.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilations.model.Compilation;
import ru.practicum.explorewithme.compilations.repository.CompilationRepository;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

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
            throw new NotFoundException("Подборка с ID %d не найдена".formatted(compId));
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        // Получаем подборку из БД или выбрасываем исключение, если её нет
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID %d не найдена".formatted(compId)));

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
     * @param pinned   флаг закрепления (true — только закреплённые)
     * @param pageable параметры пагинации (номер страницы, размер страницы, сортировка)
     * @return список DTO подборок
     */
    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        return (pinned != null)
                ? compilationRepository.findAllByPinned(pinned, pageable).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList())
                : compilationRepository.findAll(pageable).stream()
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
                        .orElseThrow(() -> new NotFoundException("Подборка с ID %d не найдена".formatted(compId)))
        );
    }

    /**
     * Валидирует и получает события по списку идентификаторов.
     * <p>
     * Проверяет, что все события существуют в системе.
     *
     * @param eventIds список идентификаторов событий
     * @return множество событий
     * @throws ValidationException если одно из событий не найдено
     */
    private Set<Event> validateAndFetchEvents(Set<Long> eventIds) {
        List<Event> foundEvents = eventRepository.findAllById(eventIds);

        if (foundEvents.size() != eventIds.size()) {
            Set<Long> missingIds = new HashSet<>(eventIds);
            missingIds.removeAll(foundEvents.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet()));

            throw new ValidationException("Следующие события не найдены: " + missingIds);
        }

        return new HashSet<>(foundEvents);
    }
}