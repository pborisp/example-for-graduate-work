package ru.skypro.homework.service.mapped;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.PasswordDTO;
import ru.skypro.homework.dto.RegisterDTO;
import ru.skypro.homework.dto.UsersDTO;
import ru.skypro.homework.dto.UserForUpdateDTO;
import ru.skypro.homework.model.Users;

@Slf4j
@Component
public class UsersMapper {

    //преобразование UsersDTO в Users - при регистрации
    public Users toUsers(RegisterDTO dto) {
        Users users = new Users();
        users.setUsername(dto.getUsername());
        users.setPassword(dto.getPassword());
        users.setFirstName(dto.getFirstName());
        users.setLastName(dto.getLastName());
        users.setPhone(dto.getPhone());
        users.setRole(dto.getRole());
        return users;
    }

    // преобразование Users в UserDTO
    public UsersDTO toUsersDTO(Users users) {
        UsersDTO dto = new UsersDTO();
        dto.setId(users.getId());
        dto.setUsername(users.getUsername());
        dto.setFirstName(users.getFirstName());
        dto.setLastName(users.getLastName());
        dto.setPhone(users.getPhone());
        dto.setRole(users.getRole());
        dto.setImage(users.getImage());
        return dto;
    }

    // преобразование UserForUpdateDTO в соответствующие поля Users
    public void updateUser(UserForUpdateDTO dto, Users users) {
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            users.setPhone(dto.getPhone());
        }
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            users.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            users.setLastName(dto.getLastName());
        }
    }

    // преобразование PasswordDTO в поле password Users
    public String setPassword(PasswordDTO dto) {
        return dto.getNewPassword();
    }

    // преобразование поля image DTO в поле image Users
    public void updateImage(UsersDTO dto, Users users) {
        if (dto.getImage() != null && dto.getImage().isBlank()) {
            users.setImage(dto.getImage());
        }
    }
}
