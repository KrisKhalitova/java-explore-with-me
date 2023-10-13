package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.locations.dto.LocationDto;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.Constant.DATE_TIME_PATTERN;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private State state;

    private String title;

    private Long views;
}
