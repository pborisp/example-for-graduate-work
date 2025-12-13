package ru.skypro.homework.dto;

import lombok.*;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class AdDTO {
    private Long author;
    private String image;
    private Long pk;
    private Integer price;
    private String title;
}
