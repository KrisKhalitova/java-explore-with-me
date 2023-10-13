package ru.practicum.ewm.requests.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequestStatusUpdateResult {
    private List<RequestDto> confirmedRequests;

    private List<RequestDto> rejectedRequests;
}
