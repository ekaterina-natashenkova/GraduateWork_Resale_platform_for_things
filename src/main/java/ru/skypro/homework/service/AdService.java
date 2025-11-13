package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;

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

    public Optional<Ad> createAd(CreateOrUpdateAd createAd, Integer authorId, String imagePath) {
        Optional<UserEntity> authorOpt = userRepository.findById(authorId);
        if (authorOpt.isEmpty()) {
            return Optional.empty();
        }

        AdEntity adEntity = adMapper.createOrUpdateAdToEntity(createAd);
        adEntity.setAuthor(authorOpt.get());
        adEntity.setImagePath(imagePath);

        AdEntity savedEntity = adRepository.save(adEntity);
        return Optional.of(adMapper.entityToAdDto(savedEntity));
    }

    public Optional<ExtendedAd> getAdById(Integer id) {
        return adRepository.findByIdWithAuthor(id)
                .map(adMapper::entityToExtendedAdDto);
    }

    public Optional<Ad> updateAd(Integer id, CreateOrUpdateAd updateAd) {
        return adRepository.findById(id)
                .map(adEntity -> {
                    adMapper.updateEntityFromDto(updateAd, adEntity);
                    AdEntity savedEntity = adRepository.save(adEntity);
                    return adMapper.entityToAdDto(savedEntity);
                });
    }

    public boolean deleteAd(Integer id) {
        if (adRepository.existsById(id)) {
            adRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Ads getAdsByAuthorId(Integer authorId) {
        List<AdEntity> adEntities = adRepository.findByAuthorId(authorId);
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

    public boolean isAdAuthor(Integer adId, Integer authorId) {
        return adRepository.findByAuthorIdAndAdId(authorId, adId).isPresent();
    }

}
