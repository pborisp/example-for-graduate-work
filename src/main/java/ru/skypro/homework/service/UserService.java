package ru.skypro.homework.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.PasswordDTO;
import ru.skypro.homework.dto.UserForUpdateDTO;
import ru.skypro.homework.dto.UsersDTO;

import java.io.IOException;


public interface UserService {
    UsersDTO getUsers();
    UserForUpdateDTO updateUser(UserForUpdateDTO userForUpdateDTO);
    void updateUserImage(MultipartFile file) throws IOException;
    void setPassword(PasswordDTO passwordDTO);
}
