package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.dto.ResponseStatsDto;
import ru.practicum.ewm.model.StatHit;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatRepository extends JpaRepository<StatHit, Integer> {

    @Query("select new ru.practicum.dto.ResponseStatsDto(s.app, s.uri, count(distinct s.ip))" +
            "from StatHit as s " +
            "where s.timestamp between :start and :end " +
            "group by s.app, s.uri " +
            "order by count(distinct(s.ip)) desc")
    Collection<ResponseStatsDto> getStatsByUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ResponseStatsDto(s.app, s.uri, count(s.ip))" +
            "from StatHit as s " +
            "where s.timestamp between :start and :end " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    Collection<ResponseStatsDto> getAllStats(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ResponseStatsDto(s.app, s.uri, count(distinct s.ip))" +
            "from StatHit as s " +
            "where s.timestamp between :start and :end " +
            "and s.uri in :uris " +
            "group by s.app, s.uri " +
            "order by count(distinct(s.ip)) desc")
    Collection<ResponseStatsDto> getStatsByUrisByUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.dto.ResponseStatsDto(s.app, s.uri, count(s.ip))" +
            "from StatHit as s " +
            "where s.timestamp between :start and :end " +
            "and s.uri in :uris " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    Collection<ResponseStatsDto> getAllStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
