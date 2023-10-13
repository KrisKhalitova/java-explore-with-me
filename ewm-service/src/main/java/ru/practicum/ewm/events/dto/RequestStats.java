package ru.practicum.ewm.events.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestStats {

    private Long eventId;
    private Long confirmedRequests;
}
