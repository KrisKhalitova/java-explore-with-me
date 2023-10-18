package ru.practicum.ewm.comments.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.users.model.User;

@UtilityClass
public class CommentMapper {
    public static Comment toComment(User user, Event event, CommentDto dto) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setEvent(event);
        comment.setAuthor(user);
        return comment;
    }

    public static CommentDto mapToCommentDto(NewCommentDto newCommentDto) {
        return CommentDto.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public static CommentDto mapToCommentDtoFromUpdateDto(UpdateCommentDto updateCommentDto) {
        return CommentDto.builder()
                .text(updateCommentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }
}
