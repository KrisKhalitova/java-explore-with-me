package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsRequestDto {

    private LocalDateTime start;

    private LocalDateTime end;

    private Boolean isUnique;

    private List<String> uris;

    private String app;
}
