package ru.skypro.homework.service;

import ru.skypro.homework.dto.RegisterDTO;

/**
 * Сервис аутентификации и регистрации пользователей.
 * <p>
 * Определяет контракт для операций управления доступом к системе:
 * вход (аутентификация) и регистрация новых пользователей.
 * Реализация интегрируется с Spring Security для управления безопасностью.
 * </p>
 *
 * <p><b>Архитектурная роль:</b></p>
 * <ul>
 *   <li><b>Слой сервисов:</b> содержит бизнес-логику аутентификации</li>
 *   <li><b>Абстракция:</b> отделяет логику безопасности от контроллеров</li>
 *   <li><b>Интеграция:</b> работает с {@link org.springframework.security.authentication.AuthenticationManager}</li>
 * </ul>
 *
 * <p><b>Используемые технологии:</b></p>
 * <ul>
 *   <li>HTTP Basic Authentication</li>
 *   <li>BCrypt для хеширования паролей</li>
 *   <li>Spring Security для управления доступом</li>
 *   <li>JPA/Hibernate для работы с базой данных</li>
 * </ul>
 *
 * @see ru.skypro.homework.service.impl.AuthServiceImpl
 * @see ru.skypro.homework.controller.AuthController
 * @see org.springframework.security.authentication.AuthenticationManager
 */
public interface AuthService {

    /**
     * Аутентифицирует пользователя в системе.
     * <p>
     * Проверяет предоставленные учетные данные (username и password)
     * и устанавливает контекст безопасности для текущей сессии.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code POST /login}</p>
     *
     * <p><b>Процесс аутентификации:</b></p>
     * <ol>
     *   <li>Поиск пользователя по username в базе данных</li>
     *   <li>Проверка, активен ли пользователь (поле {@code enabled})</li>
     *   <li>Сравнение предоставленного пароля с BCrypt хешем в БД</li>
     *   <li>Установка {@link org.springframework.security.core.context.SecurityContextHolder}</li>
     *   <li>Создание сессии безопасности для последующих запросов</li>
     * </ol>
     *
     * <p><b>Сценарии:</b></p>
     * <table border="1">
     *   <tr><th>Сценарий</th><th>Результат</th><th>HTTP Status</th></tr>
     *   <tr><td>Верные credentials</td><td>true</td><td>200 OK</td></tr>
     *   <tr><td>Неверный пароль</td><td>false</td><td>401 Unauthorized</td></tr>
     *   <tr><td>Пользователь не найден</td><td>false</td><td>401 Unauthorized</td></tr>
     *   <tr><td>Аккаунт заблокирован</td><td>false</td><td>403 Forbidden</td></tr>
     * </table>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В контроллере
     * if (authService.login(loginDTO.getUsername(), loginDTO.getPassword())) {
     *     return ResponseEntity.ok().build(); // 200 OK
     * } else {
     *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
     * }
     * </pre>
     *
     * @param userName имя пользователя (логин) для аутентификации
     * @param password пароль в открытом виде (будет сравнен с BCrypt хешем)
     * @return {@code true} если аутентификация успешна, {@code false} в противном случае
     * @throws IllegalArgumentException если userName или password равны null или пустые
     * @see ru.skypro.homework.controller.AuthController#
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    boolean login(String userName, String password);

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Создает новую учетную запись пользователя с предоставленными данными.
     * Проверяет уникальность username и email перед созданием.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code POST /register}</p>
     *
     * <p><b>Процесс регистрации:</b></p>
     * <ol>
     *   <li>Валидация входных данных (обязательные поля, форматы)</li>
     *   <li>Проверка уникальности username и email в базе данных</li>
     *   <li>Хеширование пароля с использованием BCrypt</li>
     *   <li>Создание новой сущности {@link ru.skypro.homework.model.Users}</li>
     *   <li>Установка роли по умолчанию: {@code Role.USER}</li>
     *   <li>Сохранение пользователя в базу данных</li>
     *   <li>Назначение начальных разрешений и настроек</li>
     * </ol>
     *
     * <p><b>Требования к данным:</b></p>
     * <table border="1">
     *   <tr><th>Поле</th><th>Требования</th><th>Пример</th></tr>
     *   <tr><td>username</td><td>3-50 символов, уникальный</td><td>"john_doe"</td></tr>
     *   <tr><td>password</td><td>≥8 символов, цифры+буквы</td><td>"SecurePass123"</td></tr>
     *   <tr><td>firstName</td><td>2-100 символов</td><td>"John"</td></tr>
     *   <tr><td>lastName</td><td>2-100 символов</td><td>"Doe"</td></tr>
     *   <tr><td>email</td><td>валидный email, уникальный</td><td>"john.doe@example.com"</td></tr>
     *   <tr><td>phone</td><td>опционально, валидный номер</td><td>"+79991234567"</td></tr>
     * </table>
     *
     * <p><b>Сценарии:</b></p>
     * <table border="1">
     *   <tr><th>Сценарий</th><th>Результат</th><th>HTTP Status</th></tr>
     *   <tr><td>Успешная регистрация</td><td>true</td><td>201 Created</td></tr>
     *   <tr><td>Username уже существует</td><td>false</td><td>400 Bad Request</td></tr>
     *   <tr><td>Email уже существует</td><td>false</td><td>400 Bad Request</td></tr>
     *   <tr><td>Некорректные данные</td><td>false</td><td>400 Bad Request</td></tr>
     * </table>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В контроллере
     * if (authService.register(registerDTO)) {
     *     return ResponseEntity.status(HttpStatus.CREATED)
     *             .body("User registered successfully");
     * } else {
     *     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
     *             .body("User already exists");
     * }
     * </pre>
     *
     * @param register DTO с данными для регистрации нового пользователя
     * @return {@code true} если регистрация успешна, {@code false} если пользователь уже существует или данные невалидны
     * @throws IllegalArgumentException если register DTO равен null или содержит невалидные данные
     * @see ru.skypro.homework.controller.AuthController#register(RegisterDTO)
     * @see ru.skypro.homework.dto.RegisterDTO
     */
    boolean register(RegisterDTO register);
}
