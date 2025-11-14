package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final UserService userService;
    private final ImageService imageService;
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
    @Transactional
    public Ad createAd(CreateOrUpdateAd createAd, MultipartFile image) {
        UserEntity author = userService.getCurrentUserEntity();

        try {
            // Сохраняем картинку
            String imageUrl = imageService.saveAdImage(image, null); // ID будет известен после сохранения

            // Создаем объявление
            AdEntity adEntity = adMapper.createOrUpdateAdToEntity(createAd);
            adEntity.setAuthor(author);
            adEntity.setImagePath(imageUrl);
            adEntity.setCreatedAt(LocalDateTime.now());

            AdEntity savedEntity = adRepository.save(adEntity);

            // Обновляем путь картинки с ID объявления
            String finalImageUrl = imageService.saveAdImage(image, savedEntity.getId());
            savedEntity.setImagePath(finalImageUrl);
            adRepository.save(savedEntity);

            return adMapper.entityToAdDto(savedEntity);

        } catch (IOException e) {
            log.error("Failed to save ad image", e);
            throw new RuntimeException("Failed to save image", e);
        }
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

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#adId, authentication.name)")
    public Ad updateAdImage(Integer adId, MultipartFile image) {
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        try {
            String imageUrl = imageService.saveAdImage(image, adId);
            adEntity.setImagePath(imageUrl);
            adEntity.setUpdatedAt(LocalDateTime.now());

            AdEntity savedEntity = adRepository.save(adEntity);
            return adMapper.entityToAdDto(savedEntity);

        } catch (IOException e) {
            log.error("Failed to update ad image for adId: {}", adId, e);
            throw new RuntimeException("Failed to update image", e);
        }
    }

    /**
     * Получить картинку объявления
     */
    public byte[] getAdImage(Integer adId) {
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        if (adEntity.getImagePath() == null) {
            throw new RuntimeException("Ad has no image");
        }

        try {
            return imageService.getImage(adEntity.getImagePath());
        } catch (IOException e) {
            log.error("Failed to load ad image for adId: {}", adId, e);
            throw new RuntimeException("Failed to load image", e);
        }
    }

}
