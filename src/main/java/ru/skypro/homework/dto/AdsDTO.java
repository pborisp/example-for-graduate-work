package ru.skypro.homework.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class AdsDTO {
    private Integer count;
    private List<FullAd> results;
}
