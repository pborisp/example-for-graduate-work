package ru.skypro.homework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;


/**
 * Конфигурация безопасности приложения.
 * <p>
 * Настраивает аутентификацию, авторизацию и CORS для REST API.
 * Использует Basic аутентификацию с хранением пользователей в базе данных.
 * </p>
 *
 * <p><b>Основные функции:</b></p>
 * <ul>
 *   <li>Настройка доступа к эндпоинтам (белый список и защищённые пути)</li>
 *   <li>Конфигурация CORS для кросс-доменных запросов</li>
 *   <li>Настройка аутентификации через базу данных</li>
 *   <li>Включение аннотаций Spring Security (@PreAuthorize и др.)</li>
 * </ul>
 *
 * @see EnableGlobalMethodSecurity
 * @see SecurityFilterChain
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)  // Включает аннотации @PreAuthorize, @PostAuthorize
public class WebSecurityConfig {

    /**
     * Белый список эндпоинтов, доступных без аутентификации.
     * <p>
     * Содержит пути для:
     * <ul>
     *   <li>Swagger/OpenAPI документации</li>
     *   <li>Регистрации и входа новых пользователей</li>
     *   <li>Статических файлов (изображений)</li>
     *   <li>Публичных объявлений</li>
     * </ul>
     */
    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",  // Ресурсы Swagger
            "/swagger-ui.html",       // Swagger UI интерфейс
            "/v3/api-docs",           // OpenAPI спецификация
            "/webjars/**",            // WebJars ресурсы
            "/login",                 // Эндпоинт входа (если используется)
            "/register",              // Эндпоинт регистрации
            "/uploads/**",            // Статические файлы (изображения)
            "/ads"                    // Публичный список объявлений (GET)
    };

    /**
     * Бин для кодирования паролей.
     * <p>
     * Используется BCrypt - адаптивный алгоритм хеширования с "солью".
     * Автоматически управляет сложностью хеширования.
     * </p>
     *
     * <p><b>Рекомендации:</b></p>
     * <ul>
     *   <li>BCrypt strength по умолчанию: 10 (2^10 итераций)</li>
     *   <li>Для повышения безопасности можно увеличить strength: {@code new BCryptPasswordEncoder(12)}</li>
     *   <li>Не меняйте алгоритм после начала эксплуатации системы</li>
     * </ul>
     *
     * @return экземпляр PasswordEncoder
     * @see BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Сервис для загрузки данных пользователей из базы данных.
     * <p>
     * Использует кастомные SQL запросы для интеграции с существующей схемой БД.
     * Предполагает следующую структуру таблицы users:
     * </p>
     * <pre>
     * CREATE TABLE users (
     *     username VARCHAR(255) PRIMARY KEY,
     *     password VARCHAR(255) NOT NULL,  -- BCrypt хеш
     *     enabled BOOLEAN DEFAULT true,
     *     role VARCHAR(50) NOT NULL        -- Например: 'ROLE_USER', 'ROLE_ADMIN'
     * );
     * </pre>
     *
     * @param dataSource источник данных для подключения к БД
     * @return UserDetailsService для работы с пользователями
     * @see JdbcUserDetailsManager
     */
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        // Кастомные запросы для работы с существующей схемой БД
        jdbcUserDetailsManager.setUsersByUsernameQuery(
                "SELECT username, password, enabled FROM users WHERE username=?"
        );
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT username, role FROM users WHERE username=?"
        );

        return jdbcUserDetailsManager;
    }

    /**
     * Основная цепочка фильтров безопасности.
     * <p>
     * Настраивает:
     * <ul>
     *   <li>CSRF защита отключена (т.к. это REST API)</li>
     *   <li>Авторизация запросов на основе URL паттернов</li>
     *   <li>CORS конфигурация</li>
     *   <li>Basic аутентификация</li>
     * </ul>
     * </p>
     *
     * <p><b>Важно:</b> CSRF отключена, потому что REST API обычно используется
     * с клиентами, которые не поддерживают CSRF токены (мобильные приложения, SPA).</p>
     *
     * @param http объект для настройки безопасности
     * @return сконфигурированная цепочка фильтров
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()  // Отключаем CSRF для REST API
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .mvcMatchers(AUTH_WHITELIST)  // URL из белого списка
                                        .permitAll()  // Разрешаем доступ без авторизации
                                        .mvcMatchers("/ads/**", "/users/**")   // Все остальные URL
                                        .authenticated())  // Требуют авторизации
                .cors().configurationSource(corsConfigurationSource())  // Включаем CORS
                .and()
                .httpBasic(withDefaults());  // Используем Basic аутентификацию

        return http.build();
    }

    /**
     * Конфигурация CORS (Cross-Origin Resource Sharing).
     * <p>
     * Разрешает кросс-доменные запросы от указанных источников.
     * Необходимо для работы фронтенда на отдельном домене/порту.
     * </p>
     *
     * <p><b>Текущая конфигурация разрешает:</b></p>
     * <ul>
     *   <li>Источники: localhost:3000 (frontend) и localhost:8080 (backend)</li>
     *   <li>Методы: GET, POST, PUT, PATCH, DELETE, OPTIONS</li>
     *   <li>Заголовки: все</li>
     *   <li>Креденшалы: разрешены (для передачи логина/пароля)</li>
     *   <li>Кэширование предварительных запросов: 1 час</li>
     * </ul>
     *
     * <p><b>Для production нужно:</b></p>
     * <ul>
     *   <li>Заменить allowedOrigins на реальные домены фронтенда</li>
     *   <li>Ограничить allowedMethods если не нужны все</li>
     *   <li>Ограничить allowedHeaders конкретными значениями</li>
     * </ul>
     *
     * @return конфигурация CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешаем доступ с указанных источников
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // Frontend (React/Vue/Angular)
                "http://localhost:8080"   // Backend (для тестирования)
        ));

        // Разрешаем основные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));  // Разрешаем все заголовки
        configuration.setExposedHeaders(Arrays.asList("*"));  // Разрешаем все расширенные заголовки
        configuration.setAllowCredentials(true);  // Разрешаем передачу креденшалов (куки, авторизация)
        configuration.setMaxAge(3600L);  // Кэшируем preflight запросы на 1 час

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Применяем ко всем путям
        return source;
    }
}