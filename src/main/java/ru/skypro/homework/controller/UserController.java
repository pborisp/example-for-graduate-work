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
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

/**
 * Контроллер для управления профилями пользователей.
 * <p>
 * Предоставляет REST API для работы с профилем авторизованного пользователя,
 * включая обновление информации, смену пароля и управление аватаром.
 * </p>
 *
 * <p><b>Требования к доступу:</b></p>
 * <ul>
 *   <li>Все эндпоинты требуют аутентификации</li>
 *   <li>Пользователь может изменять только свой собственный профиль</li>
 *   <li>Для доступа используется HTTP Basic аутентификация</li>
 * </ul>
 *
 * <p><b>Базовый URL:</b> {@code /users}</p>
 *
 * <p><b>Пример использования:</b></p>
 * <pre>
 * GET /users/me                  # Получить свой профиль
 * PATCH /users/me               # Обновить профиль
 * POST /users/set_password      # Сменить пароль
 * PATCH /users/me/image         # Обновить аватар
 * </pre>
 *
 * @see UserService
 * @see ImageService
 * @see UsersDTO
 * @see PasswordDTO
 * @see UserForUpdateDTO
 */
@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // Разрешить CORS запросы с фронтенда
@RestController // REST контроллер Spring MVC
@RequiredArgsConstructor  // Lombok: автоматически создает конструктор для final полей
@RequestMapping("/users") // Базовый путь для всех эндпоинтов контроллера
@Tag(name = "Пользователи") // Swagger тег
public class UserController {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Обновление пароля текущего пользователя.
     * <p>
     * Позволяет авторизованному пользователю изменить свой пароль.
     * Для смены пароля требуется предоставить текущий пароль для проверки.
     * </p>
     *
     * <p><b>Безопасность:</b></p>
     * <ul>
     *   <li>Текущий пароль проверяется перед изменением</li>
     *   <li>Новый пароль должен соответствовать политике безопасности</li>
     *   <li>Пароль хешируется с использованием BCrypt</li>
     * </ul>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * POST /users/set_password HTTP/1.1
     * Authorization: Basic dXNlcjpwYXNzd29yZA==
     * Content-Type: application/json
     *
     * {
     *   "currentPassword": "oldPassword123",
     *   "newPassword": "newSecurePassword456"
     * }
     * </pre>
     *
     * <p><b>Ответы:</b></p>
     * <ul>
     *   <li>200 OK: Пароль успешно изменен</li>
     *   <li>403 Forbidden: Неверный текущий пароль</li>
     *   <li>401 Unauthorized: Пользователь не аутентифицирован</li>
     * </ul>
     *
     * @param password DTO с текущим и новым паролем
     * @return ResponseEntity с HTTP статусом
     * @throws IllegalArgumentException если текущий пароль неверен
     */
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

    /**
     * Получение профиля текущего пользователя.
     * <p>
     * Возвращает полную информацию о профиле авторизованного пользователя.
     * Использует контекст безопасности Spring для определения текущего пользователя.
     * </p>
     *
     * <p><b>Пример ответа:</b></p>
     * <pre>
     * {
     *   "id": 123,
     *   "username": "john_doe",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phone": "+79991234567",
     *   "email": "john.doe@example.com",
     *   "image": "/uploads/avatars/john_doe.jpg",
     *   "role": "ROLE_USER"
     * }
     * </pre>
     *
     * @return ResponseEntity с профилем пользователя
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     */
    @GetMapping("/me") //Получение информации об авторизованном пользователе
    @Operation(summary = "Получение информации об авторизованном пользователе")
    public ResponseEntity<UsersDTO> getUser() {
        UsersDTO user = userService.getUsers();
        return ResponseEntity.ok(user);
    }

    /**
     * Обновление профиля текущего пользователя.
     * <p>
     * Позволяет изменить информацию профиля (имя, фамилия, телефон).
     * Username и email обычно не могут быть изменены через этот эндпоинт.
     * </p>
     *
     * <p><b>Изменяемые поля:</b></p>
     * <ul>
     *   <li>firstName (имя)</li>
     *   <li>lastName (фамилия)</li>
     *   <li>phone (телефон)</li>
     * </ul>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * PATCH /users/me HTTP/1.1
     * Authorization: Basic dXNlcjpwYXNzd29yZA==
     * Content-Type: application/json
     *
     * {
     *   "firstName": "Johnathan",
     *   "lastName": "Doer",
     *   "phone": "+79998765432"
     * }
     * </pre>
     *
     * @param userForUpdate DTO с обновленными данными пользователя
     * @return ResponseEntity с обновленным профилем
     */
    @PatchMapping("/me") //Обновление информации об авторизованном пользователе
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Обновление информации об авторизованном пользователе")
    public ResponseEntity<UserForUpdateDTO> updateUser(@RequestBody UserForUpdateDTO userForUpdate) {
        UserForUpdateDTO user = userService.updateUser(userForUpdate);
        return ResponseEntity.ok(user);
    }

    /**
     * Обновление аватара текущего пользователя.
     * <p>
     * Загружает новое изображение аватара для пользователя.
     * Поддерживает форматы: JPEG, PNG, GIF.
     * Максимальный размер файла: 10MB (настраивается в application.properties).
     * </p>
     *
     * <p><b>Требования к изображению:</b></p>
     * <ul>
     *   <li>Формат: JPEG, PNG, GIF</li>
     *   <li>Максимальный размер: 10MB</li>
     *   <li>Рекомендуемое разрешение: 200x200 - 1024x1024 пикселей</li>
     * </ul>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * PATCH /users/me/image HTTP/1.1
     * Authorization: Basic dXNlcjpwYXNzd29yZA==
     * Content-Type: multipart/form-data; boundary=boundary
     *
     * --boundary
     * Content-Disposition: form-data; name="image"; filename="avatar.jpg"
     * Content-Type: image/jpeg
     *
     * [бинарные данные изображения]
     * --boundary--
     * </pre>
     *
     * @param image файл изображения для загрузки
     * @return ResponseEntity с обновленным профилем пользователя (включая новый путь к аватару)
     * @throws IOException если произошла ошибка при сохранении файла
     */
    @PatchMapping(value = "/me/image", consumes = "multipart/form-data")
    //Обновление аватара авторизованного пользователя
    @Operation(summary = "Обновление аватара авторизованного пользователя")
    public ResponseEntity<UsersDTO> updateAvatar(@RequestParam("image") MultipartFile image) {
        log.info("Updating avatar, file size: {}, content type: {}",
                image.getSize(), image.getContentType());
        try {
            userService.updateUserImage(image);
            UsersDTO user = userService.getUsers();
            log.info("Avatar updated successfully for user, new image: {}", user.getImage());
            return ResponseEntity.ok(user);
        } catch (IOException e) {
            log.error("Internal Server Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
