package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.service.CommentService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentControllerAdmin {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}