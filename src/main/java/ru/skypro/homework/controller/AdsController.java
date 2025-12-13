package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdsService;

import java.io.IOException;
import java.util.Collections;

/**
 * Контроллер для управления объявлениями (Ads).
 * <p>
 * Предоставляет полный CRUD API для работы с объявлениями:
 * создание, чтение, обновление, удаление, а также управление изображениями.
 * </p>
 *
 * <p><b>Роли и доступ:</b></p>
 * <ul>
 *   <li>Публичные эндпоинты: {@code GET /ads}, {@code GET /ads/{id}}</li>
 *   <li>Защищенные эндпоинты: все остальные (требуют аутентификации)</li>
 *   <li>Пользователь может редактировать/удалять только свои объявления</li>
 *   <li>Администратор может редактировать/удалять любые объявления</li>
 * </ul>
 *
 * <p><b>Базовый URL:</b> {@code /ads}</p>
 *
 * <p><b>Схема работы:</b></p>
 * <pre>
 * ┌─────────┐     CRUD операции     ┌─────────────┐
 * │ Клиент  │ ────────────────────> │ AdsController │
 * └─────────┘                       └─────────────┘
 *         │     + изображения          │
 *         │ <──────────────────────────│
 *         │                            │ ──────┐
 *         │     JSON ответы            │      │ Вызов AdsService
 *         │                            │ <─────┘
 *         └────────────────────────────┘
 * </pre>
 *
 * @see AdsService
 * @see AdDTO
 * @see FullAd
 * @see AdsDTO
 * @see AdForUpdate
 */
@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // Разрешить CORS запросы с фронтенда
@RestController // REST контроллер Spring MVC
@RequiredArgsConstructor  // Lombok: автоматически создает конструктор для final полей
@RequestMapping("/ads") // Базовый путь для всех эндпоинтов контроллера
@Tag(name = "Объявления") // заголовок
public class AdsController {

    /**
     * Сервис для бизнес-логики работы с объявлениями.
     */
    private final AdsService adsService;

    /**
     * Получение списка всех объявлений (публичный доступ).
     * <p>
     * Возвращает пагинированный список всех активных объявлений в системе.
     * Включает базовую информацию без деталей автора.
     * </p>
     *
     * <p><b>Структура ответа:</b></p>
     * <pre>
     * {
     *   "count": 25,
     *   "results": [
     *     {
     *       "authorId": 123,
     *       "image": "/uploads/ads/1.jpg",
     *       "pk": 1,
     *       "price": 1000,
     *       "title": "Ноутбук"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return ResponseEntity с DTO списка объявлений
     */
    @GetMapping
    @Operation(summary = "Получение всех объявлений")
    public ResponseEntity<AdsDTO> GetAds() {
        return ResponseEntity.ok(adsService.getAll());
    }

