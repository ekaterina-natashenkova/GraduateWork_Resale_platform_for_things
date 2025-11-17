package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.dto.*;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.ImageEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Mock
    private AdMapper adMapper;

    @InjectMocks
    private AdService adService;

    @Test
    @DisplayName("createAd - успешное создание объявления с изображением")
    void createAd_WithImage_Success() throws Exception {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        createAd.setTitle("Test Ad");
        createAd.setPrice(1000);
        createAd.setDescription("Test description");

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        UserEntity author = new UserEntity();
        author.setId(1);

        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);

        AdEntity savedEntity = new AdEntity();
        savedEntity.setId(1);

        AdEntity finalEntity = new AdEntity();
        finalEntity.setId(1);

        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setFilePath("/images/ads/ad_1.jpg");

        Ad adDto = new Ad();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(adEntity);

        // Первый save - для получения ID
        when(adRepository.save(adEntity)).thenReturn(savedEntity);

        when(imageService.saveAdImageEntity(image, 1)).thenReturn(imageEntity);

        // Второй save - после добавления изображения
        when(adRepository.save(savedEntity)).thenReturn(finalEntity);
        when(adMapper.entityToAdDto(finalEntity)).thenReturn(adDto);

        // When
        Ad result = adService.createAd(createAd, image);

        // Then
        assertThat(result).isEqualTo(adDto);
        verify(adRepository, times(2)).save(any(AdEntity.class));
        verify(imageService).saveAdImageEntity(eq(image), eq(1));
    }

    @Test
    @DisplayName("createAd - успешное создание объявления без изображения")
    void createAd_WithoutImage_Success() {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        createAd.setTitle("Test Ad");
        createAd.setPrice(1000);
        createAd.setDescription("Test description");

        UserEntity author = new UserEntity();
        author.setId(1);

        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);

        AdEntity savedEntity = new AdEntity();
        savedEntity.setId(1);

        Ad adDto = new Ad();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(adEntity);
        when(adRepository.save(adEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(adDto);

        // When
        Ad result = adService.createAd(createAd, null);

        // Then
        assertThat(result).isEqualTo(adDto);

        // При создании без изображения save вызывается только 1 раз
        verify(adRepository, times(1)).save(adEntity);

        // ImageService не должен вызываться
        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("createAd - ошибка при сохранении изображения")
    void createAd_ImageSaveError() throws Exception {
        // Given
        CreateOrUpdateAd createAd = new CreateOrUpdateAd();
        createAd.setTitle("Test Ad");
        createAd.setPrice(1000);
        createAd.setDescription("Test description");

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        UserEntity author = new UserEntity();
        author.setId(1);

        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);

        AdEntity savedEntity = new AdEntity();
        savedEntity.setId(1);

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adMapper.createOrUpdateAdToEntity(createAd)).thenReturn(adEntity);
        when(adRepository.save(adEntity)).thenReturn(savedEntity);

        // Ошибка при сохранении изображения
        when(imageService.saveAdImageEntity(eq(image), eq(1))).thenThrow(new IOException("Save failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adService.createAd(createAd, image));

        assertThat(exception.getMessage()).isEqualTo("Failed to save image");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);

        // Проверяем, что объявление было сохранено (первый save)
        verify(adRepository).save(adEntity);

        // Проверяем, что была попытка сохранить изображение
        verify(imageService).saveAdImageEntity(eq(image), eq(1));

        // Проверяем, что второй save не вызывался (из-за ошибки)
        verify(adRepository, times(1)).save(any(AdEntity.class));
    }

    @Test
    @DisplayName("updateAdImage - успешное обновление изображения")
    void updateAdImage_Success() throws Exception {
        // Given
        Integer adId = 1;
        MultipartFile image = mock(MultipartFile.class);

        AdEntity adEntity = new AdEntity();
        adEntity.setId(adId);
        adEntity.setImages(new ArrayList<>());

        ImageEntity oldImage = new ImageEntity();
        oldImage.setId(100);
        adEntity.getImages().add(oldImage);

        ImageEntity newImage = new ImageEntity();
        newImage.setFilePath("/images/ads/ad_1_new.jpg");

        AdEntity savedEntity = new AdEntity();
        Ad adDto = new Ad();

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));
        when(imageService.saveAdImageEntity(eq(image), eq(adId))).thenReturn(newImage);
        when(adRepository.save(adEntity)).thenReturn(savedEntity);
        when(adMapper.entityToAdDto(savedEntity)).thenReturn(adDto);

        // When
        Ad result = adService.updateAdImage(adId, image);

        // Then
        assertThat(result).isEqualTo(adDto);
        verify(imageService).deleteImageEntity(100);
        verify(imageService).saveAdImageEntity(eq(image), eq(adId));
        verify(adRepository).save(adEntity);
    }

    @Test
    @DisplayName("updateAdImage - объявление не найдено")
    void updateAdImage_AdNotFound() {
        // Given
        Integer adId = 1;
        MultipartFile image = mock(MultipartFile.class);
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> adService.updateAdImage(adId, image));
        verify(adRepository).findById(adId);
        verify(imageService, never()).deleteImageEntity(any(Integer.class));
    }

    @Test
    @DisplayName("deleteAd - успешное удаление с изображениями")
    void deleteAd_WithImages_Success() {
        // Given
        Integer adId = 1;
        AdEntity adEntity = new AdEntity();
        adEntity.setId(adId);

        ImageEntity image1 = new ImageEntity();
        image1.setId(100);
        ImageEntity image2 = new ImageEntity();
        image2.setId(101);

        adEntity.setImages(Arrays.asList(image1, image2));

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));

        // When
        adService.deleteAd(adId);

        // Then
        verify(imageService).deleteImageEntity(100);
        verify(imageService).deleteImageEntity(101);
        verify(adRepository).delete(adEntity);
    }

    @Test
    @DisplayName("deleteAd - успешное удаление без изображений")
    void deleteAd_WithoutImages_Success() {
        // Given
        Integer adId = 1;
        AdEntity adEntity = new AdEntity();
        adEntity.setId(adId);
        adEntity.setImages(new ArrayList<>());

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));

        // When
        adService.deleteAd(adId);

        // Then
        verify(imageService, never()).deleteImageEntity(any(Integer.class));
        verify(adRepository).delete(adEntity);
    }

    @Test
    @DisplayName("getAdImage - успешное получение изображения")
    void getAdImage_Success() throws Exception {
        // Given
        Integer adId = 1;
        byte[] imageBytes = "image data".getBytes();
        when(imageService.getAdImage(adId)).thenReturn(imageBytes);

        // When
        byte[] result = adService.getAdImage(adId);

        // Then
        assertThat(result).isEqualTo(imageBytes);
        verify(imageService).getAdImage(adId);
    }

    @Test
    @DisplayName("getAdImage - ошибка при получении изображения")
    void getAdImage_Error() throws Exception {
        // Given
        Integer adId = 1;
        when(imageService.getAdImage(adId)).thenThrow(new IOException("Not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> adService.getAdImage(adId));
        verify(imageService).getAdImage(adId);
    }

    @Test
    @DisplayName("isAdOwner - пользователь является владельцем")
    void isAdOwner_UserIsOwner() {
        // Given
        Integer adId = 1;
        String userEmail = "user@example.com";

        UserEntity author = new UserEntity();
        author.setEmail(userEmail);

        AdEntity ad = new AdEntity();
        ad.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAdOwner - пользователь не является владельцем")
    void isAdOwner_UserIsNotOwner() {
        // Given
        Integer adId = 1;
        String userEmail = "user@example.com";

        UserEntity author = new UserEntity();
        author.setEmail("other@example.com");

        AdEntity ad = new AdEntity();
        ad.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isAdOwner - объявление не найдено")
    void isAdOwner_AdNotFound() {
        // Given
        Integer adId = 1;
        String userEmail = "user@example.com";

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When
        boolean result = adService.isAdOwner(adId, userEmail);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getAllAds - получение всех объявлений")
    void getAllAds_Success() {
        // Given
        AdEntity ad1 = new AdEntity();
        ad1.setId(1);
        AdEntity ad2 = new AdEntity();
        ad2.setId(2);

        List<AdEntity> adEntities = Arrays.asList(ad1, ad2);

        Ad adDto1 = new Ad();
        adDto1.setPk(1);
        Ad adDto2 = new Ad();
        adDto2.setPk(2);

        when(adRepository.findAll()).thenReturn(adEntities);
        when(adMapper.entityToAdDto(ad1)).thenReturn(adDto1);
        when(adMapper.entityToAdDto(ad2)).thenReturn(adDto2);

        // When
        Ads result = adService.getAllAds();

        // Then
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getResults()).containsExactly(adDto1, adDto2);
    }

    @Test
    @DisplayName("getAllAds - пустой список объявлений")
    void getAllAds_EmptyList() {
        // Given
        when(adRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Ads result = adService.getAllAds();

        // Then
        assertThat(result.getCount()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    @DisplayName("getAdsByAuthor - получение объявлений автора")
    void getAdsByAuthor_Success() {
        // Given
        UserEntity currentUser = new UserEntity();
        currentUser.setId(1);

        AdEntity ad1 = new AdEntity();
        ad1.setId(1);
        AdEntity ad2 = new AdEntity();
        ad2.setId(2);

        List<AdEntity> adEntities = Arrays.asList(ad1, ad2);

        Ad adDto1 = new Ad();
        adDto1.setPk(1);
        Ad adDto2 = new Ad();
        adDto2.setPk(2);

        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
        when(adRepository.findByAuthorId(1)).thenReturn(adEntities);
        when(adMapper.entityToAdDto(ad1)).thenReturn(adDto1);
        when(adMapper.entityToAdDto(ad2)).thenReturn(adDto2);

        // When
        Ads result = adService.getAdsByAuthor();

        // Then
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getResults()).containsExactly(adDto1, adDto2);
    }

    @Test
    @DisplayName("getAdsByAuthor - пустой список объявлений автора")
    void getAdsByAuthor_EmptyList() {
        // Given
        UserEntity currentUser = new UserEntity();
        currentUser.setId(1);

        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
        when(adRepository.findByAuthorId(1)).thenReturn(Collections.emptyList());

        // When
        Ads result = adService.getAdsByAuthor();

        // Then
        assertThat(result.getCount()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    @DisplayName("existsById - объявление существует")
    void existsById_AdExists() {
        // Given
        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(true);

        // When
        boolean result = adService.existsById(adId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsById - объявление не существует")
    void existsById_AdNotExists() {
        // Given
        Integer adId = 1;
        when(adRepository.existsById(adId)).thenReturn(false);

        // When
        boolean result = adService.existsById(adId);

        // Then
        assertThat(result).isFalse();
    }

}