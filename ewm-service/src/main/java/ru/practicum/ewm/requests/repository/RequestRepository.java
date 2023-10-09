package ru.practicum.ewm.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.requests.dto.ConfirmedRequestsDto;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT new ru.practicum.ewm.requests.dto.ConfirmedRequestsDto(COUNT(DISTINCT r.id), r.event.id) " +
            "FROM Request AS r " +
            "WHERE r.event.id IN (:ids) AND r.status = :status " +
            "GROUP BY (r.event)")
    List<ConfirmedRequestsDto> findAllByEventIdInAndStatus(@Param("ids") List<Long> ids, @Param("status") RequestStatus status);

    List<Request> findAllByEventIdAndEventInitiatorId(Long eventId, Long userId);

    List<Request> findAllByEventIdAndEventInitiatorIdAndIdIn(Long eventId, Long userId, List<Long> requestIds);

    boolean existsByRequesterId(Long userId);
}
