package ru.skypro.homework.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.skypro.homework.service.ImageService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @Test
    @DisplayName("GET /images/ads/{adId}/image - успешное получение изображения объявления")
    void getAdImage_Success() throws Exception {
        // Given
        Integer adId = 1;
        byte[] imageBytes = "fake image data".getBytes();
        String contentType = "image/jpeg";

        when(imageService.getAdImage(adId)).thenReturn(imageBytes);
        when(imageService.getImageContentType(adId, "ad")).thenReturn(contentType);

        // When
        ResponseEntity<byte[]> response = imageController.getAdImage(adId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(imageBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType(contentType));
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");

        verify(imageService).getAdImage(adId);
        verify(imageService).getImageContentType(adId, "ad");
    }

    @Test
    @DisplayName("GET /images/ads/{adId}/image - изображение не найдено")
    void getAdImage_NotFound() throws Exception {
        // Given
        Integer adId = 1;
        when(imageService.getAdImage(adId)).thenThrow(new IOException("Image not found"));

        // When
        ResponseEntity<byte[]> response = imageController.getAdImage(adId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(imageService).getAdImage(adId);
        verify(imageService, never()).getImageContentType(any(), any());
    }

    @Test
    @DisplayName("GET /images/ads/{adId}/image - внутренняя ошибка сервера")
    void getAdImage_InternalServerError() throws Exception {
        // Given
        Integer adId = 1;
        when(imageService.getAdImage(adId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<byte[]> response = imageController.getAdImage(adId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        verify(imageService).getAdImage(adId);
        verify(imageService, never()).getImageContentType(any(), any());
    }

    @Test
    @DisplayName("GET /images/users/{userId}/avatar - успешное получение аватара пользователя")
    void getUserAvatar_Success() throws Exception {
        // Given
        Integer userId = 1;
        byte[] imageBytes = "fake avatar data".getBytes();
        String contentType = "image/png";

        when(imageService.getUserAvatar(userId)).thenReturn(imageBytes);
        when(imageService.getImageContentType(userId, "user")).thenReturn(contentType);

        // When
        ResponseEntity<byte[]> response = imageController.getUserAvatar(userId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(imageBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType(contentType));
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");

        verify(imageService).getUserAvatar(userId);
        verify(imageService).getImageContentType(userId, "user");
    }

    @Test
    @DisplayName("GET /images/users/{userId}/avatar - аватар не найден")
    void getUserAvatar_NotFound() throws Exception {
        // Given
        Integer userId = 1;
        when(imageService.getUserAvatar(userId)).thenThrow(new IOException("Avatar not found"));

        // When
        ResponseEntity<byte[]> response = imageController.getUserAvatar(userId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(imageService).getUserAvatar(userId);
        verify(imageService, never()).getImageContentType(any(), any());
    }

    @Test
    @DisplayName("GET /images/users/{userId}/avatar - внутренняя ошибка сервера")
    void getUserAvatar_InternalServerError() throws Exception {
        // Given
        Integer userId = 1;
        when(imageService.getUserAvatar(userId)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<byte[]> response = imageController.getUserAvatar(userId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        verify(imageService).getUserAvatar(userId);
        verify(imageService, never()).getImageContentType(any(), any());
    }

    @Test
    @DisplayName("GET /images/ads/{adId}/image - проверка разных Content-Type")
    void getAdImage_DifferentContentTypes() throws Exception {
        // Given
        Integer adId = 1;
        byte[] imageBytes = "test image".getBytes();

        when(imageService.getAdImage(adId)).thenReturn(imageBytes);

        // Test JPEG
        when(imageService.getImageContentType(adId, "ad")).thenReturn("image/jpeg");
        ResponseEntity<byte[]> jpegResponse = imageController.getAdImage(adId);
        assertThat(jpegResponse.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);

        // Test PNG
        when(imageService.getImageContentType(adId, "ad")).thenReturn("image/png");
        ResponseEntity<byte[]> pngResponse = imageController.getAdImage(adId);
        assertThat(pngResponse.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);

        // Test GIF
        when(imageService.getImageContentType(adId, "ad")).thenReturn("image/gif");
        ResponseEntity<byte[]> gifResponse = imageController.getAdImage(adId);
        assertThat(gifResponse.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_GIF);

        // Test fallback
        when(imageService.getImageContentType(adId, "ad")).thenReturn("application/octet-stream");
        ResponseEntity<byte[]> fallbackResponse = imageController.getAdImage(adId);
        assertThat(fallbackResponse.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    @DisplayName("GET /images/users/{userId}/avatar - проверка кэширования")
    void getUserAvatar_CacheHeaders() throws Exception {
        // Given
        Integer userId = 1;
        byte[] imageBytes = "avatar".getBytes();
        String contentType = "image/jpeg";

        when(imageService.getUserAvatar(userId)).thenReturn(imageBytes);
        when(imageService.getImageContentType(userId, "user")).thenReturn(contentType);

        // When
        ResponseEntity<byte[]> response = imageController.getUserAvatar(userId);

        // Then
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");
    }

    @Test
    @DisplayName("GET /images/ads/{adId}/image - пустое тело ответа при ошибке")
    void getAdImage_EmptyBodyOnError() throws Exception {
        // Given
        Integer adId = 1;
        when(imageService.getAdImage(adId)).thenThrow(new IOException("Not found"));

        // When
        ResponseEntity<byte[]> response = imageController.getAdImage(adId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("GET /images/users/{userId}/avatar - проверка вызовов сервиса")
    void getUserAvatar_ServiceCalls() throws Exception {
        // Given
        Integer userId = 1;
        byte[] imageBytes = "avatar data".getBytes();
        String contentType = "image/jpeg";

        when(imageService.getUserAvatar(userId)).thenReturn(imageBytes);
        when(imageService.getImageContentType(userId, "user")).thenReturn(contentType);

        // When
        imageController.getUserAvatar(userId);

        // Then
        verify(imageService, times(1)).getUserAvatar(userId);
        verify(imageService, times(1)).getImageContentType(userId, "user");
        verifyNoMoreInteractions(imageService);
    }

}