package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.dto.ResponseStatsDto;
import ru.practicum.ewm.dto.StatHitDto;
import ru.practicum.ewm.dto.StatsRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@PropertySource(value = {"classpath:application.properties"})
public class StatsClient {

    private final WebClient client;

    public StatsClient(String baseUrl) {
        this.client = WebClient.create("http://localhost/9090)");
    }

    public void saveHits(HttpServletRequest request) {
        LocalDateTime localDateTime = LocalDateTime.now();
        final StatHitDto endpointHit = new StatHitDto("ewm-service", request.getRequestURI(),
                request.getRemoteAddr(), localDateTime);
        log.info("Сохранена информации о том, что был отправлен запрос пользователем.");
        this.client.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit, StatHitDto.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<List<ResponseStatsDto>> getStats(StatsRequestDto statsRequestDto, String appName) {
        log.info("Получена статистика по посещениям.");
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", statsRequestDto.getStart())
                        .queryParam("end", statsRequestDto.getEnd())
                        .queryParam("uris", statsRequestDto.getUris())
                        .queryParam("unique", statsRequestDto.getIsUnique())
                        .queryParam(appName)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ResponseStatsDto.class)
                .block();
    }
}