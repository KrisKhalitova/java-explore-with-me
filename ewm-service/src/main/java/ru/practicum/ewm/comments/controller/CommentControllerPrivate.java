package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable(value = "userId") Long userId,
                                 @RequestParam(value = "eventId") Long eventId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.addComment(userId, eventId, newCommentDto);
    }


    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.updateComment(userId, commentId, newCommentDto);
    }

    @GetMapping("/comments")
    List<CommentDto> getCommentsByAuthor(@PathVariable(value = "userId") Long userId,
                                         @RequestParam(defaultValue = "0")
                                         @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10")
                                         @Positive Integer size) {
        return commentService.getCommentsByAuthor(userId, from, size);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "userId") Long userId,
                              @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteComment(userId, commentId);
    }
}