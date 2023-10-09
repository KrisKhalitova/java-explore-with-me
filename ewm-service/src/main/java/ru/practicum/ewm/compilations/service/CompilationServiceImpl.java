package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.mapper.CompilationMapper;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.requests.repository.RequestRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getPinned() != null) {
            compilation.setPinned(newCompilationDto.getPinned());
        } else {
            compilation.setPinned(false);
        }
        List<Long> eventsId = newCompilationDto.getEvents();
        Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventsId));
        if (eventsId != null) {
            compilation.setEvents(events);
        }
        List<EventShortDto> eventsShortDto = EventMapper.toEventShortDtoList(new ArrayList<>(events));
        Compilation savedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDtoWithEvents(savedCompilation, eventsShortDto);
    }

    @Override
    public CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updatedCompilation) {
        Compilation toUpdate = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена"));

        if (updatedCompilation.getTitle() != null && !updatedCompilation.getTitle().isBlank()) {
            toUpdate.setTitle(updatedCompilation.getTitle());
        }
        if (updatedCompilation.getPinned() != null) {
            toUpdate.setPinned(updatedCompilation.getPinned());
        }

        List<Long> eventsId = updatedCompilation.getEvents();
        if (updatedCompilation.getEvents() != null && !updatedCompilation.getEvents().isEmpty()) {
            Set<Event> events = eventRepository.findAllByIdIn(eventsId);
            toUpdate.setEvents(events);
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
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        List<Compilation> result = pinned != null
                ? compilationRepository.findByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable).getContent();
        Set<Event> events = result.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toSet());
        List<EventShortDto> eventsShortDto = EventMapper.toEventShortDtoList(new ArrayList<>(events));
        return CompilationMapper.toCompilationDtoListWithEvents(result, eventsShortDto);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена"));
        List<Long> eventsId = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
        Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventsId));
        compilation.setEvents(events);
        List<EventShortDto> eventsShortDto = EventMapper.toEventShortDtoList(new ArrayList<>(events));
        return CompilationMapper.toCompilationDtoWithEvents(compilation, eventsShortDto);
    }
}
