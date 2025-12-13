package ru.skypro.homework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
        * Конфигурация MVC для обслуживания статических файлов.
        * <p>
 * Настраивает обработку статических ресурсов, позволяя отдавать загруженные файлы
 * через HTTP. В основном используется для обслуживания изображений, загруженных пользователями.
 * </p>
        *
        * <p><b>Пример использования:</b></p>
        * <ul>
 *   <li>Файл, загруженный в {@code /uploads/images/avatar.jpg}</li>
        *   <li>Будет доступен по URL: {@code http://localhost:8080/uploads/images/avatar.jpg}</li>
        * </ul>
        *
        * <p><b>Конфигурация в application.properties/yml:</b></p>
        * <pre>
 * # Путь к директории для хранения загруженных файлов
 * # По умолчанию: /uploads (относительно рабочей директории)
 * # Можно указать абсолютный путь: /home/user/uploads
 * file.upload.dir=/path/to/uploads
 * </pre>
        *
        * @see WebMvcConfigurer
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Директория для хранения загруженных файлов.
     * <p>
     * Значение конфигурируется через свойство {@code file.upload.dir}.
     * Если свойство не задано, используется значение по умолчанию {@code /uploads}.
     * </p>
     *
     * <p><b>Рекомендации:</b></p>
     * <ul>
     *   <li>В разработке можно использовать относительный путь</li>
     *   <li>В production используйте абсолютный путь</li>
     *   <li>Убедитесь, что у приложения есть права на запись в эту директорию</li>
     * </ul>
     */
    @Value("${file.upload.dir:/uploads}")
    private String uploadDir;

    /**
     * Регистрирует обработчик статических ресурсов для обслуживания загруженных файлов.
     * <p>
     * Сопоставляет URL-путь {@code /uploads/**} с файловой системой по указанному пути.
     * Добавляет префикс {@code file:} для указания, что это файловая система, а не classpath.
     * </p>
     *
     * <p><b>Пример:</b></p>
     * <ul>
     *   <li>uploadDir = {@code /var/uploads}</li>
     *   <li>location = {@code file:/var/uploads/}</li>
     *   <li>Запрос к {@code /uploads/image.jpg} вернёт файл {@code /var/uploads/image.jpg}</li>
     * </ul>
     *
     * @param registry реестр обработчиков ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Преобразуем путь в формат, понятный Spring (добавляем file: префикс)
        String location = "file:" + uploadDir + "/";

        // Логирование для отладки (можно заменить на logger)
        System.out.println("Configuring static resources at: " + location);

        // Регистрируем обработчик
        registry.addResourceHandler("/uploads/**")  // Обрабатываем все запросы по пути /uploads/
                .addResourceLocations(location);    // Ищем файлы в указанной директории
    }
}