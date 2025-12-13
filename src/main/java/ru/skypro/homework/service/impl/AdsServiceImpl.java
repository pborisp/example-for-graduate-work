package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserAuthServise;
import ru.skypro.homework.service.mapped.AdsMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления объявлениями.
 *
 * <p><b>Зависимости:</b></p>
 * <ul>
 *   <li>{@link AdsRepository} - доступ к данным объявлений</li>
 *   <li>{@link AdsMapper} - преобразование между сущностями и DTO</li>
 *   <li>{@link UserAuthServise} - определение текущего пользователя</li>
 *   <li>{@link ImageService} - работа с изображениями</li>
 * </ul>
 *
 * @see AdsService
 * @see ru.skypro.homework.controller.AdsController
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdsServiceImpl implements AdsService {
    private final AdsRepository adsRepository;
    private final AdsMapper adsMapper;
    private final UserAuthServise userAuthServise;
    private final ImageService imageService;

    /**
     * Получает все объявления в системе.
     * <p>
     *
     * @return DTO со списком всех объявлений
     */
    @Override
    public AdsDTO getAll() {
        List<Ads> allAds = adsRepository.findAll();
        List<FullAd> fullAds = allAds.stream()
                .map(adsMapper::getAdDTO)
                .collect(Collectors.toList());
        AdsDTO adsDTO = new AdsDTO();
        adsDTO.setCount(fullAds.size());
        adsDTO.setResults(fullAds);
        return adsDTO;
    }

    /**
     * Создает новое объявление.
     * <p>
     *
     * @param adForUpdate DTO с данными объявления
     * @param image       файл изображения
     * @return DTO созданного объявления
     * @throws IOException если ошибка сохранения изображения
     */
    @Override
    public AdDTO createAds(AdForUpdate adForUpdate, MultipartFile image) throws IOException {
        Users user = userAuthServise.getCurrentUser();
        Ads ads = adsMapper.updateAdsFromFullAds(adForUpdate);
        ads.setAuthor(user);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image image is required");
        }
        String path = imageService.saveImage(image);
        ads.setImage(path);
        adsRepository.save(ads);
        adsRepository.flush();
        AdDTO adDTO = adsMapper.adsToDto(ads);
        adDTO.setAuthor(user.getId());
        return adDTO;
    }

    /**
     * Получает полную информацию об объявлении.
     * <p>
     *
     * @param id идентификатор объявления
     * @return полная информация об объявлении или null
     */
    @Override
    public FullAd getFullAd(Long id) {
        userAuthServise.getCurrentUser();
        Ads ads = adsRepository.findByPk(id).orElse(null);
        if (ads == null) {
            return null;
        }
        FullAd fullAd = adsMapper.getAdDTO(ads);
        if (fullAd == null) {
            throw new MappingException("Failed to map Ad to FullAd");
        }
        return fullAd;
    }

    /**
     * Обновляет изображение объявления.
     *
     * @param id    идентификатор объявления
     * @param image новое изображение
     * @throws IOException если ошибка сохранения изображения
     */
    @Override
    public void uppdateImageOfAd(Long id, MultipartFile image) throws IOException {
        Users user = userAuthServise.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !userAuthServise.getAdsAuthorByPk(id).equals(user.getId())) {
            throw new RuntimeException("User is not ADMIN");
        }
        AdDTO dto = getAdDTO(id);
        if (dto == null) {
            throw new NotFoundException("Not found AdDTO");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image image is required");
        }
        String path = imageService.saveImage(image);
        adsRepository.updateImage(id, path);
        adsRepository.flush();
    }

    /**
     * Удаляет объявление.
     *
     * @param id идентификатор объявления
     */
    @Override
    public void deleteAd(Long id) {
        Users user = userAuthServise.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !userAuthServise.getAdsAuthorByPk(id).equals(user.getId())) {
            throw new RuntimeException("User is not ADMIN");
        }
        adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        adsRepository.deleteById(id);
    }

    /**
     * Обновляет информацию об объявлении.
     *
     * @param id          идентификатор объявления
     * @param adForUpdate DTO с новыми данными
     * @return обновленное объявление в формате DTO
     */
    @Override
    public AdDTO updateAd(Long id, AdForUpdate adForUpdate) {
        Users user = userAuthServise.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !userAuthServise.getAdsAuthorByPk(id).equals(user.getId())) {
            throw new RuntimeException("User is not ADMIN");
        }
        Ads ads2 = new Ads();
        ads2 = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));

        Ads ads = adsMapper.updateAdsFromFullAds(adForUpdate);

        adsRepository.updateTitle(id, ads.getTitle());
        adsRepository.updateDescription(id, ads.getDescription());
        adsRepository.updatePrice(id, ads.getPrice());
        FullAd dto = adsMapper.getAdDTO(ads2);
        AdDTO adDTO = new AdDTO();
        adDTO.setAuthor(user.getId());
        adDTO.setImage(dto.getImage());
        adDTO.setTitle(adForUpdate.getTitle());
        adDTO.setPk(id);
        adDTO.setPrice(adForUpdate.getPrice());
        return adDTO;
    }

    /**
     * Получает все объявления текущего пользователя.
     *
     * @return DTO со списком объявлений пользователя
     */
    @Override
    public AdsDTO getAllAdsByUser() {
        Users user = userAuthServise.getCurrentUser();
        if (user == null) {
            log.error("User not authenticated");
            return createEmptyAdsDTO();
        }
        log.info("Getting ads for user: {} (ID: {})", user.getUsername(), user.getId());
        List<Ads> allAds = adsRepository.findByAuthorId(user.getId()).orElse(Collections.emptyList());

        if (allAds.isEmpty()) {
            log.info("User {} has no ads, returning empty list", user.getUsername());
            return createEmptyAdsDTO();
        }
        log.info("Found {} ads for user {}", allAds.size(), user.getUsername());
        List<FullAd> fullAds = allAds.stream()
                .map(ads -> {
                    FullAd fullAd = adsMapper.getAdDTO(ads);

                    // Обрабатываем изображение
                    if (fullAd.getImage() != null && !fullAd.getImage().isEmpty()) {
                        if (!fullAd.getImage().startsWith("/")) {
                            fullAd.setImage("/uploads/" + fullAd.getImage());
                        }
                    }
                    return fullAd;
                })
                .collect(Collectors.toList());
        AdsDTO adsDTO = new AdsDTO();
        adsDTO.setCount(fullAds.size());
        adsDTO.setResults(fullAds);
        return adsDTO;
    }

    // Вспомогательный метод поиска объявления по id
    public AdDTO getAdDTO(Long id) {
        Ads ads = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));

        AdDTO dto = adsMapper.adsToDto(ads);
        if (dto == null) {
            throw new MappingException("Failed to map Ad to FullAd");
        }
        return dto;
    }

    /**
     * Создает пустой DTO со списком объявлений.
     *
     * @return пустой AdsDTO
     */
    private AdsDTO createEmptyAdsDTO() {
        AdsDTO emptyDTO = new AdsDTO();
        emptyDTO.setCount(0);
        emptyDTO.setResults(Collections.emptyList());
        return emptyDTO;
    }
}
