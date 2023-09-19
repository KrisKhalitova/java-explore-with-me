package ru.practicum.service;


import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.StatHitDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatService {
    void saveStatHit(StatHitDto statHitDto);

    Collection<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
