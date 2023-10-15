package ru.practicum.ewm.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.util.EwmPageRequest;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long userId);

    Page<Comment> findAllByAuthorId(Long userId, EwmPageRequest ewmPageRequest);
}
