package ru.practicum.ewm.requests.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.requests.model.Request;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus())
                .build();
    }

    public List<RequestDto> toRequestDtoList(List<Request> requests) {
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .requester(request.getRequester().getId())
                        .event(request.getEvent().getId())
                        .status(request.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
