package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.StatHitDto;
import ru.practicum.dto.StatsRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@PropertySource(value = {"classpath:application.properties"})
public class StatsClient {

    @Value("${stats.server.url}")
    private String baseUrl;
    private final WebClient client;

    public StatsClient() {
        this.client = WebClient.create(baseUrl);
    }

    public void saveHits(String app, String uri, String ip, LocalDateTime timestamp) {
        final StatHitDto endpointHit = new StatHitDto(app, uri, ip, timestamp);
        log.info("Сохранена информации о том, что был отправлен запрос пользователем.", endpointHit);
        this.client.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit, StatHitDto.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<List<ResponseStatsDto>> getStats(StatsRequestDto statsRequestDto) {
        log.info("Получена статистика по посещениям.");
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", statsRequestDto.getStart())
                        .queryParam("end", statsRequestDto.getEnd())
                        .queryParam("uris", statsRequestDto.getUris())
                        .queryParam("unique", statsRequestDto.getIsUnique())
                        .queryParam("app", statsRequestDto.getApp())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ResponseStatsDto.class)
                .block();
    }
}