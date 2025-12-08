package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.AdForUpdate;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.FullAd;
import ru.skypro.homework.model.Ads;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AdsService {
    AdsDTO getAll();
    AdDTO createAds(AdForUpdate adForUpdate, MultipartFile image) throws IOException;
    FullAd getFullAd(Long id);
    void uppdateImageOfAd(Long id, MultipartFile file) throws IOException;
    void deleteAd(Long id);
    AdDTO updateAd(Long id, AdForUpdate adForUpdate);
    AdsDTO getAllAdsByUser();
}
