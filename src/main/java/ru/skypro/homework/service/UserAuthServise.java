package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Comments;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentsRepository;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.mapped.AdsMapper;

import java.util.Optional;

/**
 * Сервис для работы с аутентификацией и авторизацией пользователей.
 * <p>
 * Предоставляет методы для получения информации о текущем пользователе
 * и проверки прав доступа к ресурсам (объявлениям, комментариям).
 * Интегрируется с Spring Security для управления доступом на основе ролей.
 * </p>
 *
 * <p><b>Основные функции:</b></p>
 * <ul>
 *   <li>Получение текущего аутентифицированного пользователя</li>
 *   <li>Определение авторов объявлений и комментариев</li>
 *   <li>Проверка прав доступа к ресурсам</li>
 *   <li>Интеграция с контекстом безопасности Spring</li>
 * </ul>
 *
 * <p><b>Архитектурная роль:</b></p>
 * <ul>
 *   <li><b>Вспомогательный сервис:</b> используется другими сервисами для проверки прав доступа</li>
 *   <li><b>Абстракция безопасности:</b> инкапсулирует логику работы с Spring Security</li>
 *   <li><b>Cross-cutting concern:</b> пересекающаяся функциональность для всех бизнес-сервисов</li>
 * </ul>
 *
 * <p><b>Используется в:</b></p>
 * <ul>
 *   <li>{@link UserService} - для определения текущего пользователя</li>
 *   <li>{@link AdsService} - для проверки прав на объявления</li>
 *   <li>{@link CommentService} - для проверки прав на комментарии</li>
 *   <li>Контроллеры - для предварительных проверок доступа</li>
 * </ul>
 *
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @see org.springframework.security.core.Authentication
 */
@Service
@RequiredArgsConstructor
@Transactional  // Все методы выполняются в транзакции
@Slf4j
public class UserAuthServise {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UsersRepository usersRepository;

    /**
     * Репозиторий для работы с объявлениями.
     */
    private final AdsRepository adsRepository;

    /**
     * Маппер для преобразования объявлений (используется для доступа к данным автора).
     * <p>
     * ⚠️ <b>Заметка:</b> В текущей реализации AdsMapper не используется напрямую в методах,
     * но dependency injection сохраняется для совместимости.
     * </p>
     */
    private final AdsMapper adsMapper;

    /**
     * Репозиторий для работы с комментариями.
     */
    private final CommentsRepository commentsRepository;

    /**
     * Получает текущего аутентифицированного пользователя.
     * <p>
     * Извлекает информацию о пользователе из контекста безопасности Spring Security.
     * Используется во всех сервисах, где необходимо знать текущего пользователя.
     * </p>
     *
     * <p><b>Процесс получения:</b></p>
     * <ol>
     *   <li>Получение {@link Authentication} из {@link SecurityContextHolder}</li>
     *   <li>Проверка, что пользователь аутентифицирован</li>
     *   <li>Извлечение username из объекта аутентификации</li>
     *   <li>Фильтрация анонимных пользователей ("anonymousUser")</li>
     *   <li>Поиск пользователя в базе данных по username</li>
     *   <li>Возврат сущности {@link Users}</li>
     * </ol>
     *
     * <p><b>Сценарии ошибок:</b></p>
     * <table border="1">
     *   <tr><th>Сценарий</th><th>Исключение</th><th>Описание</th></tr>
     *   <tr><td>Пользователь не аутентифицирован</td><td>RuntimeException</td><td>SecurityContext пуст</td></tr>
     *   <tr><td>Анонимный доступ</td><td>RuntimeException</td><td>username = "anonymousUser"</td></tr>
     *   <tr><td>Пользователь не найден в БД</td><td>RuntimeException</td><td>Нет записи в users таблице</td></tr>
     * </table>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В UserService.getUsers()
     * Users currentUser = userAuthService.getCurrentUser();
     * return usersMapper.toUsersDTO(currentUser);
     *
     * // В AdsService.createAds()
     * Users author = userAuthService.getCurrentUser();
     * ad.setAuthor(author);
     * </pre>
     *
     * @return сущность текущего аутентифицированного пользователя
     * @throws RuntimeException если пользователь не аутентифицирован или не найден
     * @see SecurityContextHolder
     * @see Authentication
     */
    public Users getCurrentUser() {

        // Получаем контекст безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Проверяем аутентификацию
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        // Извлекаем username
        String username = authentication.getName();

        // Проверяем на анонимного пользователя
        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("User is not authenticated");
        }

