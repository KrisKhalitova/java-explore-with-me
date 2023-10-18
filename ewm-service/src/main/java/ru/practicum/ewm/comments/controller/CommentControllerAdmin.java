package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/comments")
public class CommentControllerAdmin {
    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentAdmin(@Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Получен запрос на изменение комментария админом.");
        return commentService.updateCommentAdmin(updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Получен запрос на удаление комментария админом.");
        commentService.deleteCommentByAdmin(commentId);
    }
}