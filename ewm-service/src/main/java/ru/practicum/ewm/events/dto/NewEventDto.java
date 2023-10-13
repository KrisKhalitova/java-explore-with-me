package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.locations.dto.LocationDto;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

import static ru.practicum.ewm.util.Constant.DATE_TIME_PATTERN;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank
    private String annotation;

    @NotNull
    private Long category;

    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;

    @NotNull
    @Future
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    private LocationDto location;

    private boolean paid = false;

    @PositiveOrZero
    private int participantLimit = 0;

    private boolean requestModeration = true;

    @Size(min = 3, max = 120)
    @NotBlank
    private String title;

    public Boolean getPaid() {
        return paid;
    }

    public Boolean getRequestModeration() {
        return requestModeration;
    }
}
