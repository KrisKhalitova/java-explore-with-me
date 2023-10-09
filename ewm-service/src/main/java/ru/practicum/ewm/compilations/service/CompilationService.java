package ru.practicum.ewm.compilations.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addNewCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updateCompilation);

    void deleteCompilation(Long compId);

    CompilationDto getCompilationById(Long compilationId);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);
}
