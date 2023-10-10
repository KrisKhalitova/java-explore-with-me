package ru.practicum.ewm.compilations.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NewCompilationDto {

    private List<Long> events = new ArrayList<>();

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned = false;
}
