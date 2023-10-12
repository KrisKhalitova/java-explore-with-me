package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.model.StateActionAdmin;
import ru.practicum.ewm.events.model.StateActionPrivate;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ResponseException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.locations.mapper.LocationMapper;
import ru.practicum.ewm.locations.model.Location;
import ru.practicum.ewm.locations.repository.LocationRepository;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.requests.mapper.RequestMapper;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.requests.repository.RequestRepository;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;
import ru.practicum.ewm.util.EwmPageRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.model.State.PUBLISHED;
import static ru.practicum.ewm.events.model.StateActionPrivate.CANCEL_REVIEW;
import static ru.practicum.ewm.events.model.StateActionPrivate.SEND_TO_REVIEW;
import static ru.practicum.ewm.requests.model.RequestStatus.CONFIRMED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto newEventDto) {
        checkActualTime(newEventDto.getEventDate());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        Long catId = newEventDto.getCategory();
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена."));
        Location location = checkLocation(LocationMapper.toLocation(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto, user, category, location);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        return EventMapper.toEventFullDto(getEvent(eventId, userId),
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventRequest updateEvent) {
        Event event = getEvent(eventId, userId);
        if (event.getState() == PUBLISHED) {
            throw new ConflictException("Опубликованное событие не может быть изменено.");
        }
        updateParams(event, updateEvent);
        if (updateEvent.getStateAction() != null) {
            StateActionPrivate stateActionPrivate = StateActionPrivate.valueOf(updateEvent.getStateAction());
            if (stateActionPrivate.equals(SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            } else if (stateActionPrivate.equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEvent) {
        Event event = getEvent(eventId);
        if (updateEvent.getStateAction() != null) {
            StateActionAdmin stateAction = StateActionAdmin.valueOf(updateEvent.getStateAction());
            if (!event.getState().equals(State.PENDING) && stateAction.equals(StateActionAdmin.PUBLISH_EVENT)) {
                throw new ConflictException("Событие не может быть опубликовано.");
            }
            if (event.getState().equals(PUBLISHED) && stateAction.equals(StateActionAdmin.REJECT_EVENT)) {
                throw new ConflictException("Событие не может быть отклонено, т.к. оно уже опубликовано.");
            }
            if (stateAction.equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (stateAction.equals(StateActionAdmin.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        updateParams(event, updateEvent);
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано.");
        }
        statsClient.saveHits(request);
        return toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        Map<Long, Long> confirmedRequests = getConfirmedRequestsByEvents(events);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEvents(ParamsPublicEventDto params, Integer from, Integer size,
                                         HttpServletRequest request) {
        checkStartIsBeforeEnd(params.getRangeStart(), params.getRangeEnd());
        PageRequest pageable = new EwmPageRequest(from, size, Sort.unsorted());
        List<Event> events = eventRepository.findAllByPublic(params.getText(), params.getCategories(),
                params.getPaid(), params.getRangeStart(), params.getRangeEnd(), params.getOnlyAvailable(), pageable);
        statsClient.saveHits(request);
        return toEventsShortDto(events);
    }

    @Override
    public List<EventShortDto> toEventsShortDto(List<Event> events) {
        Map<Long, Long> views = getViews(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsByEvents(events);
        return events.stream()
                .map((event) -> EventMapper.toEventShortDtoWithConfirmedRequestsAndViews(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED);

        long freePlaces = event.getParticipantLimit() - confirmedRequests;

        RequestStatus status = RequestStatus.valueOf(String.valueOf(request.getStatus()));
        if (status.equals(RequestStatus.CONFIRMED) && freePlaces <= 0) {
            throw new ConflictException("Лимит запросов к участию исчерпан.");
        }
        List<Request> requests = requestRepository.findAllByEventIdAndEventInitiatorIdAndIdIn(eventId,
                userId, request.getRequestIds());
        setStatus(requests, status, freePlaces);

        List<RequestDto> requestsDto = requests
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());

        List<RequestDto> confirmedRequestsDto = new ArrayList<>();
        List<RequestDto> rejectedRequestsDto = new ArrayList<>();

        requestsDto.forEach(el -> {
            if (status.equals(RequestStatus.CONFIRMED)) {
                confirmedRequestsDto.add(el);
            } else {
                rejectedRequestsDto.add(el);
            }
        });

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequestsDto)
                .rejectedRequests(rejectedRequestsDto)
                .build();
    }

    @Override
    public List<RequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));
        return requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getEventsByAdminParams(ParamsAdminEventDto params, Integer from, Integer size) {
        validDateParam(params.getRangeStart(), params.getRangeEnd());
        PageRequest pageable = new EwmPageRequest(from, size, Sort.unsorted());
        List<Event> events = eventRepository.findAllForAdmin(params.getUsers(), params.getStates(),
                params.getCategories(), getRangeStart(params.getRangeStart()), pageable);
        Map<Long, Long> confReqs = getConfirmedRequestsByEvents(events);
        return events.stream()
                .map(event -> EventMapper.toEventFullDto(event, confReqs.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private EventFullDto toEventFullDto(Event event) {
        return toEventsFullDto(List.of(event)).get(0);
    }

    private List<EventFullDto> toEventsFullDto(List<Event> events) {
        Map<Long, Long> views = getViews(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsByEvents(events);
        return events.stream()
                .map((event) -> EventMapper.toEventFullDtoWithAll(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public Map<Long, Long> getViews(List<Event> events) {
        Map<Long, Long> views = new HashMap<>();
        List<Event> publishedEvents = getPublished(events);
        if (events.isEmpty()) {
            return views;
        }

        Optional<LocalDateTime> minPublishedOn = getMinPublishedOn(publishedEvents);
        if (minPublishedOn.isPresent()) {
            LocalDateTime start = minPublishedOn.get();
            LocalDateTime end = LocalDateTime.now();
            String[] uris = getURIs(publishedEvents);
            List<ResponseStatsDto> stats = getStats(start, end, uris, true);
            putStats(views, stats);
        }
        return views;
    }

    private Optional<LocalDateTime> getMinPublishedOn(List<Event> publishedEvents) {
        return publishedEvents.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
    }

    private void putStats(Map<Long, Long> views, List<ResponseStatsDto> stats) {
        stats.forEach(stat -> {
            Long eventId = Long.parseLong(stat.getUri()
                    .split("/", 0)[2]);
            views.put(eventId, views.getOrDefault(eventId, 0L) + stat.getHits());
        });
    }

    private List<Event> getPublished(List<Event> events) {
        return events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toList());
    }

    private String[] getURIs(List<Event> publishedEvents) {
        return publishedEvents.stream()
                .map(Event::getId)
                .map(id -> ("/events/" + id))
                .toArray(String[]::new);
    }

    public Map<Long, Long> getConfirmedRequestsByEvents(List<Event> events) {
        List<Long> eventsId = getPublished(events).stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        Map<Long, Long> requestStats = new HashMap<>();
        if (!eventsId.isEmpty()) {
            requestRepository.findConfirmedRequests(eventsId)
                    .forEach(stat -> requestStats.put(stat.getEventId(), stat.getConfirmedRequests()));
        }
        return requestStats;
    }

    private void checkStartIsBeforeEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ResponseException(HttpStatus.BAD_REQUEST, "INVALID_DATES");
        }
    }

    private void validDateParam(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("Дата начала не может быть указана после даты окончания");
            }
        }
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }

    private void setStatus(Collection<Request> requests, RequestStatus status, long freePlaces) {
        if (status.equals(RequestStatus.CONFIRMED)) {
            for (Request request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Статус запроса должен быть ожидание");
                }
                if (freePlaces > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    freePlaces--;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                }
            }
        } else if (status.equals(RequestStatus.REJECTED)) {
            requests.forEach(request -> {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ConflictException("Статус запроса должен быть ожидание");
                }
                request.setStatus(RequestStatus.REJECTED);
            });
        } else {
            throw new ConflictException("Вы должны либо одобрить - ПОДТВЕРЖДЕНО или отклонить - ОТКЛОНЕНА заявка");
        }
    }

    private void checkActualTime(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Некорректный запрос");
        }
    }

    private Location checkLocation(Location location) {
        if (locationRepository.existsByLatAndLon(location.getLat(), location.getLon())) {
            return locationRepository.findByLatAndLon(location.getLat(), location.getLon());
        } else {
            return locationRepository.save(location);
        }
    }

    private Event getEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
    }

    private List<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        StatsRequestDto statsRequestDto = StatsRequestDto.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .isUnique(unique)
                .build();
        ResponseEntity<Object> response = statsClient.getStats(statsRequestDto);
        try {
            return Arrays.asList(mapper.readValue(mapper.writeValueAsString(response.getBody()), ResponseStatsDto[].class));
        } catch (JsonProcessingException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void updateParams(Event event, UpdateEventRequest updateEvent) {
        String annotation = updateEvent.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            Long categoryId = updateEvent.getCategory();
            Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                    new NotFoundException("Категория не найдена."));
            event.setCategory(category);
        }
        String description = updateEvent.getDescription();
        if (description != null && !description.isBlank()) {
            event.setDescription(description);
        }
        LocalDateTime eventDate = updateEvent.getEventDate();
        if (eventDate != null) {
            checkActualTime(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(checkLocation(LocationMapper.toLocation(updateEvent.getLocation())));
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        String title = updateEvent.getTitle();
        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
    }
}
