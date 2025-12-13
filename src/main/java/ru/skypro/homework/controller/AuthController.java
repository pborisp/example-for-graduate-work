package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.LoginDTO;
import ru.skypro.homework.dto.RegisterDTO;
import ru.skypro.homework.service.AuthService;

/**
 * Контроллер для аутентификации и регистрации пользователей.
 * <p>
 * Предоставляет REST API эндпоинты для управления пользовательскими сессиями
 * с использованием HTTP Basic аутентификации.
 * </p>
 *
 * <p><b>Схема работы:</b></p>
 * <pre>
 * ┌─────────┐     POST /register    ┌─────────────┐
 * │ Клиент  │ ────────────────────> │ AuthController │
 * └─────────┘                       └─────────────┘
 *         │     POST /login            │
 *         │ ──────────────────────────>│
 *         │                            │ ──────┐
 *         │    200 OK / 401 Unauthorized│      │ Вызов AuthService
 *         │ <──────────────────────────│ <─────┘
 *         │                            │
 *         │   Последующие запросы:     │
 *         │   Header: Authorization: Basic ... │
 *         └────────────────────────────┘
 * </pre>
 *
 * <p><b>Безопасность:</b></p>
 * <ul>
 *   <li>Используется HTTP Basic Authentication</li>
 *   <li>Пароли хранятся в виде BCrypt хешей</li>
 *   <li>Сессии не используются (stateless)</li>
 *   <li>Все эндпоинты публичные (не требуют аутентификации)</li>
 * </ul>
 *
 * @see AuthService
 * @see LoginDTO
 * @see RegisterDTO
 * @see <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a>
 */
@Slf4j // Логирование через SLF4J (автоматически создает logger поле)
@CrossOrigin(value = "http://localhost:3000") // Разрешить CORS запросы с фронтенда
@RestController // Объявляет класс как Spring MVC REST контроллер
@RequiredArgsConstructor  // Lombok: создает конструктор с final полями (AuthService)
public class AuthController {

    /**
     * Сервис для бизнес-логики аутентификации.
     * Внедряется через конструктор благодаря {@code @RequiredArgsConstructor}.
     */
    private final AuthService authService;

    /**
     * Аутентификация пользователя в системе.
     * <p>
     * Проверяет учетные данные пользователя и возвращает статус аутентификации.
     * При успешной аутентификации клиент должен использовать полученные
     * credentials в заголовке Authorization для последующих запросов.
     * </p>
     *
     * <p><b>Flow:</b></p>
     * <ol>
     *   <li>Клиент отправляет username/password в теле запроса</li>
     *   <li>Сервис проверяет credentials в базе данных</li>
     *   <li>Возвращает 200 OK при успехе или 401 Unauthorized при ошибке</li>
     * </ol>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * POST /login HTTP/1.1
     * Content-Type: application/json
     *
     * {
     *   "username": "john_doe",
     *   "password": "securePassword123"
     * }
     * </pre>
     *
     * <p><b>Пример ответа:</b></p>
     * <ul>
     *   <li>Успех: HTTP 200 OK (пустое тело)</li>
     *   <li>Неудача: HTTP 401 Unauthorized (пустое тело)</li>
     * </ul>
     *
     * @param login DTO с учетными данными пользователя
     * @return ResponseEntity с HTTP статусом
     * @throws org.springframework.web.bind.MethodArgumentNotValidException если DTO невалидно
     * @see LoginDTO
     */
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

    /**
     * Регистрация нового пользователя в системе.
     * <p>
     * Создает нового пользователя с предоставленными данными.
     * Проверяет уникальность username/email перед созданием.
     * </p>
     *
     * <p><b>Требования к данным:</b></p>
     * <ul>
     *   <li>Username: уникальный, 3-50 символов</li>
     *   <li>Email: валидный email формат</li>
     *   <li>Password: минимум 8 символов, цифры и буквы</li>
     *   <li>Phone: опционально, валидный формат телефона</li>
     * </ul>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * POST /register HTTP/1.1
     * Content-Type: application/json
     *
     * {
     *   "username": "john_doe",
     *   "password": "SecurePass123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phone": "+79991234567",
     *   "email": "john.doe@example.com"
     * }
     * </pre>
     *
     * <p><b>Пример ответа:</b></p>
     * <ul>
     *   <li>Успех: HTTP 201 Created с сообщением</li>
     *   <li>Неудача: HTTP 400 Bad Request с причиной</li>
     * </ul>
     *
     * @param register DTO с данными для регистрации
     * @return ResponseEntity с HTTP статусом и сообщением
     *
     * @see RegisterDTO
     */
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
