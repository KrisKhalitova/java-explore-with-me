package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsRequestDto {

    private String start;

    private String end;

    private Boolean isUnique;

    private String[] uris;
}
