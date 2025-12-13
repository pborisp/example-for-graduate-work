package ru.skypro.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения "Homework Platform" - точка входа.
 * <p>
 * Этот класс запускает Spring Boot приложение для платформы управления домашними заданиями.
 * Использует аннотацию {@link SpringBootApplication}, которая объединяет:
 * <ul>
 *   <li>{@code @Configuration} - класс содержит конфигурацию Spring</li>
 *   <li>{@code @EnableAutoConfiguration} - автоматическая конфигурация Spring Boot</li>
 *   <li>{@code @ComponentScan} - сканирование компонентов в текущем пакете и подпакетах</li>
 * </ul>
 * </p>
 *
 * <p><b>Архитектура приложения:</b></p>
 * <pre>
 * ru.skypro.homework
 * ├── HomeworkApplication.java      (этот файл)
 * ├── config/                       (классы конфигурации)
 * ├── controller/                   (REST контроллеры)
 * ├── dto/                          (Data Transfer Objects)
 * ├── entity/                       (JPA сущности)
 * ├── exception/                    (кастомные исключения)
 * ├── repository/                   (репозитории Spring Data JPA)
 * ├── service/                      (сервисный слой)
 * └── util/                         (утилитарные классы)
 * </pre>
 *
 * <p><b>Запуск приложения:</b></p>
 * <ul>
 *   <li><b>Из IDE:</b> Запустите метод {@code main()} этого класса</li>
 *   <li><b>Через Maven:</b> {@code mvn spring-boot:run}</li>
 *   <li><b>Из JAR:</b> {@code java -jar homework-application.jar}</li>
 * </ul>
 *
 * <p><b>Доступные профили:</b></p>
 * <ul>
 *   <li><b>dev:</b> {@code -Dspring.profiles.active=dev} (разработка)</li>
 *   <li><b>test:</b> {@code -Dspring.profiles.active=test} (тестирование)</li>
 *   <li><b>prod:</b> {@code -Dspring.profiles.active=prod} (продакшен)</li>
 * </ul>
 *
 * <p><b>Основные эндпоинты после запуска:</b></p>
 * <ul>
 *   <li>Главная страница: <a href="http://localhost:8080">http://localhost:8080</a></li>
 *   <li>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
 *   <li>API Docs: <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></li>
 *   <li>Health check: <a href="http://localhost:8080/actuator/health">http://localhost:8080/actuator/health</a></li>
 * </ul>
 *
 * @see SpringBootApplication
 * @see SpringApplication
 */
@SpringBootApplication
public class HomeworkApplication {

    /**
     * Точка входа в приложение.
     * <p>
     * Запускает Spring Boot приложение с конфигурацией по умолчанию.
     * Порт по умолчанию: 8080 (можно изменить через {@code server.port} в application.properties).
     * </p>
     *
     * <p><b>Параметры командной строки:</b></p>
     * <ul>
     *   <li>{@code --server.port=9090} - изменить порт</li>
     *   <li>{@code --spring.profiles.active=prod} - активировать профиль</li>
     *   <li>{@code --spring.config.location=file:/config/} - внешняя конфигурация</li>
     *   <li>{@code --debug} - включить debug режим Spring Boot</li>
     * </ul>
     *
     * <p><b>Примеры запуска:</b></p>
     * <pre>
     * // Из IDE
     * public static void main(String[] args) {
     *     SpringApplication.run(HomeworkApplication.class, args);
     * }
     *
     * // С кастомными настройками
     * public static void main(String[] args) {
     *     SpringApplication app = new SpringApplication(HomeworkApplication.class);
     *     app.setBannerMode(Banner.Mode.OFF);
     *     app.setLogStartupInfo(false);
     *     app.run(args);
     * }
     * </pre>
     *
     * @param args аргументы командной строки, передаваемые в Spring Boot
     */
    public static void main(String[] args) {
        // Запуск Spring Boot приложения
        SpringApplication.run(HomeworkApplication.class, args);
    }
}
