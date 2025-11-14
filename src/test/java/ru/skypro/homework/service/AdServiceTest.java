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
    private UserService userService;

    @Mock
    private AdMapper adMapper;

    @InjectMocks
    private AdService adService;

    @Test
    void getAllAds_ShouldReturnAds() {
        // Given
        List<AdEntity> adEntities = List.of(new AdEntity(), new AdEntity());
        List<Ad> ads = List.of(new Ad(), new Ad());

        when(adRepository.findAll()).thenReturn(adEntities);
        when(adMapper.entityToAdDto(any(AdEntity.class))).thenReturn(new Ad());

        // When
        Ads result = adService.getAllAds();

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getResults().size());
        verify(adRepository).findAll();
        verify(adMapper, times(2)).entityToAdDto(any(AdEntity.class));
    }

    @Test
    void createAd_WithValidData_ShouldReturnAd() {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        String imagePath = "/images/test.jpg";
        UserEntity author = new UserEntity();
        author.setId(1);
        author.setEmail("test@example.com");

        AdEntity adEntity = new AdEntity();
        AdEntity savedEntity = new AdEntity();
        Ad expectedAd = new Ad();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(adEntity);
        when(adRepository.save(adEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(expectedAd);

        // When
        Ad result = adService.createAd(createAd, imagePath);

        // Then
        assertNotNull(result);
        assertEquals(expectedAd, result);
        verify(userService).getCurrentUserEntity();
        verify(adMapper).createOrUpdateAdToEntity(createAd);
        verify(adRepository).save(adEntity);
        assertEquals(author, adEntity.getAuthor());
        assertEquals(imagePath, adEntity.getImagePath());
    }

    @Test
    void getAdById_WithExistingId_ShouldReturnExtendedAd() {
        // Given
        Integer adId = 1;
        AdEntity adEntity = new AdEntity();
        ExtendedAd expectedAd = new ExtendedAd();

        when(adRepository.findByIdWithAuthor(adId)).thenReturn(Optional.of(adEntity));
        when(adMapper.entityToExtendedAdDto(adEntity)).thenReturn(expectedAd);

        // When
        Optional<ExtendedAd> result = adService.getAdById(adId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedAd, result.get());
        verify(adRepository).findByIdWithAuthor(adId);
        verify(adMapper).entityToExtendedAdDto(adEntity);
    }

    @Test
    void getAdById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Integer adId = 999;
        when(adRepository.findByIdWithAuthor(adId)).thenReturn(Optional.empty());

        // When
        Optional<ExtendedAd> result = adService.getAdById(adId);

        // Then
        assertFalse(result.isPresent());
        verify(adRepository).findByIdWithAuthor(adId);
        verify(adMapper, never()).entityToExtendedAdDto(any());
    }

    @Test
    void updateAd_WithExistingId_ShouldReturnUpdatedAd() {
        // Given
        Integer adId = 1;
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();
        AdEntity existingEntity = new AdEntity();
        AdEntity savedEntity = new AdEntity();
        Ad expectedAd = new Ad();

        when(adRepository.findById(adId)).thenReturn(Optional.of(existingEntity));
        when(adRepository.save(existingEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(expectedAd);

        // When
        Ad result = adService.updateAd(adId, updateAd);

        // Then
        assertNotNull(result);
        assertEquals(expectedAd, result);
        verify(adRepository).findById(adId);
        verify(adMapper).updateEntityFromDto(updateAd, existingEntity);
        verify(adRepository).save(existingEntity);
    }

    @Test
    void updateAd_WithNonExistingId_ShouldThrowException() {
        // Given
        Integer adId = 999;
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> adService.updateAd(adId, updateAd));
        verify(adRepository).findById(adId);
        verify(adMapper, never()).updateEntityFromDto(any(), any());
        verify(adRepository, never()).save(any());
    }

    @Test
    void deleteAd_WithExistingId_ShouldDeleteAd() {
        // Given
        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(true);

        // When
        adService.deleteAd(adId);

        // Then
        verify(adRepository).existsById(adId);
        verify(adRepository).deleteById(adId);
    }

    @Test
    void deleteAd_WithNonExistingId_ShouldThrowException() {
        // Given
        Integer adId = 999;
        when(adRepository.existsById(adId)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> adService.deleteAd(adId));
        verify(adRepository).existsById(adId);
        verify(adRepository, never()).deleteById(any());
    }

    @Test
    void getAdsByAuthor_ShouldReturnUserAds() {
        // Given
        UserEntity currentUser = new UserEntity();
        currentUser.setId(1);
        currentUser.setEmail("user@example.com");

        List<AdEntity> adEntities = List.of(new AdEntity(), new AdEntity());
        List<Ad> ads = List.of(new Ad(), new Ad());

        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
        when(adRepository.findByAuthorId(currentUser.getId())).thenReturn(adEntities);
        when(adMapper.entityToAdDto(any(AdEntity.class))).thenReturn(new Ad());

        // When
        Ads result = adService.getAdsByAuthor();

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getResults().size());
        verify(userService).getCurrentUserEntity();
        verify(adRepository).findByAuthorId(currentUser.getId());
        verify(adMapper, times(2)).entityToAdDto(any(AdEntity.class));
    }

    @Test
    void existsById_ShouldReturnRepositoryResult() {
        // Given
        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(true);

        // When
        boolean result = adService.existsById(adId);

        // Then
        assertTrue(result);
        verify(adRepository).existsById(adId);
    }

    @Test
    void isAdOwner_WithValidOwner_ShouldReturnTrue() {
        // Given
        Integer adId = 1;
        String userEmail = "owner@example.com";
        UserEntity author = new UserEntity();
        author.setEmail(userEmail);
        AdEntity adEntity = new AdEntity();
        adEntity.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertTrue(result);
        verify(adRepository).findById(adId);
    }

    @Test
    void isAdOwner_WithInvalidOwner_ShouldReturnFalse() {
        // Given
        Integer adId = 1;
        String userEmail = "owner@example.com";
        String differentEmail = "other@example.com";
        UserEntity author = new UserEntity();
        author.setEmail(differentEmail);
        AdEntity adEntity = new AdEntity();
        adEntity.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertFalse(result);
        verify(adRepository).findById(adId);
    }

    @Test
    void isAdOwner_WithNonExistingAd_ShouldReturnFalse() {
        // Given
        Integer adId = 999;
        String userEmail = "owner@example.com";

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertFalse(result);
        verify(adRepository).findById(adId);
    }

}