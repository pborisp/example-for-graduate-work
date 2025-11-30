package ru.skypro.homework.dto;

import lombok.*;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class AdForUpdate {
    private String title;
    private Integer price = 10000000;
    private String description;
}
