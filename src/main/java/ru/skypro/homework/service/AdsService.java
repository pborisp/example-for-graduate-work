package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.AdForUpdate;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.FullAd;

import java.io.IOException;

/**
 * Сервис для управления объявлениями (Ads).
 * <p>
 * Определяет контракт для полного цикла работы с объявлениями:
 * создание, чтение, обновление, удаление (CRUD), а также управление изображениями.
 * Содержит бизнес-логику, связанную с объявлениями, включая проверку прав доступа
 * и обработку файлов.
 * </p>
 *
 * <p><b>Архитектурная роль:</b></p>
 * <ul>
 *   <li><b>Слой сервисов:</b> содержит бизнес-логику работы с объявлениями</li>
 *   <li><b>Абстракция:</b> отделяет контроллеры от репозиториев и мапперов</li>
 *   <li><b>Безопасность:</b> включает проверку прав доступа (только автор или админ)</li>
 *   <li><b>Транзакционность:</b> методы должны выполняться в транзакциях</li>
 * </ul>
 *
 * <p><b>Используемые DTO:</b></p>
 * <table border="1">
 *   <tr><th>DTO</th><th>Назначение</th></tr>
 *   <tr><td>{@link AdsDTO}</td><td>Список объявлений с пагинацией</td></tr>
 *   <tr><td>{@link AdDTO}</td><td>Краткая информация об объявлении (для списков)</td></tr>
 *   <tr><td>{@link FullAd}</td><td>Полная информация об объявлении (детальная страница)</td></tr>
 *   <tr><td>{@link AdForUpdate}</td><td>Данные для создания/обновления объявления</td></tr>
 * </table>
 *
 * @see ru.skypro.homework.service.impl.AdsServiceImpl
 * @see ru.skypro.homework.controller.AdsController
 * @see ru.skypro.homework.repository
 */
public interface AdsService {

    /**
     * Получает все активные объявления в системе (публичный доступ).
     * <p>
     * Возвращает пагинированный список всех объявлений, отсортированных
     * по дате создания (новые сначала). Включает только базовую информацию,
     * достаточную для отображения в списке.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code GET /ads}</p>
     *
     * <p><b>Возвращаемые данные:</b></p>
     * <ul>
     *   <li>Общее количество объявлений</li>
     *   <li>Список объявлений с полями: pk, image, price, title</li>
     *   <li>Не включает: описание, данные автора, комментарии</li>
     * </ul>
     *
     * <p><b>Параметры пагинации (рекомендуется добавить):</b></p>
     * <pre>
     * // В будущем можно добавить:
     * AdsDTO getAll(int page, int size, String sortBy, String sortDirection);
     * </pre>
     *
     * @return DTO со списком всех объявлений
     * @see ru.skypro.homework.controller.AdsController#getAds()
     */
    AdsDTO getAll();

    /**
     * Создает новое объявление для текущего пользователя.
     * <p>
     * Автор объявления определяется автоматически из контекста безопасности.
     * Изображение сохраняется в файловую систему, путь сохраняется в БД.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code POST /ads}</p>
     *
     * <p><b>Процесс создания:</b></p>
     * <ol>
     *   <li>Валидация входных данных (title, price, description)</li>
     *   <li>Определение текущего пользователя как автора</li>
     *   <li>Сохранение изображения в директорию загрузок</li>
     *   <li>Создание сущности {@link ru.skypro.homework.model.Ads}</li>
     *   <li>Установка пути к изображению</li>
     *   <li>Сохранение в базу данных</li>
     *   <li>Возврат DTO созданного объявления</li>
     * </ol>
     *
     * <p><b>Требования к данным:</b></p>
     * <table border="1">
     *   <tr><th>Параметр</th><th>Требования</th></tr>
     *   <tr><td>adForUpdate.title</td><td>Не null, 5-100 символов</td></tr>
     *   <tr><td>adForUpdate.price</td><td>Не null, ≥ 0</td></tr>
     *   <tr><td>adForUpdate.description</td><td>Не null, 10-1000 символов</td></tr>
     *   <tr><td>image</td><td>Не null, ≤10MB, JPEG/PNG/GIF</td></tr>
     * </table>
     *
     * @param adForUpdate DTO с данными объявления (title, price, description)
     * @param image       файл изображения для объявления
     * @return DTO созданного объявления
     * @throws IOException                                               если произошла ошибка при сохранении изображения
     * @throws IllegalArgumentException                                  если данные не проходят валидацию
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @see ru.skypro.homework.controller.AdsController#(AdForUpdate, MultipartFile)
     */
    AdDTO createAds(AdForUpdate adForUpdate, MultipartFile image) throws IOException;

