package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addNewEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size);

    EventFullDto getEventByOwner(Long userId, Long eventId);

    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto getEventByPublic(Long eventId, HttpServletRequest request);

    EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest request);

    List<RequestDto> getRequestsByEventOwner(Long userId, Long eventId);

    List<EventFullDto> getEventsByAdminParams(List<Long> users, List<State> states, List<Long> categories,
                                              LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                              Integer size);

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                  Integer size, HttpServletRequest request);
}
