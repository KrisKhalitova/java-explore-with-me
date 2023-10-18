package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable(value = "userId") Long userId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Получен запрос на добавление комментария.");
        return commentService.addComment(userId, newCommentDto);
    }


    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable(value = "userId") Long userId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Получен запрос на обновление комментария пользователем.");
        return commentService.updateComment(userId, updateCommentDto);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable(value = "userId") Long userId,
                                     @PathVariable(value = "commentId") Long commentId) {
        log.info("Получен запрос на получение комментария пользователя по id.");
        return commentService.getCommentByIdPrivate(userId, commentId);
    }

    @GetMapping("/comments")
    List<CommentDto> getCommentsByAuthor(@PathVariable(value = "userId") Long userId,
                                         @RequestParam(defaultValue = "0")
                                         @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10")
                                         @Positive Integer size) {
        log.info("Получен запрос на получение комментариев автора.");
        return commentService.getCommentsByAuthor(userId, from, size);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "userId") Long userId,
                              @PathVariable(value = "commentId") Long commentId) {
        log.info("Получен запрос на удаление пользователем своего комментария.");
        commentService.deleteComment(userId, commentId);
    }
}