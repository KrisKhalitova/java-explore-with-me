package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import ru.practicum.ewm.exceptions.ResponseException;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
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
        List<Event> events = getEventsFromNewCompilationDto(newCompilationDto);
        if (eventsId != null) {
            compilation.setEvents(events);
        }
        List<EventShortDto> eventsShortDto = EventMapper.toEventShortDtoList(new ArrayList<>(events));
        Compilation savedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDtoWithEvents(savedCompilation, eventsShortDto);
    }

    private List<Event> getEventsFromNewCompilationDto(NewCompilationDto newCompilationDto) {
        if (!newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            checkSize(events, newCompilationDto.getEvents());
            return events;
        }
        return Collections.EMPTY_LIST;
    }

    private void checkSize(List<Event> events, List<Long> eventsIdToUpdate) {
        if (events.size() != eventsIdToUpdate.size()) {
            throw new ResponseException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        }
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
            List<Event> events = eventRepository.findAllByIdIn(eventsId);
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
        List<Compilation> compilations = pinned != null
                ? compilationRepository.findByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable).getContent();
        Map<Long, EventShortDto> eventsShortDto = getEventsShortDto(compilations);
        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> compEventsShortDto = new ArrayList<>();
                    compilation.getEvents().forEach(event -> compEventsShortDto.add(eventsShortDto.get(event.getId())));
                    return CompilationMapper.toCompilationDtoWithEvents(compilation, compEventsShortDto);
                }).collect(Collectors.toList());
    }

    private Map<Long, EventShortDto> getEventsShortDto(List<Compilation> compilations) {
        Set<Event> uniqueEvents = new HashSet<>();
        compilations.forEach(compilation -> uniqueEvents.addAll(compilation.getEvents()));
        Map<Long, EventShortDto> eventsShortDto = new HashMap<>();
        eventService.toEventsShortDto(new ArrayList<>(uniqueEvents)).forEach(event -> eventsShortDto.put(event.getId(), event));
        return eventsShortDto;
    }


    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена"));
        List<Long> eventsId = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
        List<Event> events = eventRepository.findAllByIdIn(eventsId);
        compilation.setEvents(events);
        List<EventShortDto> eventsShortDto = EventMapper.toEventShortDtoList(new ArrayList<>(events));
        return CompilationMapper.toCompilationDtoWithEvents(compilation, eventsShortDto);
    }
}
