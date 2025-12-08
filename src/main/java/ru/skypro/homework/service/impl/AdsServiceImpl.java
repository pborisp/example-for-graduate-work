package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.mapped.AdsMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdsServiceImpl implements AdsService {
    private final AdsRepository adsRepository;
    private final AdsMapper adsMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final ImageService imageService;

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

    @Override
    public AdDTO createAds(AdForUpdate adForUpdate, MultipartFile image) throws IOException {
        Users user = getCurrentUser();
        Ads ads = adsMapper.updateAdsFromFullAds(adForUpdate);
        ads.setAuthor(user);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image image is required");
        }
        String path = imageService.saveImage(image);
        ads.setImage(path);
        System.out.println(ads);
        System.out.println();
        adsRepository.save(ads);
        adsRepository.flush();
        AdDTO adDTO = adsMapper.adsToDto(ads);
        adDTO.setAuthor(user.getId());
        return adDTO;
    }

    @Override
    public FullAd getFullAd(Long id) {
        Users user = getCurrentUser();
        Ads ads = adsRepository.findByPk(id).orElse(null);
        if (adsMapper == null) {
            throw new IllegalStateException("AdsMapper is not initialized");
        }
        if (ads == null) {
            return null;
        }
        FullAd fullAd = adsMapper.getAdDTO(ads);
        if (fullAd == null) {
            throw new MappingException("Failed to map Ad to FullAd");
        }
        return fullAd;
    }

    @Override
    public void uppdateImageOfAd(Long id, MultipartFile image) throws IOException {
        Users user = getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !getAdsAuthorByPk(id).equals(user.getId())) {
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

    @Override
    public void deleteAd(Long id) {
        Users user = getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !getAdsAuthorByPk(id).equals(user.getId())) {
            throw new RuntimeException("User is not ADMIN");
        }
        adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        adsRepository.deleteById(id);
    }

    @Override
    public AdDTO updateAd(Long id, AdForUpdate  adForUpdate) {
        Users user = getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !getAdsAuthorByPk(id).equals(user.getId())) {
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

    @Override
    public AdsDTO getAllAdsByUser() {
        Users user = getCurrentUser();
        List<Ads> allAds = adsRepository.findByAuthorId(user.getId()).orElse(null);
        if (adsMapper == null) {
            throw new IllegalStateException("AdsMapper is not initialized");
        }
        if (allAds.isEmpty() || allAds == null) {
            return null;
        }
        List<FullAd> fullAds = allAds.stream()
                .map(adsMapper::getAdDTO)
                .collect(Collectors.toList());
        AdsDTO adsDTO = new AdsDTO();
        adsDTO.setCount(fullAds.size());
        adsDTO.setResults(fullAds);
        return adsDTO;
    }

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("User is not authenticated");
        }
        Optional<Users> users = usersRepository.findByUsername(authentication.getName());
        return users.orElseThrow(() -> new RuntimeException("User not found"));
    }

    public FullAd getAd(Long id) {
        Ads ads = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        if (adsMapper == null) {
            throw new IllegalStateException("AdsMapper is not initialized");
        }
        FullAd fullAd = adsMapper.getAdDTO(ads);
        if (fullAd == null) {
            throw new MappingException("Failed to map Ad to FullAd");
        }
        return fullAd;
    }

    // Вспомогательный метод поиска объявления по id
    public AdDTO getAdDTO(Long id) {
        Ads ads = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        if (adsMapper == null) {
            throw new IllegalStateException("AdsMapper is not initialized");
        }
        AdDTO dto = adsMapper.adsToDto(ads);
        if (dto == null) {
            throw new MappingException("Failed to map Ad to FullAd");
        }
        return dto;
    }

    public Long getAdsAuthorByPk(Long id) {
        Ads ads = adsRepository.findByPk(id).orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));
        if (adsMapper == null) {
            throw new IllegalStateException("AdsMapper is not initialized");
        }
        return ads.getAuthor().getId();
    }
}
