package ru.skypro.homework.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class Comments {
    private Integer count;
    private List<Comment> results;
}
