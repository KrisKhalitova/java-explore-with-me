package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.StatHitDto;
import ru.practicum.dto.StatsRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> saveHits(HttpServletRequest request) {
        LocalDateTime localDateTime = LocalDateTime.now();
        final StatHitDto endpointHit = new StatHitDto("ewm-service", request.getRequestURI(),
                request.getRemoteAddr(), localDateTime);
        return post("/hit", endpointHit);
    }

    public ResponseEntity<Object> getStats(StatsRequestDto statsRequestDto) {
        return getStats(statsRequestDto.getStart(), statsRequestDto.getEnd(), statsRequestDto.getUris(),
                statsRequestDto.getIsUnique());
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        List<String> urList = new ArrayList<>();
        Collections.addAll(urList, uris);
        String path = getStatsPath(urList);
        Map<String, Object> parameters = getStatsParameters(start, end, uris, unique);
        return get(path, parameters);
    }

    private String getStatsPath(List<String> uris) {
        if (uris == null) {
            return "/stats?start={start}&end={end}&unique={unique}";
        } else {
            return "/stats?start={start}&end={end}&unique={unique}&uris={uris}";
        }
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private Map<String, Object> getStatsParameters(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        if (uris == null) {
            return Map.of(
                    "start", start.format(DATE_FORMATTER),
                    "end", end.format(DATE_FORMATTER),
                    "unique", unique
            );
        } else {
            return Map.of(
                    "start", start.format(DATE_FORMATTER),
                    "end", end.format(DATE_FORMATTER),
                    "unique", unique,
                    "uris", String.join(", ", uris)
            );
        }
    }
}
