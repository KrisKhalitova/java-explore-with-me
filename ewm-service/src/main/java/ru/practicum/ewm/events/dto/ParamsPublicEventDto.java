package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.Constant.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
public class ParamsPublicEventDto {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable;
    private String sort;
}
