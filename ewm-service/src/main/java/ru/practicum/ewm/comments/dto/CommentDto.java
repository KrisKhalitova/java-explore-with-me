package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.Constant.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {

    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime updated;
}
