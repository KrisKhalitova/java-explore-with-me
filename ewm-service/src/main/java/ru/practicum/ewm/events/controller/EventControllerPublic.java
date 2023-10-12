package ru.practicum.ewm.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.ParamsPublicEventDto;
import ru.practicum.ewm.events.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid @ModelAttribute ParamsPublicEventDto paramsPublicEventDto,
                                         @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Positive Integer size,
                                         HttpServletRequest request) {
        if (paramsPublicEventDto.getOnlyAvailable() == null) {
            paramsPublicEventDto.setOnlyAvailable(false);
        }
        return eventService.getEvents(paramsPublicEventDto, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByPublic(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventByPublic(eventId, request);
    }
}