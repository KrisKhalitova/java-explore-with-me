package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ResponseStatsDto;
import ru.practicum.ewm.dto.StatHitDto;
import ru.practicum.ewm.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatController {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatService service;

    @PostMapping("/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void saveStatHit(@RequestBody @Valid StatHitDto statHitDto) {
        log.info("Сохранена информации о том, что был отправлен запрос пользователем. {}", statHitDto);
        service.saveStatHit(statHitDto);
    }

    @GetMapping("/stats")
    public Collection<ResponseStatsDto> getStats(
            @RequestParam(value = "start") @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime end,
            @RequestParam(value = "uris", defaultValue = "") List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") Boolean unique
    ) {
        log.info("Получена статистика по посещениям.", start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }
}
