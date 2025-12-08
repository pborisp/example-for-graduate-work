package ru.skypro.homework.dto;

import lombok.*;

@AllArgsConstructor //конструктор с аргументами
@NoArgsConstructor  //пустой конструктор
@Getter
@Setter
@Data
public class RegisterDTO {
    private String username;
    private String password;
    private Role role;
    private String firstName;
    private String lastName;
    private String phone;
}
