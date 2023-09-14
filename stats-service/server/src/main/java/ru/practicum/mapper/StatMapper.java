package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.StatHitDto;
import ru.practicum.model.StatHit;

@UtilityClass
public class StatMapper {
    public static StatHit statHitDtoToStatHit(StatHitDto statsHitDto) {
        return StatHit.builder()
                .app(statsHitDto.getApp())
                .uri(statsHitDto.getUri())
                .ip(statsHitDto.getIp())
                .timestamp(statsHitDto.getTimestamp())
                .build();
    }
}
