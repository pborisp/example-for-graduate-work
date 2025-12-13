package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.PasswordDTO;
import ru.skypro.homework.dto.UserForUpdateDTO;
import ru.skypro.homework.dto.UsersDTO;

import java.io.IOException;

/**
 * Сервис для управления пользователями и их профилями.
 * <p>
 * Определяет контракт для операций с пользователями в системе.
 * Реализация этого интерфейса содержит бизнес-логику работы с профилями пользователей,
 * включая получение данных, обновление информации, смену пароля и управление аватарами.
 * </p>
 *
 * <p><b>Назначение интерфейса:</b></p>
 * <ul>
 *   <li><b>Абстракция:</b> отделяет контракт от реализации</li>
 *   <li><b>Тестируемость:</b> позволяет легко создавать моки и стабы</li>
 *   <li><b>Гибкость:</b> возможность иметь разные реализации (например, для тестов)</li>
 *   <li><b>Чистая архитектура:</b> разделение ответственности между слоями</li>
 * </ul>
 *
 * <p><b>Используется в:</b></p>
 * <ul>
 *   <li>{@link ru.skypro.homework.controller.UserController} - REST API эндпоинты</li>
 *   <li>{@link ru.skypro.homework.service.impl.UserServiceImpl} - реализация сервиса</li>
 * </ul>
 *
 * @see ru.skypro.homework.service.impl.UserServiceImpl
 * @see ru.skypro.homework.controller.UserController
 * @see UsersDTO
 * @see UserForUpdateDTO
 * @see PasswordDTO
 */
public interface UserService {

    /**
     * Получает профиль текущего авторизованного пользователя.
     * <p>
     * Определяет пользователя на основе контекста безопасности Spring Security.
     * Возвращает полную информацию о профиле, исключая чувствительные данные
     * (например, хеш пароля).
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code GET /users/me}</p>
     *
     * <p><b>Бизнес-правила:</b></p>
     * <ul>
     *   <li>Доступно только аутентифицированным пользователям</li>
     *   <li>Возвращает данные пользователя из текущей сессии</li>
     *   <li>Не включает пароль и другую чувствительную информацию</li>
     *   <li>Включает URL аватара, если он установлен</li>
     * </ul>
     *
     * <p><b>Пример ответа:</b></p>
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
     * @return DTO с профилем текущего пользователя
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @throws RuntimeException                                          если пользователь не найден в базе данных
     * @see ru.skypro.homework.controller.UserController#getUser()
     */
    UsersDTO getUsers();

    /**
     * Обновляет профиль текущего пользователя.
     * <p>
     * Принимает DTO с новыми данными и обновляет профиль текущего авторизованного пользователя.
     * Поддерживает частичное обновление - обновляются только те поля, которые не являются null.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code PATCH /users/me}</p>
     *
     * <p><b>Обновляемые поля:</b></p>
     * <table border="1">
     *   <tr><th>Поле DTO</th><th>Поле пользователя</th><th>Ограничения</th></tr>
     *   <tr><td>firstName</td><td>firstName</td><td>2-100 символов</td></tr>
     *   <tr><td>lastName</td><td>lastName</td><td>2-100 символов</td></tr>
     *   <tr><td>phone</td><td>phone</td><td>Валидный номер телефона</td></tr>
     * </table>
     *
     * <p><b>НЕ обновляются:</b></p>
     * <ul>
     *   <li>username (логин) - для смены нужен отдельный процесс</li>
     *   <li>email - должен быть уникальным, требует подтверждения</li>
     *   <li>password - используйте {@link #setPassword(PasswordDTO)}</li>
     *   <li>role - только администратор может изменять роли</li>
     * </ul>
     *
     * @param userForUpdateDTO DTO с новыми данными пользователя
     * @return обновленный DTO профиля пользователя
     * @throws IllegalArgumentException                                  если данные не проходят валидацию
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @see ru.skypro.homework.controller.UserController#updateUser(UserForUpdateDTO)
     */
    UserForUpdateDTO updateUser(UserForUpdateDTO userForUpdateDTO);

    /**
     * Обновляет аватар (изображение профиля) текущего пользователя.
     * <p>
     * Загружает и сохраняет файл изображения, затем обновляет путь к аватару в профиле пользователя.
     * Старое изображение удаляется из файловой системы.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code PATCH /users/me/image}</p>
     *
     * <p><b>Требования к файлу:</b></p>
     * <ul>
     *   <li><b>Форматы:</b> JPEG, PNG, GIF</li>
     *   <li><b>Максимальный размер:</b> 10MB (настраивается в application.properties)</li>
     *   <li><b>Рекомендуемое разрешение:</b> 200x200 - 1024x1024 пикселей</li>
     *   <li><b>Соотношение сторон:</b> 1:1 (квадратное) для лучшего отображения</li>
     * </ul>
     *
     * <p><b>Процесс загрузки:</b></p>
     * <ol>
     *   <li>Валидация файла (размер, тип, содержание)</li>
     *   <li>Генерация уникального имени файла</li>
     *   <li>Сохранение в директорию загрузок</li>
     *   <li>Обновление поля {@code image} в профиле пользователя</li>
     *   <li>Удаление старого файла аватара (если был)</li>
     * </ol>
     *
     * @param file файл изображения для загрузки в формате multipart/form-data
     * @throws IOException                                               если произошла ошибка ввода/вывода при сохранении файла
     * @throws IllegalArgumentException                                  если файл не соответствует требованиям
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @see ru.skypro.homework.controller.UserController#updateAvatar(MultipartFile)
     * @see org.springframework.web.multipart.MultipartFile
     */
    void updateUserImage(MultipartFile file) throws IOException;

    /**
     * Изменяет пароль текущего пользователя.
     * <p>
     * Проверяет текущий пароль перед установкой нового.
     * Новый пароль должен соответствовать политике безопасности.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code POST /users/set_password}</p>
     *
     * <p><b>Требования к паролю:</b></p>
     * <ul>
     *   <li><b>Минимальная длина:</b> 8 символов</li>
     *   <li><b>Сложность:</b> минимум одна цифра и одна буква</li>
     *   <li><b>История паролей:</b> нельзя использовать последние 5 паролей</li>
     * </ul>
     *
     * <p><b>Процесс смены пароля:</b></p>
     * <ol>
     *   <li>Проверка текущего пароля (должен совпадать с хранимым хешем)</li>
     *   <li>Валидация нового пароля (длина, сложность)</li>
     *   <li>Проверка истории паролей (нельзя повторять старые)</li>
     *   <li>Хеширование нового пароля с использованием BCrypt</li>
     *   <li>Сохранение нового хеша в базу данных</li>
     *   <li>Добавление старого пароля в историю</li>
     * </ol>
     *
     * <p><b>Сценарии ошибок:</b></p>
     * <ul>
     *   <li>Неверный текущий пароль → {@code IllegalArgumentException}</li>
     *   <li>Новый пароль не соответствует политике → {@code IllegalArgumentException}</li>
     *   <li>Новый пароль совпадает со старым → {@code IllegalArgumentException}</li>
     * </ul>
     *
     * @param passwordDTO DTO содержащий текущий и новый пароли
     * @throws IllegalArgumentException                                  если текущий пароль неверен или новый пароль невалиден
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @see ru.skypro.homework.controller.UserController#setPassword(PasswordDTO)
     */
    void setPassword(PasswordDTO passwordDTO);
}
