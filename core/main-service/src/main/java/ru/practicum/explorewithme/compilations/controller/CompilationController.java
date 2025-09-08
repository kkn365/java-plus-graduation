package ru.practicum.explorewithme.compilations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.service.CompilationService;

import java.util.List;

import static ru.practicum.explorewithme.util.Constants.DEFAULT_FROM_VALUE;
import static ru.practicum.explorewithme.util.Constants.DEFAULT_SIZE_VALUE;

@RestController
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.create(newCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        compilationService.delete(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto update(@PathVariable Long compId,
                                 @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        return compilationService.update(compId, updateRequest);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                       @RequestParam(defaultValue = DEFAULT_FROM_VALUE) int from,
                                       @RequestParam(defaultValue = DEFAULT_SIZE_VALUE) int size) {
        return compilationService.getAll(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {
        return compilationService.getById(compId);
    }
}