        // Ищем пользователя в базе данных
        Optional<Users> users = usersRepository.findByUsername(authentication.getName());
        return users.orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получает идентификатор автора объявления по ID объявления.
     * <p>
     * Используется для проверки прав доступа: сравнивает ID автора объявления
     * с ID текущего пользователя, чтобы определить, может ли пользователь
     * редактировать или удалять объявление.
     * </p>
     *
     * <p><b>Используется для:</b></p>
     * <ul>
     *   <li>Проверки, является ли текущий пользователь автором объявления</li>
     *   <li>Определения прав на редактирование/удаление</li>
     *   <li>Валидации операций с объявлениями</li>
     * </ul>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // Проверка прав доступа
     * Long adAuthorId = userAuthService.getAdsAuthorByPk(adId);
     * Long currentUserId = userAuthService.getCurrentUser().getId();
     *
     * if (!adAuthorId.equals(currentUserId) && !user.isAdmin()) {
     *     throw new AccessDeniedException("Only author or admin can modify ad");
     * }
     * </pre>
     *
     * @param id идентификатор объявления (pk)
     * @return идентификатор автора объявления
     * @throws NotFoundException     если объявление не найдено
     * @throws IllegalStateException если AdsMapper не инициализирован (ошибка конфигурации)
     * @see ru.skypro.homework.service.AdsService#(Long)
     * @see ru.skypro.homework.service.AdsService#deleteAd(Long)
     */
    public Long getAdsAuthorByPk(Long id) {
        log.debug("Getting author for ad id: {}", id);

        // Находим объявление
        Ads ads = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        return ads.getAuthor().getId();
    }

    /**
     * Получает идентификатор автора комментария по ID комментария.
     * <p>
     * Используется для проверки прав доступа к комментариям.
     * Определяет, может ли текущий пользователь редактировать или удалять комментарий.
     * </p>
     *
     * <p><b>Особенности:</b></p>
     * <ul>
     *   <li>Проверяет существование комментария</li>
     *   <li>Возвращает ID пользователя-автора</li>
     *   <li>Используется вместе с {@link #getCurrentUser()} для проверки прав</li>
     * </ul>
     *
     * <p><b>⚠️ Заметка о сообщении об ошибке:</b></p>
     * <p>В исключении указано "Ad not found with id", что неверно - должно быть "Comment not found".
     * Это опечатка в исходном коде.</p>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>
     * // В CommentService.deleteComment()
     * Long commentAuthorId = userAuthService.getCommentsAuthorByPk(commentId);
     * Long currentUserId = userAuthService.getCurrentUser().getId();
     *
     * if (!commentAuthorId.equals(currentUserId) && !currentUser.isAdmin()) {
     *     throw new AccessDeniedException("Only author or admin can delete comment");
     * }
     * </pre>
     *
     * @param id идентификатор комментария (pk)
     * @return идентификатор автора комментария
     * @throws NotFoundException     если комментарий не найден (с некорректным сообщением)
     * @throws IllegalStateException если AdsMapper не инициализирован
     * @see ru.skypro.homework.service.CommentService#deleteComment(Long, Long)
     * @see ru.skypro.homework.service.CommentService#updateComment(Long, Long, String)
     */
    public Long getCommentsAuthorByPk(Long id) {
        log.debug("Getting author for comment id: {}", id);

        // Находим комментарий
        Comments comments = commentsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Comment not found with id: " + id));
        return comments.getAuthor().getId();
    }
}
