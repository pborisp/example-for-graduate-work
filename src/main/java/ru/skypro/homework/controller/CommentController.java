package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.*;

@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") //разрешить запросы с Fronted
@RestController //Контроллер
@RequiredArgsConstructor //автоматическое создание конструктора с полями final (lombok)
@RequestMapping("/ads") //путь к запросу
@Tag(name = "Комментарии") //заголовок
public class CommentController {
    @GetMapping("/{id}/comments") //Получение комментариев объявления
    @Operation(summary = "Получение комментариев объявления")
    public ResponseEntity<Comments> GetComments(@PathVariable Long id) {
        return ResponseEntity.ok(new Comments());
    }

    @PostMapping(value = "/{id}/comments") //Добавление комментария к объявлению
    @Operation(summary = "Добавление комментария к объявлению")
    public ResponseEntity<CommentDTO> setComment(@PathVariable Long id, @RequestPart String text) {
        return ResponseEntity.ok(new CommentDTO());
    }

    @DeleteMapping("/{adId}/comments/{commentId}") //Удаление комментария
    @Operation(summary = "Удаление комментария")
    public ResponseEntity<?> dellComment(@PathVariable("adId") Long adid, @PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{adId}/comments/{commentId}") //Обновление комментария
    @Operation(summary = "Обновление комментария")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable("adId") Long adid, @PathVariable("commentId") Long commentId, @RequestPart String text) {
        return ResponseEntity.ok(new CommentDTO());
    }


}
