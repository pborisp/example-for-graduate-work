package ru.skypro.homework.dto;

import lombok.*;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class CommentDTO {
    private Long author;
    private String authorImage;
    private String authorFirstName;
    private Long createdAt;
    private Long pk;
    private String text;
}
