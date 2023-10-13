package ru.practicum.ewm.requests.service;

import ru.practicum.ewm.requests.dto.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto addRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequestsByUser(Long userId);
}
