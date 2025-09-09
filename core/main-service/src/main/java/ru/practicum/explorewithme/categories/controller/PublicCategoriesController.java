package ru.practicum.explorewithme.categories.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.service.CategoryService;

import java.util.Collection;

import static ru.practicum.explorewithme.util.Constants.DEFAULT_FROM_VALUE;
import static ru.practicum.explorewithme.util.Constants.DEFAULT_SIZE_VALUE;

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicCategoriesController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Collection<CategoryDto>> getCategories(
            @RequestParam(defaultValue = DEFAULT_FROM_VALUE) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = DEFAULT_SIZE_VALUE) @Positive Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.getCategories(pageable));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.getCategory(categoryId));
    }
}
