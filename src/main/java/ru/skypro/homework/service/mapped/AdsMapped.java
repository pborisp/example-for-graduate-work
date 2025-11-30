package ru.skypro.homework.service.mapped;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.FullAd;
import ru.skypro.homework.dto.UsersDTO;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Users;

@Component
public class AdsMapped {
    //преобразование AdDTO в Ads - при регистрации
    public Ads toAds(AdDTO dto) {
        Ads ads = new Ads();
        ads.setPk(dto.getPk());
        ads.setImage(dto.getImage());
        ads.setPrice(dto.getPrice());
        ads.setTitle(dto.getTitle());
        return ads;
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
        fullAd.setEmail(ads.getAuthor().getEmail());
        fullAd.setPhone(ads.getAuthor().getPhone());
        return fullAd;
    }

    //преобразование отдельных полей FullAd DTO в Ads
    public void updateAdsFromFullAds(FullAd fullAd, Ads ads) {
        if (fullAd.getTitle() != null && fullAd.getTitle().isBlank()) {
            ads.setTitle(fullAd.getTitle());
        }
        if (fullAd.getPrice() != null) {
            ads.setPrice(fullAd.getPrice());
        }
        if (fullAd.getDescription() != null && fullAd.getDescription().isBlank()) {
            ads.setDescription(fullAd.getDescription());
        }
    }

    //Преобразование image от FullAd DTO в Ads
    public void updateImageFromDTO(AdDTO dto, Ads ads) {
        if (dto.getImage() != null && dto.getImage().isBlank()) {
            ads.setImage(dto.getImage());
        }
    }
}
