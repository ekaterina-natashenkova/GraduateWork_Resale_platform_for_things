package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.dto.*;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @InjectMocks
    private AdService adService;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Mock
    private AdMapper adMapper;

    @Mock
    private MultipartFile multipartFile;

    private UserEntity testUser;
    private AdEntity testAdEntity;
    private Ad testAdDto;
    private ExtendedAd testExtendedAdDto;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAdEntity = new AdEntity();
        testAdEntity.setId(1);
        testAdEntity.setTitle("Test Ad");
        testAdEntity.setPrice(1000);
        testAdEntity.setDescription("Test Description");
        testAdEntity.setAuthor(testUser);
        testAdEntity.setImagePath("/images/ads/test.jpg");
        testAdEntity.setCreatedAt(LocalDateTime.now());

        testAdDto = new Ad();
        testAdDto.setPk(1);
        testAdDto.setTitle("Test Ad");
        testAdDto.setPrice(1000);
        testAdDto.setAuthor(1);

        testExtendedAdDto = new ExtendedAd();
        testExtendedAdDto.setPk(1);
        testExtendedAdDto.setTitle("Test Ad");
        testExtendedAdDto.setPrice(1000);
        testExtendedAdDto.setAuthorFirstName("John");
        testExtendedAdDto.setAuthorLastName("Doe");
        testExtendedAdDto.setEmail("test@example.com");
    }

    @Test
    void isAdOwner_WhenUserIsOwner_ShouldReturnTrue() {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));

        // When
        boolean result = adService.isAdOwner(1, "test@example.com");

        // Then
        assertTrue(result);
        verify(adRepository).findById(1);
    }

    @Test
    void isAdOwner_WhenUserIsNotOwner_ShouldReturnFalse() {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));

        // When
        boolean result = adService.isAdOwner(1, "other@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void isAdOwner_WhenAdNotFound_ShouldReturnFalse() {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.empty());

        // When
        boolean result = adService.isAdOwner(1, "test@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void getAllAds_ShouldReturnAds() {
        // Given
        List<AdEntity> adEntities = List.of(testAdEntity);
        when(adRepository.findAll()).thenReturn(adEntities);
        when(adMapper.entityToAdDto(testAdEntity)).thenReturn(testAdDto);

        // When
        Ads result = adService.getAllAds();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResults().size());
        assertEquals(testAdDto, result.getResults().get(0));
    }

    @Test
    void createAd_ShouldCreateAdWithImage() throws IOException {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        createAd.setTitle("New Ad");
        createAd.setPrice(2000);
        createAd.setDescription("New Description");

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(imageService.saveAdImage(multipartFile, null)).thenReturn("/images/ads/temp.jpg");
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(testAdEntity);
        when(adRepository.save(any(AdEntity.class))).thenReturn(testAdEntity);
        when(imageService.saveAdImage(multipartFile, 1)).thenReturn("/images/ads/ad_1_final.jpg");
        when(adMapper.entityToAdDto(testAdEntity)).thenReturn(testAdDto);

        // When
        Ad result = adService.createAd(createAd, multipartFile);

        // Then
        assertNotNull(result);
        verify(adRepository, times(2)).save(any(AdEntity.class));
        verify(imageService, times(2)).saveAdImage(any(MultipartFile.class), any());
    }

    @Test
    void createAd_WhenImageSaveFails_ShouldThrowException() throws IOException {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(imageService.saveAdImage(multipartFile, null))
                .thenThrow(new IOException("File save failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.createAd(createAd, multipartFile);
        });
    }

    @Test
    void getAdById_ShouldReturnExtendedAd() {
        // Given
        when(adRepository.findByIdWithAuthor(1)).thenReturn(Optional.of(testAdEntity));
        when(adMapper.entityToExtendedAdDto(testAdEntity)).thenReturn(testExtendedAdDto);

        // When
        Optional<ExtendedAd> result = adService.getAdById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testExtendedAdDto, result.get());
    }

    @Test
    void getAdById_WhenAdNotFound_ShouldReturnEmpty() {
        // Given
        when(adRepository.findByIdWithAuthor(1)).thenReturn(Optional.empty());

        // When
        Optional<ExtendedAd> result = adService.getAdById(1);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void updateAd_ShouldUpdateAd() {
        // Given
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();
        updateAd.setTitle("Updated Ad");
        updateAd.setPrice(1500);
        updateAd.setDescription("Updated Description");

        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));
        when(adRepository.save(testAdEntity)).thenReturn(testAdEntity);
        when(adMapper.entityToAdDto(testAdEntity)).thenReturn(testAdDto);

        // When
        Ad result = adService.updateAd(1, updateAd);

        // Then
        assertNotNull(result);
        verify(adMapper).updateEntityFromDto(updateAd, testAdEntity);
        verify(adRepository).save(testAdEntity);
    }

    @Test
    void updateAd_WhenAdNotFound_ShouldThrowException() {
        // Given
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();
        when(adRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.updateAd(1, updateAd);
        });
    }

    @Test
    void deleteAd_ShouldDeleteAd() {
        // Given
        when(adRepository.existsById(1)).thenReturn(true);

        // When
        adService.deleteAd(1);

        // Then
        verify(adRepository).deleteById(1);
    }

    @Test
    void deleteAd_WhenAdNotFound_ShouldThrowException() {
        // Given
        when(adRepository.existsById(1)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.deleteAd(1);
        });
    }

    @Test
    void getAdsByAuthor_ShouldReturnUserAds() {
        // Given
        List<AdEntity> userAds = List.of(testAdEntity);
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(adRepository.findByAuthorId(1)).thenReturn(userAds);
        when(adMapper.entityToAdDto(testAdEntity)).thenReturn(testAdDto);

        // When
        Ads result = adService.getAdsByAuthor();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResults().size());
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        // Given
        when(adRepository.existsById(1)).thenReturn(true);

        // When
        boolean result = adService.existsById(1);

        // Then
        assertTrue(result);
    }

    @Test
    void updateAdImage_ShouldUpdateImage() throws IOException {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));
        when(imageService.saveAdImage(multipartFile, 1)).thenReturn("/images/ads/new_image.jpg");
        when(adRepository.save(testAdEntity)).thenReturn(testAdEntity);
        when(adMapper.entityToAdDto(testAdEntity)).thenReturn(testAdDto);

        // When
        Ad result = adService.updateAdImage(1, multipartFile);

        // Then
        assertNotNull(result);
        assertEquals("/images/ads/new_image.jpg", testAdEntity.getImagePath());
        verify(adRepository).save(testAdEntity);
    }

    @Test
    void updateAdImage_WhenAdNotFound_ShouldThrowException() {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.updateAdImage(1, multipartFile);
        });
    }

    @Test
    void getAdImage_ShouldReturnImageBytes() throws IOException {
        // Given
        byte[] imageBytes = "test image content".getBytes();
        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));
        when(imageService.getImage("/images/ads/test.jpg")).thenReturn(imageBytes);

        // When
        byte[] result = adService.getAdImage(1);

        // Then
        assertArrayEquals(imageBytes, result);
    }

    @Test
    void getAdImage_WhenAdNotFound_ShouldThrowException() {
        // Given
        when(adRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.getAdImage(1);
        });
    }

    @Test
    void getAdImage_WhenNoImage_ShouldThrowException() {
        // Given
        testAdEntity.setImagePath(null);
        when(adRepository.findById(1)).thenReturn(Optional.of(testAdEntity));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adService.getAdImage(1);
        });
    }

}