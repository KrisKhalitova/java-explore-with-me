package ru.practicum.ewm.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @Size(min = 2, max = 2000)
    @NotBlank(message = "Комментарий не может быть пустым.")
    private String text;

    private Long commentId;
}
