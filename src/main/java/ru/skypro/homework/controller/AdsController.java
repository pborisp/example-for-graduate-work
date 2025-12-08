package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.service.AdsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // разрешить запросы с Fronted
@RestController // Контроллер
@RequiredArgsConstructor  // Автоматическое создание конструктора с полями final (lombok)
@RequestMapping("/ads") // путь запроса
@Tag(name = "Объявления") // заголовок
public class AdsController {
    private final AdsService adsService;

    @GetMapping
    @Operation(summary = "Получение всех объявлений")
    public ResponseEntity<AdsDTO> GetAds() {
        return ResponseEntity.ok(adsService.getAll());
    }

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

    @GetMapping("/me")
    @Operation(summary = "Получение объявлений авторизованного пользователя")
    public ResponseEntity<AdsDTO> getAds() {
        return ResponseEntity.ok(adsService.getAllAdsByUser());
    }

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
