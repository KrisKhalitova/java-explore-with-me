package ru.practicum.ewm.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.mapper.CategoryMapper;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addNewCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategoryFromNewCategoryDto(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category savedCategory = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена"));
        savedCategory.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(savedCategory));
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена."));
        boolean isExist = eventRepository.existsByCategoryId(catId);
        if (isExist) {
            throw new ConflictException("Категория не пуста.");
        } else {
            categoryRepository.delete(category);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена."));
        return CategoryMapper.toCategoryDto(category);
    }
}