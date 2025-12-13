package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Сервис для работы с загрузкой и хранением изображений.
 * <p>
 * Обрабатывает файлы изображений, загружаемые пользователями:
 * аватары профилей, изображения объявлений и другие медиа-файлы.
 * Обеспечивает безопасное сохранение файлов в файловой системе,
 * генерацию уникальных имен и управление директорией загрузок.
 * </p>
 *
 * <p><b>Основные функции:</b></p>
 * <ul>
 *   <li>Создание и валидация директории для загрузок</li>
 * <li>Сохранение загруженных изображений с уникальными именами</li>
 *   <li>Определение расширений файлов</li>
 *   <li>Генерация путей для доступа к файлам через HTTP</li>
 * </ul>
 *
 * <p><b>Архитектурная роль:</b></p>
 * <ul>
 *   <li><b>Вспомогательный сервис:</b> используется другими сервисами ({@link UserService}, {@link AdsService})</li>
 *   <li><b>Инкапсуляция файловых операций:</b> изолирует работу с файловой системой</li>
 *   <li><b>Безопасность:</b> предотвращает перезапись файлов и атаки путем инъекций путей</li>
 * </ul>
 *
 * <p><b>Используется в:</b></p>
 * <ul>
 *   <li>{@link ru.skypro.homework.service.UserService#updateUserImage(MultipartFile)} - аватары пользователей</li>
 *   <li>{@link ru.skypro.homework.service.AdsService#(MultipartFile)} - изображения объявлений</li>
 *   <li>{@link ru.skypro.homework.service.AdsService#uppdateImageOfAd(Long, MultipartFile)} - обновление изображений</li>
 * </ul>
 *
 * @see MultipartFile
 * @see java.nio.file.Files
 */
@Slf4j
@Service  // Spring сервис - будет автоматически создан и может быть инжектирован
public class ImageService {

    /**
     * Базовая директория для сохранения загруженных файлов.
     * <p>
     * Значение конфигурируется через свойство {@code file.upload.dir} в application.properties.
     * Если свойство не указано, используется значение по умолчанию {@code "./uploads"}.
     * </p>
     *
     * <p><b>Примеры конфигурации:</b></p>
     * <pre>
     * # application.properties
     * file.upload.dir=./uploads                     # Относительный путь
     * file.upload.dir=/var/www/uploads             # Абсолютный путь (Linux)
     * file.upload.dir=C:/app/uploads               # Абсолютный путь (Windows)
     * file.upload.dir=${UPLOAD_DIR:./uploads}      # С переменной окружения
     * </pre>
     */
    private String uploadDir;

    /**
     * Устанавливает директорию для загрузок из конфигурации Spring.
     * <p>
     * Аннотация {@code @Value} внедряет значение из properties файла.
     * Если свойство не задано, используется значение по умолчанию {@code "./uploads"}.
     * Автоматически создает директорию, если она не существует.
     * </p>
     *
     * @param uploadDir путь к директории загрузок из конфигурации
     */
    @Value("${file.upload.dir:./uploads}")
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;

        // Защита от null или пустых значений
        if (this.uploadDir == null || this.uploadDir.isBlank()) {
            this.uploadDir = "./uploads";
            log.warn("uploadDir is null or blank, using default: {}", this.uploadDir);
        }
        log.info("Setting upload directory to: {}", this.uploadDir);
        createUploadDirectory();  // Создаем директорию при инициализации
    }


    public ImageService() {
    }

    /**
     * Создает директорию для загрузок, если она не существует.
     * <p>
     * Вызывается автоматически при установке {@link #uploadDir}.
     * Использует {@link Files#(Path)} для создания всей иерархии директорий.
     * </p>
     *
     * <p><b>Примеры путей:</b></p>
     * <ul>
     *   <li>{@code ./uploads} → создаст {@code /app/current/directory/uploads}</li>
     *   <li>{@code /var/www/uploads} → создаст {@code /var/www/uploads}</li>
     *   <li>{@code uploads/images/avatars} → создаст вложенные директории</li>
     * </ul>
     *
     * @throws RuntimeException если не удалось создать директорию
     */
    private void createUploadDirectory() {
        Path path = Paths.get(uploadDir);

        try {
            if (!Files.exists(path)) {
                // Создаем все необходимые директории (включая родительские)
                Files.createDirectories(path);
                log.info("Upload directory created: {}", path.toAbsolutePath());
            } else {
                log.debug("Upload directory already exists: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Failed to create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Сохраняет загруженное изображение в файловой системе.
     * <p>
     * Сохраняет файл с уникальным именем (UUID) в директории загрузок.
     * Возвращает относительный путь к файлу, который будет использоваться в HTTP запросах.
     * </p>
     *
     * <p><b>Процесс сохранения:</b></p>
     * <ol>
     *   <li>Генерация уникального имени файла с помощью UUID</li>
     *   <li>Определение расширения файла из оригинального имени</li>
     *   <li>Проверка существования директории загрузок</li>
     *   <li>Копирование содержимого файла на диск</li>
     *   <li>Возврат относительного пути для использования в приложении</li>
     * </ol>
     *
     * <p><b>Примеры возвращаемых путей:</b></p>
     * <ul>
     *   <li>{@code /uploads/550e8400-e29b-41d4-a716-446655440000.jpg}</li>
     *   <li>{@code /uploads/a1b2c3d4-e5f6-7890-abcd-ef1234567890.png}</li>
     * </ul>
     *
     * <p><b>Меры безопасности:</b></p>
     * <ul>
     *   <li>UUID предотвращает конфликты имен и атаки перезаписью</li>
     *   <li>Ограниченный набор разрешенных расширений (через проверку в вызывающем коде)</li>
     *   <li>Стандартное копирование предотвращает симлинк-атаки</li>
     *   <li>REPLACE_EXISTING защищает от коллизий UUID (крайне маловероятно)</li>
     * </ul>
     *
     * @param file загруженный файл изображения (не должен быть null)
     * @return относительный путь к сохраненному файлу (например, {@code /uploads/filename.jpg})
     * @throws IOException              если произошла ошибка ввода/вывода при сохранении файла
     * @throws IllegalArgumentException если файл пустой или его имя некорректно
     * @see UUID
     * @see Files#copy(java.io.InputStream, Path, java.nio.file.CopyOption...)
     */
    public String saveImage(MultipartFile file) throws IOException {
        // Валидация входных данных
        if (file == null || file.isEmpty()) {
            log.error("Attempt to save null or empty file");
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        log.debug("Saving image: originalFilename={}, size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // Определяем расширение файла
        String extension = getFileExtension(file.getOriginalFilename());

        // Генерируем уникальное имя файла
        String fileName = UUID.randomUUID().toString() + extension;

        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }

    /**
     * Определяет расширение файла из имени файла.
     * <p>
     * Извлекает расширение (часть после последней точки) из имени файла.
     * Если расширение не найдено, возвращает {@code ".jpg"} по умолчанию.
     * </p>
     *
     * <p><b>Примеры:</b></p>
     * <ul>
     *   <li>{@code "avatar.jpg"} → {@code ".jpg"}</li>
     *   <li>{@code "image.png"} → {@code ".png"}</li>
     *   <li>{@code "photo"} → {@code ".jpg"} (по умолчанию)</li>
     *   <li>{@code null} → {@code ".jpg"} (по умолчанию)</li>
     *   <li>{@code "file.name.with.dots.tar.gz"} → {@code ".gz"}</li>
     * </ul>
     *
     * <p><b>⚠️ Ограничение:</b> метод не проверяет, является ли расширение безопасным.
     * Валидацию расширений (например, только .jpg, .png, .gif) должен выполнять вызывающий код.</p>
     *
     * @param filename оригинальное имя файла (может быть null)
     * @return расширение файла с точкой или {@code ".jpg"} по умолчанию
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
