package ru.practicum.ewm.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ConfirmedRequestsDto {
    private Long count;
    private Long event;
}
