package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.StatHitDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatMapper;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Override
    public void saveStatHit(StatHitDto statHitDto) {
        statRepository.save(StatMapper.statHitDtoToStatHit(statHitDto));
    }

    @Override
    public Collection<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            log.info("End time can't be before start time");
            throw new ValidationException("End time can't be before start time");
        }

        if (uris.isEmpty()) {
            if (unique) {
                log.info("Get all stats with isUnique {} ", unique);
                return statRepository.getStatsByUniqueIp(start, end);
            } else {
                log.info("Get all stats with isUnique {} ", unique);
                return statRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                log.info("Get all stats with isUnique {} when uris {} ", unique, uris);
                return statRepository.getStatsByUrisByUniqueIp(start, end, uris);
            } else {
                log.info("Get all stats with isUnique {} when uris {} ", unique, uris);
                return statRepository.getAllStatsByUris(start, end, uris);
            }
        }
    }
}
