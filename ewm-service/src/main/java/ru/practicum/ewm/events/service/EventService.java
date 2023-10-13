package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    EventFullDto addNewEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size);

    EventFullDto getEventByOwner(Long userId, Long eventId);

    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventRequest updateEvent);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventAdminRequest);

    EventFullDto getEventByPublic(Long eventId, HttpServletRequest request);

    EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest request);

    List<RequestDto> getRequestsByEventOwner(Long userId, Long eventId);

    List<EventFullDto> getEventsByAdminParams(ParamsAdminEventDto paramsAdminEventDto, Integer from,
                                              Integer size);

    List<EventShortDto> getEvents(ParamsPublicEventDto paramsPublicEventDto, Integer from,
                                  Integer size, HttpServletRequest request);

    List<EventShortDto> toEventsShortDto(List<Event> events);
}
