package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.CommentService;

/**
 * Контроллер для управления комментариями к объявлениям.
 * <p>
 * Предоставляет REST API для работы с комментариями:
 * получение, создание, обновление и удаление комментариев.
 * Все операции привязаны к конкретному объявлению.
 * </p>
 *
 * <p><b>Иерархия доступа:</b></p>
 * <ul>
 *   <li><b>Чтение комментариев:</b> публичный доступ (GET /ads/{id}/comments)</li>
 *   <li><b>Создание комментария:</b> любой аутентифицированный пользователь</li>
 *   <li><b>Изменение комментария:</b> только автор комментария или администратор</li>
 *   <li><b>Удаление комментария:</b> только автор комментария или администратор</li>
 * </ul>
 *
 * <p><b>Базовый URL:</b> {@code /ads/{adId}/comments}</p>
 *
 * <p><b>Структура API:</b></p>
 * <pre>
 * GET    /ads/{adId}/comments                # Получить все комментарии объявления
 * POST   /ads/{adId}/comments                # Добавить комментарий
 * DELETE /ads/{adId}/comments/{commentId}    # Удалить комментарий
 * PATCH  /ads/{adId}/comments/{commentId}    # Обновить комментарий
 * </pre>
 *
 * @see CommentService
 * @see CommentDTO
 * @see CommentsList
 * @see CommentForAdd
 */
@Slf4j //Логирование
@CrossOrigin(value = "http://localhost:3000") // Разрешить CORS запросы с фронтенда
@RestController // REST контроллер Spring MVC
@RequiredArgsConstructor // Lombok: автоматически создает конструктор для final полей
@RequestMapping("/ads") // Базовый путь (наследуется от AdsController)
@Tag(name = "Комментарии") //заголовок
public class CommentController {

    /**
     * Сервис для бизнес-логики работы с комментариями.
     */
    private final CommentService commentService;

    /**
     * Получение всех комментариев объявления (публичный доступ).
     * <p>
     * Возвращает список всех комментариев для указанного объявления.
     * Комментарии отсортированы по дате создания (от новых к старым).
     * </p>
     *
     * <p><b>Структура ответа:</b></p>
     * <pre>
     * {
     *   "count": 5,
     *   "results": [
     *     {
     *       "author": 123,
     *       "authorImage": "/uploads/avatars/user123.jpg",
     *       "authorFirstName": "John",
     *       "createdAt": 1672531200000,
     *       "pk": 456,
     *       "text": "Отличное предложение!"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param id идентификатор объявления (adId)
     * @return ResponseEntity со списком комментариев
     */
    @GetMapping("/{id}/comments") //Получение комментариев объявления
    @Operation(summary = "Получение комментариев объявления")
    public ResponseEntity<CommentsList> GetCommentsByAds(@PathVariable Long id) {
        try {
            CommentsList commentsList = commentService.getCommentsByAds(id);
            return ResponseEntity.ok(commentsList);
        } catch (NotFoundException e) {
            log.error("commentService is null - not injected!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Добавление комментария к объявлению (требуется аутентификация).
     * <p>
     * Создает новый комментарий для указанного объявления.
     * Автор определяется автоматически из контекста безопасности.
     * </p>
     *
     * <p><b>Требования к комментарию:</b></p>
     * <ul>
     *   <li>Текст: не пустой, 1-1000 символов</li>
     *   <li>Объявление должно существовать</li>
     *   <li>Пользователь должен быть аутентифицирован</li>
     * </ul>
     *
     * <p><b>Пример запроса:</b></p>
     * <pre>
     * POST /ads/123/comments HTTP/1.1
     * Authorization: Basic dXNlcjpwYXNzd29yZA==
     * Content-Type: application/json
     *
     * {
     *   "text": "Отличное предложение! Интересует обмен?"
     * }
     * </pre>
     *
     * @param id            идентификатор объявления (adId)
     * @param commentForAdd DTO с текстом комментария
     * @return ResponseEntity с созданным комментарием
     */
    @PostMapping(value = "/{id}/comments") //Добавление комментария к объявлению
    @Operation(summary = "Добавление комментария к объявлению")
    public ResponseEntity<CommentDTO> setComment(@PathVariable("id") Long id, @RequestBody CommentForAdd commentForAdd) {
        try {
            String text = commentForAdd.getText();
            CommentDTO comments = commentService.createComment(id, text);
            return ResponseEntity.ok(comments);
        } catch (NotFoundException e) {
            log.error("commentService is null - not injected!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            log.error("User is not ADMIN", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    /**
     * Удаление комментария (только автор или администратор).
     * <p>
     * Удаляет комментарий по его ID. Комментарий должен принадлежать указанному объявлению.
     * </p>
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @return ResponseEntity без тела (204 No Content при успехе)
     */
    @DeleteMapping("/{adId}/comments/{commentId}") //Удаление комментария
    @Operation(summary = "Удаление комментария")
    public ResponseEntity<?> dellComment(@PathVariable("adId") Long adId, @PathVariable("commentId") Long commentId) {
        try {
            commentService.deleteComment(adId, commentId);
            return ResponseEntity.status(204).build();
        } catch (NotFoundException e) {
            log.error("Ad not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("User is not ADMIN", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Обновление комментария (только автор или администратор).
     * <p>
     * Обновляет текст существующего комментария.
     * Комментарий должен принадлежать указанному объявлению.
     * </p>
     *
     * @param adId          идентификатор объявления
     * @param commentId     идентификатор комментария
     * @param commentForAdd DTO с новым текстом комментария
     * @return ResponseEntity с обновленным комментарием
     */
    @PatchMapping("/{adId}/comments/{commentId}") //Обновление комментария
    @Operation(summary = "Обновление комментария")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable("adId") Long adId, @PathVariable("commentId") Long commentId, @RequestBody CommentForAdd commentForAdd) {
        try {
            String text = commentForAdd.getText();
            CommentDTO comments = commentService.updateComment(adId, commentId, text);
            return ResponseEntity.ok(comments);
        } catch (NotFoundException e) {
            log.error("Ad not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("Only ADMIN or comment author can edit comments", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Comment does not belong to this ad", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}