package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // разрешить запросы с Fronted
@RestController // Контроллер
@RequiredArgsConstructor  // Автоматическое создание конструктора с полями final (lombok)
@RequestMapping("/users") // путь запроса
@Tag(name = "Пользователи") // заголовок
public class UserController {

    @PostMapping("/set_password") // Обновление пароля
    @Operation(summary = "Обновление пароля")
    public ResponseEntity<?> setPassword(@RequestBody Password password) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me") //Получение информации об авторизованном пользователе
    @Operation(summary = "Получение информации об авторизованном пользователе")
    public ResponseEntity<User> getUser() {
        return ResponseEntity.ok(new User());
    }

    @PatchMapping("/me") //Обновление информации об авторизованном пользователе
    @Operation(summary = "Обновление информации об авторизованном пользователе")
    public ResponseEntity<UserForUpdate> updateUser(@RequestBody UserForUpdate userForUpdate) {
        return ResponseEntity.ok(new UserForUpdate());
    }

    @PatchMapping(value = "/me/image", consumes = "multipart/form-data") //Обновление аватара авторизованного пользователя
    @Operation(summary = "Обновление аватара авторизованного пользователя")
    public ResponseEntity<?> updateAvatar(@RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok().build();
    }

}
