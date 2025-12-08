package ru.skypro.homework.dto;

import lombok.*;

import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class UserForUpdateDTO {
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^\\+7 \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}$",
            message = "Телефон должен быть в формате +7 (XXX) XXX-XX-XX")
    private String phone;
}
