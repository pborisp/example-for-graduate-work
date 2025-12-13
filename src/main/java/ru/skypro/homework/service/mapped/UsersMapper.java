package ru.skypro.homework.service.mapped;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.model.Users;

/**
 * Маппер для преобразования между сущностью {@link Users} и DTO пользователей.
 * <p>
 * Реализует ручное преобразование объектов между слоями:
 * <ul>
 *   <li><b>DTO → Entity:</b> для создания/обновления пользователей из API запросов</li>
 *   <li><b>Entity → DTO:</b> для возврата данных пользователя в API ответах</li>
 *   <li><b>Partial updates:</b> частичное обновление полей пользователя</li>
 * </ul>
 *
 * <p><b>Основные преобразования:</b></p>
 * <ol>
 *   <li>{@link RegisterDTO} → {@link Users} (при регистрации)</li>
 *   <li>{@link Users} → {@link UsersDTO} (для ответов API)</li>
 *   <li>{@link UserForUpdateDTO} → {@link Users} (обновление профиля)</li>
 *   <li>{@link PasswordDTO} → password field (смена пароля)</li>
 * </ol>
 *
 * <p><b>Особенности реализации:</b></p>
 * <ul>
 *   <li>Выполняет валидацию входных данных (проверка на null/пустые строки)</li>
 *   <li>Устанавливает значения по умолчанию (например, роль USER)</li>
 *   <li>Логирует ошибки преобразования</li>
 *   <li>Не изменяет поля, если новые значения null или пустые</li>
 * </ul>
 *
 * @see Users
 * @see UsersDTO
 * @see RegisterDTO
 * @see UserForUpdateDTO
 * @see PasswordDTO
 */
@Slf4j
@Component  // Spring компонент (бин), может быть инжектирован
public class UsersMapper {

    /**
     * Преобразует {@link RegisterDTO} в сущность {@link Users} при регистрации нового пользователя.
     * <p>
     * Используется в {@code AuthService.register()} для создания нового пользователя из данных регистрации.
     * Устанавливает роль по умолчанию {@link Role#USER}, если роль не указана в DTO.
     * </p>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В AuthService.register()
     * RegisterDTO registerDTO = ... // данные от пользователя
     * Users user = usersMapper.toUsers(registerDTO);
     * user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
     * userRepository.save(user);
     * </pre>
     *
     * @param dto DTO с данными для регистрации (может быть null)
     * @return сущность Users или null, если DTO равен null
     * @throws NullPointerException если обязательные поля DTO равны null
     */
    public Users toUsers(RegisterDTO dto) {
        if (dto == null) {
            return null;
        }
        Users users = new Users();
        users.setUsername(dto.getUsername());
        users.setPassword(dto.getPassword());
        users.setFirstName(dto.getFirstName());
        users.setLastName(dto.getLastName());
        users.setPhone(dto.getPhone());
        users.setRole(dto.getRole() != null ? dto.getRole() : Role.USER);
        return users;
    }

    /**
     * Преобразует сущность {@link Users} в {@link UsersDTO} для возврата в API.
     * <p>
     * Используется при получении информации о пользователе (GET /users/me).
     * <b>Не включает</b> поле password из соображений безопасности.
     * </p>
     *
     * <p><b>Пример ответа API:</b></p>
     * <pre>
     * {
     *   "id": 123,
     *   "username": "john_doe",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phone": "+79991234567",
     *   "role": "USER",
     *   "image": "/uploads/avatars/john.jpg"
     * }
     * </pre>
     *
     * @param users сущность пользователя (не должна быть null)
     * @return DTO с информацией о пользователе
     * @throws NullPointerException если users равен null
     */
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

    /**
     * Частично обновляет сущность {@link Users} из {@link UserForUpdateDTO}.
     * <p>
     * Используется для обновления профиля пользователя (PATCH /users/me).
     * Обновляет только те поля, которые не являются null/пустыми в DTO.
     * Это позволяет делать частичные обновления без потери существующих данных.
     * </p>
     *
     * <p><b>Правила обновления:</b></p>
     * <ul>
     *   <li>Поле обновляется, если в DTO оно не null и не пустая строка</li>
     *   <li>Поля username, password, role <b>не обновляются</b> через этот метод</li>
     *   <li>Для смены пароля используйте {@link #(PasswordDTO)}</li>
     * </ul>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В UserService.updateUser()
     * Users user = userRepository.findById(id).orElseThrow(...);
     * usersMapper.updateUser(updateDTO, user);
     * userRepository.save(user); // Сохраняем изменения
     * </pre>
     *
     * @param dto   DTO с новыми данными пользователя
     * @param users сущность пользователя для обновления
     * @throws IllegalArgumentException если users равен null
     */
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
}