    /**
     * Создание нового объявления (требуется аутентификация).
     * <p>
     * Создает новое объявление с заголовком, описанием, ценой и изображением.
     * Автор определяется автоматически из контекста безопасности.
     * </p>
     *
     * <p><b>Требования к данным:</b></p>
     * <ul>
     *   <li>Заголовок: не пустой, 5-100 символов</li>
     *   <li>Описание: не пустое, до 1000 символов</li>
     *   <li>Цена: положительное число</li>
     *   <li>Изображение: JPEG/PNG, максимум 10MB</li>
     * </ul>
     *
     * <p><b>Пример запроса (multipart/form-data):</b></p>
     * <pre>
     * POST /ads HTTP/1.1
     * Authorization: Basic dXNlcjpwYXNzd29yZA==
     * Content-Type: multipart/form-data; boundary=boundary
     *
     * --boundary
     * Content-Disposition: form-data; name="properties"
     * Content-Type: application/json
     *
     * {
     *   "title": "Ноутбук ASUS",
     *   "price": 45000,
     *   "description": "Отличное состояние, 2022 год"
     * }
     * --boundary
     * Content-Disposition: form-data; name="image"; filename="laptop.jpg"
     * Content-Type: image/jpeg
     *
     * [бинарные данные изображения]
     * --boundary--
     * </pre>
     *
     * @param properties DTO с данными объявления
     * @param image      файл изображения
     * @return ResponseEntity с созданным объявлением
     * @throws IOException если произошла ошибка при сохранении изображения
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавление объявления")
    public ResponseEntity<AdDTO> setAd(@RequestPart(name = "properties", required = true) AdForUpdate properties,
                                       @RequestPart(name = "image", required = true) MultipartFile image) throws IOException {
        // Проверка 1: Входные параметры
        if (properties == null) {
            log.error("Properties is null");
            return ResponseEntity.badRequest().build();
        }
        // Проверка 2: Сервис
        if (adsService == null) {
            log.error("adsService is null - not injected!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        // Валидация полей
        if (properties.getTitle() == null || properties.getTitle().isBlank() ||
                properties.getPrice() == null ||
                properties.getDescription() == null || properties.getDescription().isBlank()) {
            log.error("Validation failed: title={}, price={}, description={}",
                    properties.getTitle(), properties.getPrice(), properties.getDescription());
            return ResponseEntity.badRequest().build();
        }
        if (image == null || image.isEmpty()) {
            log.error("Image is null or empty");
            return ResponseEntity.badRequest().build();
        }
        log.info("Creating ad: title={}, price={}", properties.getTitle(), properties.getPrice());

        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            log.info("Current method: {}", stackTrace[1].getMethodName());
            AdDTO dto = adsService.createAds(properties, image);
            return ResponseEntity.status(201).body(dto);
        } catch (IOException e) {
            log.error("Internal Server Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение полной информации об объявлении по ID (публичный доступ).
     * <p>
     * Возвращает детальную информацию об объявлении, включая данные автора.
     * </p>
     *
     * <p><b>Пример ответа:</b></p>
     * <pre>
     * {
     *   "pk": 1,
     *   "authorFirstName": "John",
     *   "authorLastName": "Doe",
     *   "description": "Отличное состояние, 2022 год",
     *   "email": "john.doe@example.com",
     *   "image": "/uploads/ads/1.jpg",
     *   "phone": "+79991234567",
     *   "price": 45000,
     *   "title": "Ноутбук ASUS"
     * }
     * </pre>
     *
     * @param id идентификатор объявления
     * @return ResponseEntity с полной информацией об объявлении
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получение информации об объявлении")
    public ResponseEntity<FullAd> getAd(@PathVariable Long id) {
        FullAd adDTO = adsService.getFullAd(id);
        if (adDTO == null) {
            log.error("adsService is null - not injected!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(adDTO);
    }

    /**
     * Удаление объявления по ID.
     * <p>
     * Удаляет объявление. Доступно только автору объявления или администратору.
     * </p>
     *
     * @param id идентификатор объявления
     * @return ResponseEntity без тела (204 No Content при успехе)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление объявления")
    public ResponseEntity<?> dellAd(@PathVariable Long id) {
        try {
            adsService.deleteAd(id);
            return ResponseEntity.status(204).build();
        } catch (NotFoundException e) {
            log.error("Ad not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            log.error("User is not ADMIN", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Обновление информации об объявлении.
     * <p>
     * Обновляет заголовок, описание и цену объявления.
     * Доступно только автору объявления или администратору.
     * </p>
     *
     * @param id          идентификатор объявления
     * @param adForUpdate DTO с обновленными данными
     * @return ResponseEntity с обновленным объявлением
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Обновление информации об объявлении")
    public ResponseEntity<AdDTO> updateAd(@PathVariable("id") Long id,
                                          @RequestBody AdForUpdate adForUpdate) {
        try {
            AdDTO adDTO = adsService.updateAd(id, adForUpdate);
            return ResponseEntity.ok().body(adDTO);
        } catch (NotFoundException e) {
            log.error("Ad not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            log.error("User is not ADMIN", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Получение объявлений текущего пользователя.
     * <p>
     * Возвращает список всех объявлений, созданных авторизованным пользователем.
     * </p>
     *
     * @return ResponseEntity со списком объявлений пользователя
     */
    @GetMapping("/me")
    @Operation(summary = "Получение объявлений авторизованного пользователя")
    public ResponseEntity<AdsDTO> getAds() {
        AdsDTO adsDTO = adsService.getAllAdsByUser();
        if (adsDTO == null) {
            log.info("No ads found for current user, returning empty DTO");
            AdsDTO emptyDTO = new AdsDTO();
            emptyDTO.setCount(0);
            emptyDTO.setResults(Collections.emptyList());
            return ResponseEntity.ok(emptyDTO);
        }
        // Обрабатываем изображения
        if (adsDTO.getResults() != null) {
            adsDTO.getResults().forEach(ad -> {
                if (ad.getImage() != null && !ad.getImage().isEmpty()) {
                    // Добавляем timestamp для предотвращения кэширования
                    ad.setImage(ad.getImage() + "?t=" + System.currentTimeMillis());
                }
            });
        }
        return ResponseEntity.ok(adsService.getAllAdsByUser());
    }

    /**
     * Обновление изображения объявления.
     * <p>
     * Заменяет изображение существующего объявления.
     * Доступно только автору объявления или администратору.
     * </p>
     *
     * @param id    идентификатор объявления
     * @param image новое изображение
     * @return ResponseEntity с именем файла
     * @throws IOException если произошла ошибка при сохранении изображения
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновление картинки объявления")
    public ResponseEntity<String> UpdateImageAd(@PathVariable("id") Long id,
                                                @RequestPart("image") MultipartFile image) {
        log.info("Updating image for ad with id: {}", id);
        try {
            adsService.uppdateImageOfAd(id, image);
            return ResponseEntity.ok(image.getOriginalFilename());
        } catch (IOException e) {
            log.error("Internal Server Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (NotFoundException e) {
            log.error("Ad not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            log.error("User is not ADMIN", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
