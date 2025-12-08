package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // разрешить запросы с Fronted
@RestController // Контроллер
@RequiredArgsConstructor  // Автоматическое создание конструктора с полями final (lombok)
@RequestMapping("/users") // путь запроса
@Tag(name = "Пользователи") // заголовок
public class UserController {

    private final UserService userService;

    @PostMapping("/set_password") // Обновление пароля
    @Operation(summary = "Обновление пароля")
    public ResponseEntity<?> setPassword(@RequestBody PasswordDTO password) {
        try {
            userService.setPassword(password);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/me") //Получение информации об авторизованном пользователе
    @Operation(summary = "Получение информации об авторизованном пользователе")
    public ResponseEntity<UsersDTO> getUser() {
        UsersDTO user = userService.getUsers();
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/me") //Обновление информации об авторизованном пользователе
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Обновление информации об авторизованном пользователе")
    public ResponseEntity<UserForUpdateDTO> updateUser(@RequestBody UserForUpdateDTO userForUpdate) {
        UserForUpdateDTO user = userService.updateUser(userForUpdate);
        return ResponseEntity.ok(user);
    }

    @PatchMapping(value = "/me/image", consumes = "multipart/form-data") //Обновление аватара авторизованного пользователя
    @Operation(summary = "Обновление аватара авторизованного пользователя")
    public ResponseEntity<UsersDTO> updateAvatar(@RequestParam("image") MultipartFile image) {
        try {
            userService.updateUserImage(image);
            UsersDTO user = userService.getUsers();
            return ResponseEntity.ok(user);
        } catch (IOException e) {
            log.error("Internal Server Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
