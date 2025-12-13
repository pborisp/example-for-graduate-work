package ru.skypro.homework.service.mapped;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.AdForUpdate;
import ru.skypro.homework.dto.FullAd;
import ru.skypro.homework.model.Ads;

/**
 * Маппер для преобразования между сущностью {@link Ads} и DTO объявлений.
 * <p>
 * Обеспечивает преобразование данных объявлений между слоями приложения:
 * <ul>
 *   <li><b>Entity → AdDTO:</b> для списков объявлений (краткая информация)</li>
 *   <li><b>Entity → FullAd:</b> для детальной страницы объявления</li>
 *   <li><b>AdForUpdate → Entity:</b> для создания/обновления объявлений</li>
 * </ul>
 *
 * <p><b>Разница между DTO:</b></p>
 * <table border="1">
 *   <tr><th>DTO</th><th>Назначение</th><th>Поля</th></tr>
 *   <tr><td>{@link AdDTO}</td><td>Списки объявлений</td><td>id, image, price, title</td></tr>
 *   <tr><td>{@link FullAd}</td><td>Детальная страница</td><td>Все поля + автор</td></tr>
 *   <tr><td>{@link AdForUpdate}</td><td>Создание/обновление</td><td>title, price, description</td></tr>
 * </table>
 *
 * <p><b>Поток данных:</b></p>
 * <pre>
 * База данных → Ads → AdDTO/FullAd → API клиенту
 * API клиента → AdForUpdate → Ads → База данных
 * </pre>
 *
 * @see Ads
 * @see AdDTO
 * @see FullAd
 * @see AdForUpdate
 */
@Component
public class AdsMapper {

    /**
     * Преобразует сущность {@link Ads} в {@link AdDTO} для списков объявлений.
     * <p>
     * Используется в эндпоинтах:
     * <ul>
     *   <li>{@code GET /ads} - все объявления</li>
     *   <li>{@code GET /ads/me} - мои объявления</li>
     * </ul>
     * Возвращает краткую информацию об объявлении, достаточную для отображения в списке.
     * </p>
     *
     * <p><b>Маппинг полей:</b></p>
     * <table border="1">
     *   <tr><th>Ads поле</th><th>AdDTO поле</th><th>Описание</th></tr>
     *   <tr><td>pk</td><td>pk</td><td>Идентификатор объявления</td></tr>
     *   <tr><td>image</td><td>image</td><td>URL главного изображения</td></tr>
     *   <tr><td>price</td><td>price</td><td>Цена в рублях</td></tr>
     *   <tr><td>title</td><td>title</td><td>Заголовок объявления</td></tr>
     * </table>
     *
     * <p><b>Пример ответа API:</b></p>
     * <pre>
     * {
     *   "pk": 123,
     *   "image": "/uploads/ads/laptop.jpg",
     *   "price": 45000,
     *   "title": "Ноутбук ASUS"
     * }
     * </pre>
     *
     * @param ads сущность объявления (не должна быть null)
     * @return DTO для списка объявлений
     * @throws NullPointerException если ads равен null
     *
     * @see ru.skypro.homework.controller.AdsController#getAds()
     * @see ru.skypro.homework.controller.AdsController
     */
    public AdDTO adsToDto(Ads ads) {
        AdDTO dto = new AdDTO();
        dto.setPk(ads.getPk());
        dto.setImage(ads.getImage());
        dto.setPrice(ads.getPrice());
        dto.setTitle(ads.getTitle());
        return dto;
    }

    /**
     * Преобразует сущность {@link Ads} в {@link FullAd} для детальной страницы.
     * <p>
     * Используется в эндпоинте {@code GET /ads/{id}}.
     * Возвращает полную информацию об объявлении, включая данные автора.
     * </p>
     *
     * <p><b>Пример ответа API:</b></p>
     * <pre>
     * {
     *   "pk": 123,
     *   "image": "/uploads/ads/laptop.jpg",
     *   "price": 45000,
     *   "title": "Ноутбук ASUS",
     *   "description": "Отличное состояние, 2022 год",
     *   "authorFirstName": "John",
     *   "authorLastName": "Doe",
     *   "email": "john.doe@example.com",
     *   "phone": "+79991234567"
     * }
     * </pre>
     *
     * @param ads сущность объявления (не должна быть null, должен быть автор)
     * @return DTO с полной информацией об объявлении
     * @throws NullPointerException если ads или ads.getAuthor() равен null
     *
     * @see ru.skypro.homework.controller.AdsController#getAd(Long)
     */
    public FullAd getAdDTO(Ads ads) {
        FullAd fullAd = new FullAd();
        fullAd.setPk(ads.getPk());
        fullAd.setImage(ads.getImage());
        fullAd.setPrice(ads.getPrice());
        fullAd.setTitle(ads.getTitle());
        fullAd.setDescription(ads.getDescription());
        fullAd.setAuthorFirstName(ads.getAuthor().getFirstName());
        fullAd.setAuthorLastName(ads.getAuthor().getLastName());
        fullAd.setEmail(ads.getAuthor().getUsername());
        fullAd.setPhone(ads.getAuthor().getPhone());
        return fullAd;
    }

    /**
     * Преобразует {@link AdForUpdate} DTO в новую сущность {@link Ads}.
     * <p>
     * Используется при создании нового объявления.
     * <b>Не устанавливает:</b>
     * <ul>
     *   <li>id (автоматически генерируется БД)</li>
     *   <li>image (устанавливается отдельно после загрузки файла)</li>
     *   <li>author (должен быть установлен отдельно из контекста безопасности)</li>
     *   <li>comments (инициализируется пустым списком)</li>
     * </ul>
     * </p>
     *
     * <p><b>Типичное использование:</b></p>
     * <pre>
     * // В AdsService.createAds()
     * Ads newAd = adsMapper.updateAdsFromFullAds(adForUpdate);
     * newAd.setAuthor(currentUser); // Устанавливаем автора
     * newAd.setImage(savedImagePath); // Устанавливаем путь к изображению
     * Ads savedAd = adRepository.save(newAd);
     * </pre>
     *
     * <p><b>Рекомендации по улучшению:</b></p>
     * <ul>
     *   <li>Добавить валидацию полей (не null, цена ≥ 0)</li>
     *   <li>Добавить значения по умолчанию</li>
     *   <li>Создать отдельный метод для обновления существующей сущности</li>
     * </ul>
     *
     * @param adForUpdate DTO с данными для создания/обновления объявления
     * @return новая сущность Ads или null, если DTO равен null
     *
     * @see ru.skypro.homework.controller.AdsController#
     */
    public Ads updateAdsFromFullAds(AdForUpdate adForUpdate) {
        if (adForUpdate == null) {
            return null;
        }
        Ads ads = new Ads();
        ads.setTitle(adForUpdate.getTitle());
        ads.setDescription(adForUpdate.getDescription());
        ads.setPrice(adForUpdate.getPrice());
        return ads;
    }
}
