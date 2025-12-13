package ru.skypro.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.skypro.homework.dto.Role;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Сущность "Пользователь" (User) - основной пользователь системы.
 * <p>
 * Реализует интерфейс {@link UserDetails} для интеграции с Spring Security.
 * Хранит информацию о пользователе, его роли и связи с другими сущностями.
 * </p>
 *
 * <p><b>Таблица в базе данных:</b> {@code users}</p>
 * <p><b>Индексы:</b> username (unique)</p>
 *
 * <p><b>Связи с другими сущностями:</b></p>
 * <ul>
 *   <li>1:N с {@link Ads} - пользователь может создавать множество объявлений</li>
 *   <li>1:N с {@link Comments} - пользователь может оставлять множество комментариев</li>
 * </ul>
 *
 * <p><b>Интеграция с Spring Security:</b></p>
 * <ul>
 *   <li>Реализует {@link UserDetails} для аутентификации</li>
 *   <li>Использует {@link Role} для определения прав доступа</li>
 *   <li>Автоматически конвертирует роль в {@link GrantedAuthority}</li>
 * </ul>
 *
 * @see UserDetails
 * @see Ads
 * @see Comments
 * @see Role
 */
@Entity
@Data // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@Table(name = "users")
public class Users implements UserDetails {

    /**
     * Уникальный идентификатор пользователя.
     * <p>
     * Автоматически генерируется базой данных с использованием стратегии IDENTITY.
     * Это поле является первичным ключом таблицы.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальное имя пользователя для входа в систему.
     * <p>
     * Используется как логин при аутентификации.
     * В текущей реализации также используется как email.
     * </p>
     *
     * <p><b>Ограничения:</b></p>
     * <ul>
     *   <li>Не может быть null</li>
     *   <li>Должен быть уникальным</li>
     *   <li>Максимальная длина: 32 символа</li>
     * </ul>
     */
    @Column(nullable = false, unique = true, length = 32)
    private String username;

    /**
     * Хешированный пароль пользователя.
     * <p>
     * Должен храниться в зашифрованном виде (обычно BCrypt).
     * Никогда не возвращается в JSON ответах API благодаря {@code @JsonIgnore}.
     * </p>
     *
     * <p><b>Ограничения:</b></p>
     * <ul>
     *   <li>Не может быть null</li>
     *   <li>Максимальная длина: 64 символа (для BCrypt хеша)</li>
     * </ul>
     */
    @Column(nullable = false, length = 64)
    private String password;

    /**
     * Имя пользователя.
     * <p>
     * Отображается в профиле пользователя и в комментариях/объявлениях.
     * </p>
     *
     * <p><b>Ограничения:</b></p>
     * <ul>
     *   <li>Не может быть null</li>
     *   <li>Максимальная длина: 16 символов</li>
     * </ul>
     */
    @Column(name = "first_name", nullable = false, length = 16)
    private String firstName;

    /**
     * Фамилия пользователя.
     * <p>
     * Отображается в профиле пользователя и в комментариях/объявлениях.
     * </p>
     *
     * <p><b>Ограничения:</b></p>
     * <ul>
     *   <li>Не может быть null</li>
     *   <li>Максимальная длина: 16 символов</li>
     * </ul>
     */
    @Column(name = "last_name", nullable = false, length = 16)
    private String lastName;

    /**
     * Номер телефона пользователя.
     * <p>
     * Опциональное поле для контактной информации.
     * Может быть использовано для восстановления доступа или уведомлений.
     * </p>
     *
     * <p><b>Ограничения:</b></p>
     * <ul>
     *   <li>Максимальная длина: 18 символов (с учетом международного формата)</li>
     * </ul>
     */
    @Column(length = 18)
    private String phone;

    /**
     * Роль пользователя в системе.
     * <p>
     * Определяет уровень доступа пользователя.
     * Хранится в БД как строка (STRING), а не как число.
     * Значение по умолчанию: {@link Role#USER}
     * </p>
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /**
     * Путь к аватару пользователя.
     * <p>
     * Содержит относительный путь к файлу изображения от корневой директории загрузок.
     * Формат: {@code /uploads/avatars/filename.jpg}
     * Может быть null, если аватар не установлен.
     * </p>
     */
    @Column(name = "image_url")
    private String image;

    /**
     * Флаг активности учетной записи.
     * <p>
     * Используется Spring Security в методе {@link #isEnabled()}.
     * Если {@code false}, пользователь не может войти в систему.
     * Значение по умолчанию: {@code true} (активен).
     * </p>
     */
    @Column(nullable = true)
    private Boolean enabled = true;

