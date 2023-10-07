package ru.practicum.ewm.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
public class AppConfig {

    @Bean
    StatsClient statsClient() {
        return new StatsClient("http://localhost/9090)");
    }
}
