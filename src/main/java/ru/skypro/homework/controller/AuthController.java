package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.LoginDTO;
import ru.skypro.homework.dto.RegisterDTO;
import ru.skypro.homework.service.AuthService;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // разрешить запросы с Fronted
@RestController // Контроллер
@RequiredArgsConstructor  // Автоматическое создание конструктора с полями final (lombok)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")  //Авторизация пользователя"
    @Operation(tags = {"Авторизация"}, summary = "Авторизация пользователя")
    public ResponseEntity<Void> login(@RequestBody LoginDTO login) {
        log.info("Login for User: {}", login.getUsername());
        if (authService.login(login.getUsername(), login.getPassword())) {
            log.info("User {} logged in", login.getUsername());
            return ResponseEntity.ok().build();
        } else {
            log.warn("User {} not logged in", login.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register") //Регистрация пользователя
    @Operation(tags = {"Регистрация"}, summary = "Регистрация пользователя")
    public ResponseEntity<String> register(@RequestBody RegisterDTO register) {
        if (authService.register(register)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }
    }
}
