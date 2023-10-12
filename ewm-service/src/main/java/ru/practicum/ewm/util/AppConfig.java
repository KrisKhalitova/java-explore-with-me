package ru.practicum.ewm.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
@RequiredArgsConstructor
public class AppConfig {


    @Bean
    StatsClient statsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder restTemplate) {
        return new StatsClient(serverUrl, restTemplate);

    }
}
