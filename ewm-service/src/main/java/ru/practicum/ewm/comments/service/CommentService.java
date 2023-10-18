package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getComments(Long eventId, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);

    CommentDto updateComment(Long userId, UpdateCommentDto updateCommentDto);

    CommentDto updateCommentAdmin(UpdateCommentDto updateCommentDto);

    CommentDto getCommentByIdPrivate(Long userId, Long commentId);
}
