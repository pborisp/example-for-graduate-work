package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.PasswordDTO;
import ru.skypro.homework.dto.UserForUpdateDTO;
import ru.skypro.homework.dto.UsersDTO;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserAuthServise;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.mapped.UsersMapper;

import java.io.IOException;

/**
 * Реализация сервиса для управления пользователями и их профилями.
 * <p>
 * Содержит бизнес-логику работы с профилями пользователей: получение информации,
 * обновление данных, смена пароля и управление аватарами.
 * Все операции выполняются в контексте текущего аутентифицированного пользователя.
 * </p>
 *
 * <p><b>Архитектура:</b></p>
 * <ul>
 *   <li><b>Слой сервисов:</b> реализует интерфейс {@link UserService}</li>
 *   <li><b>Транзакционность:</b> методы выполняются в управляемых транзакциях</li>
 *   <li><b>Безопасность:</b> использует {@link UserAuthServise} для определения текущего пользователя</li>
 *   <li><b>Файловые операции:</b> делегирует работу с изображениями {@link ImageService}</li>
 * </ul>
 *
 * <p><b>Зависимости:</b></p>
 * <ul>
 *   <li>{@link UsersRepository} - доступ к данным пользователей</li>
 *   <li>{@link UsersMapper} - преобразование между сущностями и DTO</li>
 *   <li>{@link PasswordEncoder} - хеширование паролей (BCrypt)</li>
 *   <li>{@link ImageService} - работа с файлами изображений</li>
 *   <li>{@link UserAuthServise} - определение текущего пользователя</li>
 * </ul>
 *
 * @see UserService
 * @see ru.skypro.homework.controller.UserController
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    /**
     * Кодировщик паролей (BCrypt).
     * Используется для проверки текущих паролей и хеширования новых.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Сервис для работы с изображениями.
     * Обрабатывает загрузку, сохранение и управление файлами аватаров.
     */
    private final ImageService imageService;

    /**
     * Сервис для работы с аутентификацией и определением текущего пользователя.
     * Интегрируется с Spring Security для получения контекста безопасности.
     */
    private final UserAuthServise userAuthServise;

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     * <p>
     * Определяет пользователя из контекста безопасности и возвращает его данные
     * в формате DTO. Не включает чувствительную информацию (например, пароль).
     * </p>
     *
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Получение текущего пользователя через {@link UserAuthServise}</li>
     *   <li>Проверка, что пользователь не null</li>
     *   <li>Преобразование сущности в DTO через {@link UsersMapper}</li>
     *   <li>Возврат DTO с профилем пользователя</li>
     * </ol>
     *
     * @return DTO с профилем текущего пользователя
     * @throws RuntimeException если пользователь не аутентифицирован (делегируется из UserAuthServise)
     * @see UserAuthServise#getCurrentUser()
     * @see UsersMapper#toUsersDTO(Users)
     */
    @Override
    public UsersDTO getUsers() {
        // Получаем текущего пользователя
        Users user = userAuthServise.getCurrentUser();
        // Преобразуем в DTO
        UsersDTO dto = usersMapper.toUsersDTO(user);
        return dto;
    }

    /**
     * Обновляет профиль текущего пользователя.
     * <p>
     * Принимает DTO с новыми данными и обновляет профиль пользователя.
     * Поддерживает частичное обновление - обновляются только не-null поля.
     * </p>
     *
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Получение текущего пользователя</li>
     *   <li>Частичное обновление полей через {@link UsersMapper#updateUser(UserForUpdateDTO, Users)}</li>
     *   <li>Сохранение обновленного пользователя в БД</li>
     *   <li>Возврат того же DTO (возможно, с обновленными данными)</li>
     * </ol>
     *
     * @param userForUpdateDTO DTO с новыми данными пользователя
     * @return тот же DTO (без изменений)
     * @throws RuntimeException если пользователь не аутентифицирован
     */
    @Override
    public UserForUpdateDTO updateUser(UserForUpdateDTO userForUpdateDTO) {
        // Получаем текущего пользователя
        Users user = userAuthServise.getCurrentUser();
        // Обновляем поля пользователя
        usersMapper.updateUser(userForUpdateDTO, user);
        // Сохраняем изменения
        usersRepository.save(user);
        return userForUpdateDTO;
    }

    /**
     * Обновляет аватар (изображение профиля) текущего пользователя.
     * <p>
     * Загружает новое изображение, сохраняет его в файловой системе
     * и обновляет путь к аватару в профиле пользователя.
     * Выполняется в транзакции для обеспечения целостности данных.
     * </p>
     *
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Проверка, что файл не пустой</li>
     *   <li>Сохранение изображения через {@link ImageService}</li>
     *   <li>Обновление поля {@code image} в профиле пользователя</li>
     *   <li>Сохранение изменений в БД</li>
     *   <li>Принудительная синхронизация с БД через {@code flush()}</li>
     * </ol>
     *
     * @param image файл изображения для загрузки
     * @throws IOException              если произошла ошибка при сохранении файла
     * @throws IllegalArgumentException если файл пустой
     */
    @Override
    public void updateUserImage(MultipartFile image) throws IOException {
        // Получаем текущего пользователя
        Users user = userAuthServise.getCurrentUser();
        // Проверяем файл
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is empty");
        }
        // Сохраняем изображение
        String path = imageService.saveImage(image);
        // Обновляем профиль
        user.setImage(path);
        usersRepository.save(user);
        //сбрасываем принудительно кэш для текущего пользователя
        usersRepository.flush();
    }

    /**
     * Изменяет пароль текущего пользователя.
     * <p>
     * Проверяет текущий пароль перед установкой нового.
     * Новый пароль хешируется с использованием BCrypt перед сохранением.
     * </p>
     *
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Получение текущего пользователя</li>
     *   <li>Проверка текущего пароля через {@code passwordEncoder.matches()}</li>
     *   <li>Хеширование нового пароля</li>
     *   <li>Обновление поля {@code password} в профиле</li>
     *   <li>Сохранение изменений в БД</li>
     * </ol>
     *
     * @param passwordDTO DTO с текущим и новым паролем
     * @throws IllegalArgumentException если текущий пароль неверен
     * @throws RuntimeException         если пользователь не аутентифицирован
     */
    @Override
    public void setPassword(PasswordDTO passwordDTO) {
        // Получаем текущего пользователя
        Users user = userAuthServise.getCurrentUser();
        // Проверяем соответсвие введенного пароля существующему - если не соответствует - выбрасываем исключение
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        // Шифруем и устанавливаем новый пароль
        String newEncodedPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
        user.setPassword(newEncodedPassword);
        usersRepository.save(user);
    }
}
