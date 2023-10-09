package ru.practicum.ewm.compilations.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.events.dto.EventShortDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        return new Compilation(
                newCompilationDto.getTitle(),
                newCompilationDto.getPinned()
        );
    }

    public CompilationDto toCompilationDtoWithEvents(Compilation compilation, List<EventShortDto> eventsShortDto) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventsShortDto)
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .build();
    }

    public List<CompilationDto> toCompilationDtoListWithEvents(List<Compilation> compilations, List<EventShortDto> eventsShortDto) {
        return compilations.stream()
                .map(compilation-> CompilationDto.builder()
                        .id(compilation.getId())
                        .pinned(compilation.getPinned())
                        .title(compilation.getTitle())
                        .events(eventsShortDto)
                        .build())
                .collect(Collectors.toList());
    }
}