    /**
     * Получает полную информацию об объявлении по ID (публичный доступ).
     * <p>
     * Возвращает детальную информацию, включая данные автора.
     * Используется для отображения страницы объявления.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code GET /ads/{id}}</p>
     *
     * <p><b>Возвращаемые данные:</b></p>
     * <table border="1">
     *   <tr><th>Поле</th><th>Источник</th><th>Описание</th></tr>
     *   <tr><td>pk</td><td>Ads.pk</td><td>ID объявления</td></tr>
     *   <tr><td>title</td><td>Ads.title</td><td>Заголовок</td></tr>
     *   <tr><td>price</td><td>Ads.price</td><td>Цена</td></tr>
     *   <tr><td>description</td><td>Ads.description</td><td>Описание</td></tr>
     *   <tr><td>image</td><td>Ads.image</td><td>URL изображения</td></tr>
     *   <tr><td>authorFirstName</td><td>Ads.author.firstName</td><td>Имя автора</td></tr>
     *   <tr><td>authorLastName</td><td>Ads.author.lastName</td><td>Фамилия автора</td></tr>
     *   <tr><td>email</td><td>Ads.author.username</td><td>Email автора</td></tr>
     *   <tr><td>phone</td><td>Ads.author.phone</td><td>Телефон автора</td></tr>
     * </table>
     *
     * @param id идентификатор объявления
     * @return полная информация об объявлении или null, если не найдено
     * @see ru.skypro.homework.controller.AdsController#getAd(Long)
     */
    FullAd getFullAd(Long id);

    /**
     * Обновляет изображение существующего объявления.
     * <p>
     * Заменяет текущее изображение объявления на новое.
     * Старое изображение удаляется из файловой системы.
     * Доступно только автору объявления или администратору.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code POST /ads/{id}/image}</p>
     *
     * <p><b>Процесс обновления:</b></p>
     * <ol>
     *   <li>Поиск объявления по ID</li>
     *   <li>Проверка прав доступа (автор или админ)</li>
     *   <li>Валидация нового изображения</li>
     *   <li>Удаление старого файла изображения</li>
     *   <li>Сохранение нового изображения</li>
     *   <li>Обновление пути к изображению в БД</li>
     * </ol>
     *
     * <p><b>⚠️ Внимание на опечатку:</b> метод назван {@code uppdateImageOfAd} вместо {@code updateImageOfAd}</p>
     *
     * @param id   идентификатор объявления
     * @param file новое изображение для объявления
     * @throws IOException                                               если произошла ошибка при работе с файлами
     * @throws org.webjars.NotFoundException                             если объявление не найдено
     * @throws org.springframework.security.access.AccessDeniedException если нет прав на обновление
     * @see ru.skypro.homework.controller.AdsController#(Long, MultipartFile)
     */
    void uppdateImageOfAd(Long id, MultipartFile file) throws IOException;

    /**
     * Удаляет объявление по ID.
     * <p>
     * Удаляет объявление, все связанные комментарии и изображение из файловой системы.
     * Доступно только автору объявления или администратору.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code DELETE /ads/{id}}</p>
     *
     * <p><b>Процесс удаления:</b></p>
     * <ol>
     *   <li>Поиск объявления по ID</li>
     *   <li>Проверка прав доступа (автор или админ)</li>
     *   <li>Удаление файла изображения из файловой системы</li>
     *   <li>Каскадное удаление всех комментариев (настроено в JPA)</li>
     *   <li>Удаление объявления из базы данных</li>
     * </ol>
     *
     * @param id идентификатор объявления
     * @throws org.webjars.NotFoundException                             если объявление не найдено
     * @throws org.springframework.security.access.AccessDeniedException если нет прав на удаление
     * @see ru.skypro.homework.controller.AdsController#(Long)
     */
    void deleteAd(Long id);

    /**
     * Обновляет информацию об объявлении (заголовок, описание, цену).
     * <p>
     * Обновляет текстовые данные объявления без изменения изображения.
     * Доступно только автору объявления или администратору.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code PATCH /ads/{id}}</p>
     *
     * <p><b>Обновляемые поля:</b></p>
     * <table border="1">
     *   <tr><th>Поле</th><th>Обновляется если</th></tr>
     *   <tr><td>title</td><td>Не null и не пустое</td></tr>
     *   <tr><td>description</td><td>Не null и не пустое</td></tr>
     *   <tr><td>price</td><td>Не null и ≥ 0</td></tr>
     * </table>
     *
     * <p><b>НЕ обновляются:</b></p>
     * <ul>
     *   <li>Изображение (используйте {@link #uppdateImageOfAd})</li>
     *   <li>Автор (нельзя передать объявление другому пользователю)</li>
     *   <li>Дата создания</li>
     * </ul>
     *
     * @param id          идентификатор объявления
     * @param adForUpdate DTO с новыми данными
     * @return обновленное объявление в формате DTO
     * @throws org.webjars.NotFoundException                             если объявление не найдено
     * @throws IllegalArgumentException                                  если данные невалидны
     * @throws org.springframework.security.access.AccessDeniedException если нет прав на обновление
     * @see ru.skypro.homework.controller.AdsController#updateAd(Long, AdForUpdate)
     */
    AdDTO updateAd(Long id, AdForUpdate adForUpdate);

    /**
     * Получает все объявления текущего пользователя.
     * <p>
     * Определяет текущего пользователя из контекста безопасности
     * и возвращает все созданные им объявления.
     * </p>
     *
     * <p><b>Используется в эндпоинте:</b> {@code GET /ads/me}</p>
     *
     * <p><b>Особенности:</b></p>
     * <ul>
     *   <li>Возвращает только объявления текущего пользователя</li>
     *   <li>Включает скрытые/неактивные объявления (если такие есть)</li>
     *   <li>Формат ответа такой же как у {@link #getAll()}</li>
     * </ul>
     *
     * @return DTO со списком объявлений текущего пользователя
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не аутентифицирован
     * @see ru.skypro.homework.controller.AdsController#()
     */
    AdsDTO getAllAdsByUser();
}
