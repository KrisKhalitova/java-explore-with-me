package ru.practicum.ewm.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto addNewEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addNewEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        return eventService.updateEventByOwner(userId, eventId, updateEvent);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
        return eventService.updateRequestsStatus(userId, eventId, request);
    }

    @GetMapping
    public List<EventShortDto> getEventsByOwner(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        return eventService.getEventsByOwner(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByOwner(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequestsByEventOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getRequestsByEventOwner(userId, eventId);
    }
}