package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // разрешить запросы с Fronted
@RestController // Контроллер
@RequiredArgsConstructor  // Автоматическое создание конструктора с полями final (lombok)
@RequestMapping("/ads") // путь запроса
@Tag(name = "Объявления") // заголовок
public class AdsController {
    @GetMapping
    @Operation(summary = "Получение всех объявлений")
    public ResponseEntity<Ads> GetAds() {
        return ResponseEntity.ok(new Ads());
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Добавление объявления")
    public ResponseEntity<Ad> setAd(@RequestPart("properties") AdForUpdate properties, @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(new Ad());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации об объявлении")
    public ResponseEntity<FullAd> getAd(@PathVariable Long id) {
        return ResponseEntity.ok(new FullAd());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление объявления")
    public ResponseEntity<?> dellAd(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновление информации об объявлении")
    public ResponseEntity<AdForUpdate> updateAd(@PathVariable Long id) {
        return ResponseEntity.ok(new AdForUpdate());
    }

    @GetMapping("/me")
    @Operation(summary = "Получение объявлений авторизованного пользователя")
    public ResponseEntity<Ads> getAds() {
        return ResponseEntity.ok(new Ads());
    }

    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    @Operation(summary = "Обновление картинки объявления")
    public ResponseEntity<String> UpdateImageAd(@RequestPart("id") Long id, @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(new Ad().getImage());
    }
}
