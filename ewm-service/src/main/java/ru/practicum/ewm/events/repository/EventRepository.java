package ru.practicum.ewm.events.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Set<Event> findAllByIdIn(List<Long> events);

    boolean existsByCategoryId(Long catId);

    Page<Event> findAll(Specification<Event> specification, Pageable pageable);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "WHERE e.eventDate > :rangeStart " +
            "AND (e.category.id IN :categories OR :categories IS NULL) " +
            "AND (e.initiator.id IN :users OR :users IS NULL) " +
            "AND (e.state IN :states OR :states IS NULL)"
    )
    List<Event> findAllForAdmin(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, PageRequest pageable);
}

