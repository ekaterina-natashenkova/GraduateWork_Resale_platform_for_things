package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final UserService userService;
    private final AdMapper adMapper;

    /**
     * Проверка, является ли пользователь владельцем объявления - используется в @PreAuthorize
     */
    public boolean isAdOwner(Integer adId, String userEmail) {
        return adRepository.findById(adId)
                .map(ad -> ad.getAuthor().getEmail().equals(userEmail))
                .orElse(false);
    }

    public Ads getAllAds() {
        List<AdEntity> adEntities = adRepository.findAll();
        List<Ad> ads = adEntities.stream()
                .map(adMapper::entityToAdDto)
                .collect(Collectors.toList());

        Ads result = new Ads();
        result.setCount(ads.size());
        result.setResults(ads);
        return result;
    }

    /**
     * Создать объявление (автор определяется из SecurityContext)
     */
    public Ad createAd(CreateOrUpdateAd createAd, String imagePath) {
        UserEntity author = userService.getCurrentUserEntity();

        AdEntity adEntity = adMapper.createOrUpdateAdToEntity(createAd);
        adEntity.setAuthor(author);
        adEntity.setImagePath(imagePath);

        AdEntity savedEntity = adRepository.save(adEntity);
        return adMapper.entityToAdDto(savedEntity);
    }

    public Optional<ExtendedAd> getAdById(Integer id) {
        return adRepository.findByIdWithAuthor(id)
                .map(adMapper::entityToExtendedAdDto);
    }

    /**
     * Обновить объявление с проверкой прав через @PreAuthorize
     */
    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#id, authentication.name)")
    public Ad updateAd(Integer id, CreateOrUpdateAd updateAd) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        adMapper.updateEntityFromDto(updateAd, adEntity);
        AdEntity savedEntity = adRepository.save(adEntity);
        return adMapper.entityToAdDto(savedEntity);
    }

    /**
     * Удалить объявление с проверкой прав через @PreAuthorize
     */
    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#id, authentication.name)")
    public void deleteAd(Integer id) {
        if (!adRepository.existsById(id)) {
            throw new RuntimeException("Ad not found");
        }
        adRepository.deleteById(id);
    }

    public Ads getAdsByAuthor() {
        UserEntity currentUser = userService.getCurrentUserEntity();
        List<AdEntity> adEntities = adRepository.findByAuthorId(currentUser.getId());
        List<Ad> ads = adEntities.stream()
                .map(adMapper::entityToAdDto)
                .collect(Collectors.toList());

        Ads result = new Ads();
        result.setCount(ads.size());
        result.setResults(ads);
        return result;
    }

    public boolean existsById(Integer id) {
        return adRepository.existsById(id);
    }

}
