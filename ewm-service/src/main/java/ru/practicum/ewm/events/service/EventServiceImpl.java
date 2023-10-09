package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.ewm.categories.mapper.CategoryMapper;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.categories.service.CategoryService;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.model.StateActionAdmin;
import ru.practicum.ewm.events.model.StateActionPrivate;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.locations.mapper.LocationMapper;
import ru.practicum.ewm.locations.model.Location;
import ru.practicum.ewm.locations.repository.LocationRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.requests.mapper.RequestMapper;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.requests.repository.RequestRepository;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {

    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final CategoryService categoryService;
    final LocationRepository locationRepository;
    final RequestRepository requestRepository;
    final StatsClient statsClient;
    final ObjectMapper mapper = new ObjectMapper();
    @Value("${app}")
    private String app;

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto newEventDto) {
        checkActualTime(newEventDto.getEventDate());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        Long catId = newEventDto.getCategory();
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена."));
        Location location = checkLocation(LocationMapper.toLocation(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        return EventMapper.toEventFullDto(getEvent(eventId, userId),
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEvent(eventId, userId);
        if (event.getState() == PUBLISHED) {
            throw new ConflictException("Опубликованное событие не может быть изменено.");
        }
        String annotation = updateEvent.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(updateEvent.getCategory())));
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
            Location location = checkLocation(LocationMapper.toLocation(updateEvent.getLocation()));
            event.setLocation(location);
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
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
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
        String annotation = updateEvent.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(updateEvent.getCategory())));
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
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = getConfirmedRequests(ids);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                         Integer size, HttpServletRequest request) {
        List<EventShortDto> list = new ArrayList<>();
        return list;
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
    public List<EventFullDto> getEventsByAdminParams(List<Long> users, List<String> states, List<Long> categories,
                                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                                     Integer size) {
        List<EventFullDto> list = new ArrayList<>();
        return list;
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

    private Map<Long, Long> getConfirmedRequests(List<Long> ids) {
        return requestRepository.findAllByEventIdInAndStatus(ids, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));
    }

    private List<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        StatsRequestDto statsRequestDto = StatsRequestDto.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .isUnique(unique)
                .build();
        ResponseEntity<List<ResponseStatsDto>> response = statsClient.getStats(statsRequestDto, app);
        try {
            return Arrays.asList(mapper.readValue(mapper.writeValueAsString(response.getBody()), ResponseStatsDto[].class));
        } catch (JsonProcessingException e) {
            throw new ValidationException(e.getMessage());
        }
    }
}
