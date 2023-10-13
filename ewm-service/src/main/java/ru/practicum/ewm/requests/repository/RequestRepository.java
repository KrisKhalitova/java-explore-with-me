package ru.practicum.ewm.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.events.dto.RequestStats;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventIdAndEventInitiatorId(Long eventId, Long userId);

    List<Request> findAllByEventIdAndEventInitiatorIdAndIdIn(Long eventId, Long userId, List<Long> requestIds);

    boolean existsByRequesterId(Long userId);

    @Query("SELECT new ru.practicum.ewm.events.dto.RequestStats(r.event.id, count(r.id)) " +
            "FROM Request AS r " +
            "WHERE r.event.id IN ?1 " +
            "AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<RequestStats> findConfirmedRequests(List<Long> eventsId);
}