    /**
     * Список объявлений, созданных пользователем.
     * <p>
     * Связь "один-ко-многим" с сущностью {@link Ads}.
     * Загружается лениво (LAZY) для оптимизации производительности.
     * Исключен из toString() и equals()/hashCode() чтобы избежать циклических ссылок.
     * Игнорируется при сериализации в JSON.
     * </p>
     *
     * <p><b>Каскадные операции:</b></p>
     * <ul>
     *   <li>ALL - все операции каскадируются на объявления</li>
     * </ul>
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<Ads> ads;

    /**
     * Список комментариев, оставленных пользователем.
     * <p>
     * Связь "один-ко-многим" с сущностью {@link Comments}.
     * Загружается лениво (LAZY) для оптимизации производительности.
     * Исключен из toString() и equals()/hashCode() чтобы избежать циклических ссылок.
     * Игнорируется при сериализации в JSON.
     * </p>
     *
     * <p><b>Каскадные операции:</b></p>
     * <ul>
     *   <li>ALL - все операции каскадируются на комментарии</li>
     * </ul>
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // Исключить из toString() чтобы избежать бесконечной рекурсии
    @JsonIgnore // Не включать в JSON ответы
    private List<Comments> comments;

    // =============================================
    // КОНСТРУКТОРЫ
    // =============================================

    /**
     * Полный конструктор для создания пользователя со всеми полями.
     *
     * @param id        уникальный идентификатор
     * @param email     email/username пользователя
     * @param password  хешированный пароль
     * @param firstName имя
     * @param lastName  фамилия
     * @param phone     телефон
     * @param role      роль пользователя
     * @param image     путь к аватару
     * @param ads       список объявлений пользователя
     * @param comments  список комментариев пользователя
     */
    public Users(Long id, String email, String password, String firstName, String lastName, String phone, Role role, String image, List<Ads> ads, List<Comments> comments) {
        this.id = id;
        this.username = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.image = image;
        this.ads = ads;
        this.comments = comments;
    }

    /**
     * Конструктор по умолчанию (требуется JPA).
     */
    public Users() {
    }

    /**
     * Минимальный конструктор для создания пользователя.
     * Используется при регистрации нового пользователя.
     *
     * @param password хешированный пароль
     * @param role     роль пользователя
     * @param email    email/username пользователя
     */
    public Users(String password, Role role, String email) {
        this.password = password;
        this.role = role;
        this.username = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Ads> getAds() {
        return ads;
    }

    public void setAds(List<Ads> ads) {
        this.ads = ads;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Users users = (Users) o;
        return Objects.equals(id, users.id) && Objects.equals(username, users.username) && Objects.equals(password, users.password) && Objects.equals(firstName, users.firstName) && Objects.equals(lastName, users.lastName) && Objects.equals(phone, users.phone) && role == users.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, firstName, lastName, phone, role);
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", image='" + image + '\'' +
                '}';
    }

    // =============================================
    // РЕАЛИЗАЦИЯ UserDetails (Spring Security)
    // =============================================

    /**
     * Возвращает authorities (права доступа) пользователя.
     * <p>
     * Конвертирует роль пользователя в {@link GrantedAuthority} для Spring Security.
     * Формат: {@code "ROLE_" + role.name()} (например, "ROLE_USER", "ROLE_ADMIN")
     * </p>
     *
     * @return коллекция authorities пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Указывает, не истек ли срок действия учетной записи.
     * <p>
     * В текущей реализации всегда возвращает {@code true}.
     * Можно расширить для реализации политики истечения срока действия аккаунта.
     * </p>
     *
     * @return true если учетная запись активна
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Указывает, не заблокирована ли учетная запись.
     * <p>
     * В текущей реализации всегда возвращает {@code true}.
     * Можно расширить для реализации блокировки аккаунта при нарушении правил.
     * </p>
     *
     * @return true если учетная запись не заблокирована
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Указывает, не истекли ли учетные данные (пароль).
     * <p>
     * В текущей реализации всегда возвращает {@code true}.
     * Можно расширить для реализации политики смены пароля.
     * </p>
     *
     * @return true если учетные данные не истекли
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Указывает, включена ли учетная запись.
     * <p>
     * Использует поле {@link #enabled} для определения статуса аккаунта.
     * Если {@code false}, пользователь не сможет войти в систему.
     * </p>
     *
     * @return true если учетная запись включена
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
