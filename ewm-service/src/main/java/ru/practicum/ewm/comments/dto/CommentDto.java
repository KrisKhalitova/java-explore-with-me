package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.Null;
import java.time.LocalDateTime;

import static ru.practicum.ewm.util.Constant.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {

    @Null
    private Long id;

    private String text;

    @Null
    private Long eventId;

    @Null
    private Long authorId;

    @Null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;
}
