package ru.practicum.ewm.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Set<Event> findAllByIdIn(List<Long> events);

    @Query("select e from Event e " +
            "where (coalesce(:users, null) is null or e.initiator.id in :users) " +
            "and (coalesce(:states, null) is null or e.state in :states) " +
            "and (coalesce(:categories, null) is null or e.category.id in :categories) " +
            "and (coalesce(:rangeStart, null) is null or e.eventDate >= :rangeStart) " +
            "and (coalesce(:rangeEnd, null) is null or e.eventDate <= :rangeEnd) ")
    List<Event> findByAdmin(@Param("users") List<Long> users,
                            @Param("states") List<String> states,
                            @Param("categories") List<Long> categories,
                            @Param("rangeStart") LocalDateTime rangeStart,
                            @Param("rangeEnd") LocalDateTime rangeEnd,
                            Pageable pageable);

    boolean existsByCategoryId(Long catId);
}
