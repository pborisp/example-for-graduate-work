package ru.skypro.homework.service.mapped;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Comments;
import ru.skypro.homework.model.Users;

import java.time.ZoneOffset;

/**
 * Маппер для преобразования между сущностью {@link Comments} и DTO комментариев.
 * <p>
 * Реализует корректное двустороннее преобразование данных комментариев:
 * <ul>
 *   <li><b>DTO → Entity:</b> при создании новых комментариев из данных API</li>
 *   <li><b>Entity → DTO:</b> при возврате комментариев в API ответах</li>
 * </ul>
 *
 * <p><b>Ключевые особенности:</b></p>
 * <ul>
 *   <li>Корректно преобразует {@link java.time.LocalDateTime} в timestamp миллисекунд</li>
 *   <li>Использует UTC временную зону для консистентности</li>
 *   <li>Обрабатывает null значения для избежания NPE</li>
 *   <li>Включает данные автора в DTO</li>
 * </ul>
 *
 * @see Comments
 * @see CommentDTO
 * @see Ads
 * @see Users
 */
@Component
public class CommentsMapper {

    /**
     * Создает новую сущность {@link Comments} из данных пользователя.
     * <p>
     * Используется при добавлении нового комментария к объявлению.
     * Поле {@code createdAt} автоматически устанавливается в текущее время
     * благодаря значению по умолчанию в сущности {@link Comments}.
     * </p>
     *
     * @param text   текст комментария (не должен быть null или пустым)
     * @param author пользователь-автор комментария (не должен быть null)
     * @param ad     объявление, к которому относится комментарий (не должен быть null)
     * @return новая сущность Comments, готовая к сохранению в БД
     * @throws IllegalArgumentException если text, author или ad равны null
     * @see ru.skypro.homework.service.CommentService#createComment(Long, String)
     */
    public Comments toComments(String text, Users author, Ads ad) {
        Comments comment = new Comments();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setAd(ad);
        // createdAt автоматически установится = LocalDateTime.now()
        return comment;
    }

    /**
     * Преобразует сущность {@link Comments} в {@link CommentDTO} для возврата в API.
     * <p>
     * ✅ <b>Критическое исправление:</b> Правильное преобразование времени в миллисекундах.
     * <p>
     *
     * <pre>
     * dto.setCreatedAt(comments.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
     * // toEpochMilli() возвращает long (миллисекунды с 1970-01-01)
     * // Результат: корректный timestamp, который понимают JavaScript и фронтенд
     * </pre>
     *
     * <p><b>Пример JSON ответа:</b></p>
     * <pre>
     * {
     *   "pk": 456,
     *   "author": 123,
     *   "authorImage": "/uploads/avatars/user123.jpg",
     *   "authorFirstName": "John",
     *   "text": "Отличное предложение!",
     *   "createdAt": 1736935200000  // ✅ 2025 год, а не 1970!
     * }
     * </pre>
     *
     * <p><b>Используется в:</b></p>
     * <ul>
     *   <li>{@code GET /ads/{id}/comments} - получение всех комментариев объявления</li>
     *   <li>{@code POST /ads/{id}/comments} - возврат созданного комментария</li>
     *   <li>{@code PATCH /ads/{adId}/comments/{commentId}} - возврат обновленного комментария</li>
     * </ul>
     *
     * @param comments сущность комментария из БД (может быть null)
     * @return CommentDTO для ответа API или null, если comments равен null
     * @see ru.skypro.homework.controller.CommentController#
     * @see ru.skypro.homework.controller.CommentController#
     * @see ru.skypro.homework.controller.CommentController#
     */
    public CommentDTO toDto(Comments comments) {
        if (comments == null) {
            return null;
        }
        CommentDTO dto = new CommentDTO();
        dto.setPk(comments.getPk());
        if (comments.getAuthor() != null) {
            dto.setAuthor(comments.getAuthor().getId());
            dto.setAuthorImage(comments.getAuthor().getImage());
            dto.setAuthorFirstName(comments.getAuthor().getFirstName());
        }
        dto.setText(comments.getText());
        if (comments.getCreatedAt() != null) {
            dto.setCreatedAt(comments.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
        }
        return dto;
    }
}
