package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.mapper.CompilationMapper;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.requests.repository.RequestRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        return  CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updatedCompilation) {
        Compilation toUpdate = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException(String.format("Подборка событий не найдена")));

        if (updatedCompilation.getTitle() != null && !updatedCompilation.getTitle().isBlank()) {
            toUpdate.setTitle(updatedCompilation.getTitle());
        }
        if (updatedCompilation.getPinned() != null) {
            toUpdate.setPinned(updatedCompilation.getPinned());
        }

        if (updatedCompilation.getEvents() != null && !updatedCompilation.getEvents().isEmpty()) {
            List<Long> eventsId = updatedCompilation.getEvents();
            Collection<Event> events = eventRepository.findAllByIdIn(eventsId);
            toUpdate.setEvents(new HashSet<>(events));
        }
        Compilation updated = compilationRepository.save(toUpdate);
        return CompilationMapper.toCompilationDto(updated);
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена"));
        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        return null;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId);
        return CompilationMapper.toCompilationDto(compilation);
    }

    private Compilation getCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена"));
    }
}
