package ru.skypro.homework.service.mapped;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.AdForUpdate;
import ru.skypro.homework.dto.FullAd;
import ru.skypro.homework.model.Ads;

@Component
public class AdsMapper {
    //преобразование AdDTO в Ads - при регистрации
    public Ads toAds(AdDTO dto) {
        Ads ads = new Ads();
        ads.setPk(dto.getPk());
        ads.setImage(dto.getImage());
        ads.setPrice(dto.getPrice());
        ads.setTitle(dto.getTitle());
        return ads;
    }

    public AdDTO adsToDto(Ads ads) {
        AdDTO dto = new AdDTO();
        dto.setPk(ads.getPk());
        dto.setImage(ads.getImage());
        dto.setPrice(ads.getPrice());
        dto.setTitle(ads.getTitle());
        return dto;
    }

    //преобразование Ads в DTO
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

    //преобразование отдельных полей FullAd DTO в Ads
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

    //Преобразование image от FullAd DTO в Ads
    public void updateImageFromDTO(AdDTO dto, Ads ads) {
        if (dto.getImage() != null && dto.getImage().isBlank()) {
            ads.setImage(dto.getImage());
        }
    }
}
