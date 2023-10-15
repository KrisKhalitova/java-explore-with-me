package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;
import ru.practicum.ewm.util.EwmPageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.comments.mapper.CommentMapper.*;
import static ru.practicum.ewm.events.model.State.PUBLISHED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        CommentDto commentDto = mapToCommentDto(newCommentDto);
        Event event = findEventById(eventId);
        User user = findUserById(userId);

        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidationException("Событие еще не опубликовано.");
        }
        Comment comment = commentRepository.save(toComment(user, event, commentDto));

        return toCommentDto(comment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToCommentDto(newCommentDto);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User author = findUserById(userId);
        Comment comment = findCommentById(commentId);
        if (comment.getAuthor() != author) {
            throw new ValidationException("Только автор комментария может его удалить.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size) {
        return commentRepository.findAllByAuthorId(userId, new EwmPageRequest(from, size, Sort.unsorted()))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        boolean isExist = commentRepository.existsById(commentId);

        if (isExist) {
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Комментарий не найден.");
        }
    }

    @Override
    public List<CommentDto> getComments(Long eventId, Integer from, Integer size) {
        findEventById(eventId);
        Pageable pageable = new EwmPageRequest(from, size, Sort.unsorted());
        return commentRepository.findAllByEventId(eventId, pageable)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = findCommentById(commentId);
        return toCommentDto(comment);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ValidationException("Комментарий не найден."));
    }
}
