package ru.practicum.explorewithme.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        Set<Event> events = dto.getEvents() != null
                ? new HashSet<>(eventRepository.findAllById(dto.getEvents()))
                : Collections.emptySet();

        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(events)
                .build();

        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            compilation.setEvents(events);
        }

        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);

        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, page).stream()
                    .map(compilationMapper::toDto)
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAll(page).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(Long compId) {
        return compilationMapper.toDto(
                compilationRepository.findById(compId)
                        .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId))
        );
    }
}