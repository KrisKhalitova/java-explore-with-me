package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getComments(Long eventId, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);
}
