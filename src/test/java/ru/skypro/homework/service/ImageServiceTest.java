package ru.skypro.homework.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.ImageEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.repository.AdRepository;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    // Тестируем только методы, которые не работают с файлами
    @Test
    @DisplayName("getUserAvatar - пользователь не найден")
    void getUserAvatar_UserNotFound() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IOException.class, () -> imageService.getUserAvatar(userId));
    }

    @Test
    @DisplayName("getUserAvatar - у пользователя нет аватара")
    void getUserAvatar_NoAvatar() {
        // Given
        Integer userId = 1;
        UserEntity user = new UserEntity();
        user.setImagePath(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(IOException.class, () -> imageService.getUserAvatar(userId));
    }

    @Test
    @DisplayName("getAdImage - объявление не найдено")
    void getAdImage_AdNotFound() {
        // Given
        Integer adId = 1;
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IOException.class, () -> imageService.getAdImage(adId));
    }

    @Test
    @DisplayName("getAdImage - у объявления нет изображения")
    void getAdImage_NoImage() {
        // Given
        Integer adId = 1;
        AdEntity ad = new AdEntity();
        ad.setImagePath(null);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When & Then
        assertThrows(IOException.class, () -> imageService.getAdImage(adId));
    }

    @Test
    @DisplayName("getImageContentType - для пользователя с JPEG")
    void getImageContentType_UserJpeg() {
        // Given
        Integer userId = 1;
        UserEntity user = new UserEntity();
        user.setImagePath("/images/users/avatar.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        String contentType = imageService.getImageContentType(userId, "user");

        // Then
        assertThat(contentType).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("getImageContentType - для объявления с PNG")
    void getImageContentType_AdPng() {
        // Given
        Integer adId = 1;
        AdEntity ad = new AdEntity();
        ad.setImagePath("/images/ads/image.png");

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When
        String contentType = imageService.getImageContentType(adId, "ad");

        // Then
        assertThat(contentType).isEqualTo("image/png");
    }

    @Test
    @DisplayName("getImageContentType - fallback при отсутствии сущности")
    void getImageContentType_Fallback() {
        // Given
        Integer entityId = 1;
        when(userRepository.findById(entityId)).thenReturn(Optional.empty());

        // When
        String contentType = imageService.getImageContentType(entityId, "user");

        // Then
        assertThat(contentType).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("saveUserImage - пользователь не найден")
    void saveUserImage_UserNotFound() {
        // Given
        String userEmail = "nonexistent@example.com";
        MultipartFile image = mock(MultipartFile.class);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IOException.class, () -> imageService.saveUserImage(image, userEmail));

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(imageRepository);
    }

}