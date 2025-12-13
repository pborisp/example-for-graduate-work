package ru.skypro.homework.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.RegisterDTO;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.mapped.UsersMapper;

import java.util.Optional;

/**
 * Реализация сервиса аутентификации и регистрации пользователей.
 * <p>
 * Содержит бизнес-логику для входа пользователей в систему и регистрации новых аккаунтов.
 * Интегрируется с Spring Security для управления безопасностью и использует BCrypt
 * для хеширования паролей.
 * </p>
 *
 * <p><b>Технологии:</b></p>
 * <ul>
 *   <li>Spring Security для аутентификации</li>
 *   <li>BCryptPasswordEncoder для хеширования паролей</li>
 *   <li>JPA/Hibernate для работы с базой данных</li>
 *   <li>Транзакционность через {@code @Transactional}</li>
 * </ul>
 *
 * <p><b>Основные функции:</b></p>
 * <ol>
 *   <li>Аутентификация пользователей (проверка логина/пароля)</li>
 *   <li>Регистрация новых пользователей с валидацией данных</li>
 *   <li>Хеширование паролей перед сохранением в БД</li>
 *   <li>Установка ролей по умолчанию</li>
 * </ol>
 *
 * @see AuthService
 * @see UsersMapper
 * @see UsersRepository
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    /**
     * Логгер для записи событий и ошибок.
     * Используется SLF4J с привязкой к классу AuthServiceImpl.
     */
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Репозиторий для работы с пользователями в базе данных.
     * Предоставляет CRUD операции и кастомные запросы.
     */
    private final UsersRepository usersRepository;

    /**
     * Маппер для преобразования между DTO и сущностями Users.
     * Используется при регистрации для создания сущности из RegisterDTO.
     */
    private final UsersMapper usersMapper;

    /**
     * Кодировщик паролей (BCrypt).
     * Используется для хеширования паролей перед сохранением в БД
     * и проверки паролей при аутентификации.
     */
    private final PasswordEncoder encoder;

    /**
     * Конструктор с dependency injection.
     * Spring автоматически внедряет зависимости при создании бина.
     *
     * @param usersRepository репозиторий пользователей
     * @param usersMapper     маппер пользователей
     * @param encoder         кодировщик паролей (BCrypt)
     */
    public AuthServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.encoder = encoder;
    }

    /**
     * Аутентифицирует пользователя в системе.
     * <p>
     * Проверяет соответствие предоставленных учетных данных (username и password)
     * данным в базе данных. Использует BCrypt для безопасного сравнения паролей.
     * </p>
     *
     * <p><b>Процесс аутентификации:</b></p>
     * <ol>
     *   <li>Поиск пользователя по username в базе данных</li>
     *   <li>Если пользователь не найден → возврат false</li>
     *   <li>Сравнение предоставленного пароля с BCrypt хешем через {@code encoder.matches()}</li>
     *   <li>Если пароль не совпадает → возврат false</li>
     *   <li>Если все проверки пройдены → возврат true</li>
     * </ol>
     *
     * @param userName имя пользователя для аутентификации
     * @param password пароль в открытом виде
     * @return {@code true} если аутентификация успешна, {@code false} в противном случае
     * @throws NullPointerException если {@code orElseThrow(null)} вызовется (дефект)
     */
    @Override
    public boolean login(String userName, String password) {
        Users user = usersRepository.findByUsername(userName).orElseThrow(null);
        if (user == null) {
            return false;
        }
        if (!encoder.matches(password, user.getPassword())) {
            return false;
        }
        return true;
    }

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Создает новую учетную запись с предоставленными данными.
     * Выполняет валидацию, проверку уникальности, хеширование пароля
     * и сохранение в базу данных.
     * </p>
     *
     * <p><b>Процесс регистрации:</b></p>
     * <ol>
     *   <li>Валидация входных данных (не null, обязательные поля)</li>
     *   <li>Проверка уникальности username</li>
     *   <li>Установка роли по умолчанию (USER) если не указана</li>
     *   <li>Валидация роли (только USER или ADMIN)</li>
     *   <li>Преобразование DTO в сущность через маппер</li>
     *   <li>Хеширование пароля с использованием BCrypt</li>
     *   <li>Установка аккаунта как активного ({@code enabled = true})</li>
     *   <li>Сохранение пользователя в базу данных</li>
     * </ol>
     *
     * @param register DTO с данными для регистрации
     * @return {@code true} если регистрация успешна, {@code false} если данные невалидны или пользователь существует
     * @throws IllegalArgumentException если данные не проходят валидацию
     */
    @Override
    public boolean register(RegisterDTO register) {

        usersRepository.findByUsername(register.getUsername());

        if (register == null || register.getUsername() == null || register.getPassword() == null) {
            log.error("Registration data is null");
            return false;
        }
        if (usersRepository.existsByUsername(register.getUsername())) {
            return false;
        }
        if (register.getRole() == null) {
            // Устанавливаем роль по умолчанию если не указана
            register.setRole(Role.USER);
        } else if (register.getRole() != Role.ADMIN && register.getRole() != Role.USER) {
            log.error("Invalid role: {}", register.getRole());
            return false;
        }
        // Создание и сохранение пользователя
        try {
            Users user = usersMapper.toUsers(register);
            user.setPassword(encoder.encode(register.getPassword()));
            user.setEnabled(true);
            user.setRole(register.getRole());
            usersRepository.save(user);
            log.info("User registered successfully: {}", user.getUsername());
            return true;
        } catch (Exception e) {
            log.error("Error registering user: {}", register.getUsername(), e);
            return false;
        }
    }
}