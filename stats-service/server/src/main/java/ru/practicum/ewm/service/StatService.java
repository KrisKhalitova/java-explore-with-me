package ru.practicum.ewm.service;


import ru.practicum.ewm.dto.ResponseStatsDto;
import ru.practicum.ewm.dto.StatHitDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatService {
    void saveStatHit(StatHitDto statHitDto);

    Collection<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
