package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdMapper adMapper;

    @InjectMocks
    private AdService adService;

    @Test
    void getAllAds_ShouldReturnAds() {

        List<AdEntity> adEntities = List.of(new AdEntity(), new AdEntity());
        List<Ad> ads = List.of(new Ad(), new Ad());

        when(adRepository.findAll()).thenReturn(adEntities);
        when(adMapper.entityToAdDto(any(AdEntity.class))).thenReturn(new Ad());

        Ads result = adService.getAllAds();

        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getResults().size());
        verify(adRepository).findAll();
        verify(adMapper, times(2)).entityToAdDto(any(AdEntity.class));
    }

    @Test
    void createAd_WithValidAuthor_ShouldReturnAd() {

        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        Integer authorId = 1;
        String imagePath = "/images/test.jpg";
        UserEntity author = new UserEntity();
        author.setId(authorId);
        AdEntity adEntity = new AdEntity();
        AdEntity savedEntity = new AdEntity();
        Ad expectedAd = new Ad();

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(adEntity);
        when(adRepository.save(adEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(expectedAd);

        Optional<Ad> result = adService.createAd(createAd, authorId, imagePath);

        assertTrue(result.isPresent());
        assertEquals(expectedAd, result.get());
        verify(userRepository).findById(authorId);
        verify(adRepository).save(adEntity);
        assertEquals(author, adEntity.getAuthor());
        assertEquals(imagePath, adEntity.getImagePath());
    }

    @Test
    void createAd_WithInvalidAuthor_ShouldReturnEmpty() {

        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        Integer authorId = 999;

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        Optional<Ad> result = adService.createAd(createAd, authorId, "/image.jpg");

        assertFalse(result.isPresent());
        verify(userRepository).findById(authorId);
        verify(adRepository, never()).save(any());
    }

    @Test
    void getAdById_WithExistingId_ShouldReturnExtendedAd() {

        Integer adId = 1;
        AdEntity adEntity = new AdEntity();
        ExtendedAd expectedAd = new ExtendedAd();

        when(adRepository.findByIdWithAuthor(adId)).thenReturn(Optional.of(adEntity));
        when(adMapper.entityToExtendedAdDto(adEntity)).thenReturn(expectedAd);

        Optional<ExtendedAd> result = adService.getAdById(adId);

        assertTrue(result.isPresent());
        assertEquals(expectedAd, result.get());
        verify(adRepository).findByIdWithAuthor(adId);
    }

    @Test
    void getAdById_WithNonExistingId_ShouldReturnEmpty() {

        Integer adId = 999;
        when(adRepository.findByIdWithAuthor(adId)).thenReturn(Optional.empty());

        Optional<ExtendedAd> result = adService.getAdById(adId);

        assertFalse(result.isPresent());
        verify(adRepository).findByIdWithAuthor(adId);
    }

    @Test
    void updateAd_WithExistingId_ShouldReturnUpdatedAd() {

        Integer adId = 1;
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();
        AdEntity existingEntity = new AdEntity();
        AdEntity savedEntity = new AdEntity();
        Ad expectedAd = new Ad();

        when(adRepository.findById(adId)).thenReturn(Optional.of(existingEntity));
        when(adRepository.save(existingEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(expectedAd);

        Optional<Ad> result = adService.updateAd(adId, updateAd);

        assertTrue(result.isPresent());
        assertEquals(expectedAd, result.get());
        verify(adMapper).updateEntityFromDto(updateAd, existingEntity);
        verify(adRepository).save(existingEntity);
    }

    @Test
    void updateAd_WithNonExistingId_ShouldReturnEmpty() {

        Integer adId = 999;
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        Optional<Ad> result = adService.updateAd(adId, updateAd);

        assertFalse(result.isPresent());
        verify(adRepository).findById(adId);
        verify(adRepository, never()).save(any());
    }

    @Test
    void deleteAd_WithExistingId_ShouldReturnTrue() {

        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(true);

        boolean result = adService.deleteAd(adId);

        assertTrue(result);
        verify(adRepository).deleteById(adId);
    }

    @Test
    void deleteAd_WithNonExistingId_ShouldReturnFalse() {

        Integer adId = 999;
        when(adRepository.existsById(adId)).thenReturn(false);

        boolean result = adService.deleteAd(adId);

        assertFalse(result);
        verify(adRepository, never()).deleteById(adId);
    }

    @Test
    void getAdsByAuthorId_ShouldReturnAds() {

        Integer authorId = 1;
        List<AdEntity> adEntities = List.of(new AdEntity());
        List<Ad> ads = List.of(new Ad());

        when(adRepository.findByAuthorId(authorId)).thenReturn(adEntities);
        when(adMapper.entityToAdDto(any(AdEntity.class))).thenReturn(new Ad());

        Ads result = adService.getAdsByAuthorId(authorId);

        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResults().size());
        verify(adRepository).findByAuthorId(authorId);
    }

    @Test
    void existsById_ShouldReturnRepositoryResult() {

        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(true);

        boolean result = adService.existsById(adId);

        assertTrue(result);
        verify(adRepository).existsById(adId);
    }

    @Test
    void isAdAuthor_WithValidAuthor_ShouldReturnTrue() {

        Integer adId = 1;
        Integer authorId = 1;
        when(adRepository.findByAuthorIdAndAdId(authorId, adId)).thenReturn(Optional.of(new AdEntity()));

        boolean result = adService.isAdAuthor(adId, authorId);

        assertTrue(result);
        verify(adRepository).findByAuthorIdAndAdId(authorId, adId);
    }

    @Test
    void isAdAuthor_WithInvalidAuthor_ShouldReturnFalse() {

        Integer adId = 1;
        Integer authorId = 999;
        when(adRepository.findByAuthorIdAndAdId(authorId, adId)).thenReturn(Optional.empty());

        boolean result = adService.isAdAuthor(adId, authorId);

        assertFalse(result);
        verify(adRepository).findByAuthorIdAndAdId(authorId, adId);
    }

}