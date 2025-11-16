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
import ru.skypro.homework.model.entity.ImageEntity;
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
     * Создать объявление
     */
    @Transactional
    public Ad createAd(CreateOrUpdateAd createAd, MultipartFile image) {
        UserEntity author = userService.getCurrentUserEntity();

        try {
            // Создаем объявление
            AdEntity adEntity = adMapper.createOrUpdateAdToEntity(createAd);
            adEntity.setAuthor(author);
            adEntity.setCreatedAt(LocalDateTime.now());

            // Сохраняем картинку и получаем ImageEntity
            if (image != null && !image.isEmpty()) {
                // Сохраняем объявление чтобы получить ID
                AdEntity savedEntity = adRepository.save(adEntity);

                // Сохраняем изображение с привязкой к объявлению
                ImageEntity imageEntity = imageService.saveAdImageEntity(image, savedEntity.getId());

                // Обновляем объявление
                savedEntity.setImagePath(imageEntity.getFilePath());
                savedEntity.addImage(imageEntity);

                AdEntity finalEntity = adRepository.save(savedEntity);
                return adMapper.entityToAdDto(finalEntity);
            } else {
                // Без изображения
                AdEntity savedEntity = adRepository.save(adEntity);
                return adMapper.entityToAdDto(savedEntity);
            }

        } catch (IOException e) {
            log.error("Failed to save ad image", e);
            throw new RuntimeException("Failed to save image", e);
        }
    }

    /**
     * Обновить изображение объявления
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#adId, authentication.name)")
    public Ad updateAdImage(Integer adId, MultipartFile image) {
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        try {
            // Удаляем старое изображение если есть
            if (!adEntity.getImages().isEmpty()) {
                for (ImageEntity oldImage : adEntity.getImages()) {
                    imageService.deleteImageEntity(oldImage.getId());
                }
                adEntity.getImages().clear();
            }

            // Сохраняем новое изображение с привязкой к объявлению
            ImageEntity imageEntity = imageService.saveAdImageEntity(image, adId);

            // Обновляем объявление
            adEntity.setImagePath(imageEntity.getFilePath());
            adEntity.addImage(imageEntity);
            adEntity.setUpdatedAt(LocalDateTime.now());

            AdEntity savedEntity = adRepository.save(adEntity);
            return adMapper.entityToAdDto(savedEntity);

        } catch (IOException e) {
            log.error("Failed to update ad image for adId: {}", adId, e);
            throw new RuntimeException("Failed to update image", e);
        }
    }

    // Остальные методы остаются без изменений...
    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#id, authentication.name)")
    public void deleteAd(Integer id) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        // Удаляем связанные изображения если есть
        if (!adEntity.getImages().isEmpty()) {
            for (ImageEntity image : adEntity.getImages()) {
                imageService.deleteImageEntity(image.getId());
            }
        }

        adRepository.delete(adEntity);
    }

    /**
     * Получить картинку объявления
     */
    public byte[] getAdImage(Integer adId) {
        try {
            return imageService.getAdImage(adId);
        } catch (IOException e) {
            log.error("Failed to load ad image for adId: {}", adId, e);
            throw new RuntimeException("Failed to load image", e);
        }
    }

    // Остальные методы без изменений...
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

    public Optional<ExtendedAd> getAdById(Integer id) {
        return adRepository.findById(id)
                .map(adMapper::entityToExtendedAdDto);
    }

    public AdEntity getAdEntityById(Integer id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));
    }

    @PreAuthorize("hasRole('ADMIN') or @adService.isAdOwner(#id, authentication.name)")
    public Ad updateAd(Integer id, CreateOrUpdateAd updateAd) {
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        adMapper.updateEntityFromDto(updateAd, adEntity);
        adEntity.setUpdatedAt(LocalDateTime.now());
        AdEntity savedEntity = adRepository.save(adEntity);
        return adMapper.entityToAdDto(savedEntity);
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
