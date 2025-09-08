package ru.practicum.explorewithme.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.dto.NewCategoryDto;
import ru.practicum.explorewithme.categories.service.CategoryService;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminCategoriesController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(newCategoryDto));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long categoryId,
                                              @RequestBody @Valid CategoryDto categoryDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.updateCategory(categoryId, categoryDto));
    }
}