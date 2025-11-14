package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private UserService userService;

    @Mock
    private MultipartFile multipartFile;

    @TempDir
    Path tempDir;

    private final String testImagesPath = "uploads/images";

    @BeforeEach
    void setUp() {
        // Устанавливаем временный путь для тестов
        ReflectionTestUtils.setField(imageService, "imagesPath", tempDir.toString());
    }

    @Test
    void saveUserImage_ShouldSaveImageAndUpdateUser() throws IOException {
        // Given
        String userEmail = "test@example.com";
        String originalFilename = "avatar.jpg";
        byte[] fileContent = "fake image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveUserImage(multipartFile, userEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/images/users/user_test@example.com_"));
        assertTrue(result.endsWith(".jpg"));

        // Проверяем, что файл был создан
        String filename = result.replace("/images/users/", "");
        Path expectedFile = tempDir.resolve("users").resolve(filename);
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(fileContent, Files.readAllBytes(expectedFile));

        // Проверяем, что UserService был вызван
        verify(userService).updateUserImage(userEmail, result);
    }

    @Test
    void saveUserImage_WithPngExtension_ShouldSaveWithCorrectExtension() throws IOException {
        // Given
        String userEmail = "user@test.com";
        String originalFilename = "profile.png";
        byte[] fileContent = "png content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveUserImage(multipartFile, userEmail);

        // Then
        assertTrue(result.endsWith(".png"));
    }

    @Test
    void saveUserImage_WithNoExtension_ShouldUseDefaultExtension() throws IOException {
        // Given
        String userEmail = "user@test.com";
        String originalFilename = "avatar"; // без расширения
        byte[] fileContent = "image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveUserImage(multipartFile, userEmail);

        // Then
        assertTrue(result.endsWith(".jpg")); // default extension
    }

    @Test
    void saveUserImage_WithNullFilename_ShouldUseDefaultExtension() throws IOException {
        // Given
        String userEmail = "user@test.com";
        byte[] fileContent = "image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveUserImage(multipartFile, userEmail);

        // Then
        assertTrue(result.endsWith(".jpg")); // default extension
    }

    @Test
    void saveUserImage_WhenDirectoryCreationFails_ShouldThrowException() {
        // Given
        String userEmail = "test@example.com";
        String originalFilename = "avatar.jpg";

        // Создаем файл вместо директории, чтобы вызвать ошибку
        Path usersDir = tempDir.resolve("users");
        try {
            Files.createFile(usersDir); // Создаем файл с именем директории
        } catch (IOException e) {
            fail("Failed to setup test");
        }

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);

        // When & Then
        assertThrows(IOException.class, () -> {
            imageService.saveUserImage(multipartFile, userEmail);
        });
    }

    @Test
    void saveAdImage_WithAdId_ShouldSaveImageWithAdPrefix() throws IOException {
        // Given
        Integer adId = 123;
        String originalFilename = "product.jpg";
        byte[] fileContent = "ad image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveAdImage(multipartFile, adId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/images/ads/ad_123_"));
        assertTrue(result.endsWith(".jpg"));

        // Проверяем, что файл был создан
        String filename = result.replace("/images/ads/", "");
        Path expectedFile = tempDir.resolve("ads").resolve(filename);
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(fileContent, Files.readAllBytes(expectedFile));
    }

    @Test
    void saveAdImage_WithNullAdId_ShouldSaveImageWithTempPrefix() throws IOException {
        // Given
        String originalFilename = "temp.jpg";
        byte[] fileContent = "temp image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveAdImage(multipartFile, null);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/images/ads/ad_temp_"));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    void saveAdImage_WithComplexExtension_ShouldPreserveExtension() throws IOException {
        // Given
        Integer adId = 456;
        String originalFilename = "product.image.jpeg"; // сложное расширение
        byte[] fileContent = "image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getBytes()).thenReturn(fileContent);

        // When
        String result = imageService.saveAdImage(multipartFile, adId);

        // Then
        assertTrue(result.endsWith(".jpeg")); // берется последнее расширение
    }

    @Test
    void getImage_ShouldReturnFileContent() throws IOException {
        // Given
        String imagePath = "/images/users/avatar.jpg";
        byte[] expectedContent = "test image content".getBytes();

        // Создаем тестовый файл
        Path testFile = tempDir.resolve("users").resolve("avatar.jpg");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, expectedContent);

        // When
        byte[] result = imageService.getImage(imagePath);

        // Then
        assertArrayEquals(expectedContent, result);
    }

    @Test
    void getImage_WithAdsPath_ShouldReturnFileContent() throws IOException {
        // Given
        String imagePath = "/images/ads/ad_123.jpg";
        byte[] expectedContent = "ad image content".getBytes();

        // Создаем тестовый файл
        Path testFile = tempDir.resolve("ads").resolve("ad_123.jpg");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, expectedContent);

        // When
        byte[] result = imageService.getImage(imagePath);

        // Then
        assertArrayEquals(expectedContent, result);
    }

    @Test
    void getImage_WhenFileNotFound_ShouldThrowIOException() {
        // Given
        String imagePath = "/images/users/nonexistent.jpg";

        // When & Then
        assertThrows(IOException.class, () -> {
            imageService.getImage(imagePath);
        });
    }

    @Test
    void getImage_WithNestedPath_ShouldHandleCorrectly() throws IOException {
        // Given
        String imagePath = "/images/users/2024/01/avatar.jpg";
        byte[] expectedContent = "nested image content".getBytes();

        // Создаем тестовый файл во вложенной директории
        Path testFile = tempDir.resolve("users/2024/01/avatar.jpg");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, expectedContent);

        // When
        byte[] result = imageService.getImage(imagePath);

        // Then
        assertArrayEquals(expectedContent, result);
    }

    @Test
    void deleteImage_ShouldDeleteExistingFile() throws IOException {
        // Given
        String imagePath = "/images/users/to_delete.jpg";

        // Создаем файл для удаления
        Path testFile = tempDir.resolve("users").resolve("to_delete.jpg");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, "content to delete".getBytes());
        assertTrue(Files.exists(testFile));

        // When
        imageService.deleteImage(imagePath);

        // Then
        assertFalse(Files.exists(testFile));
    }

    @Test
    void deleteImage_WhenFileNotFound_ShouldNotThrowException() throws IOException {
        // Given
        String imagePath = "/images/users/nonexistent.jpg";

        // When & Then (не должно быть исключения)
        assertDoesNotThrow(() -> {
            imageService.deleteImage(imagePath);
        });
    }

    @Test
    void deleteImage_WithAdsPath_ShouldDeleteFile() throws IOException {
        // Given
        String imagePath = "/images/ads/ad_999.jpg";

        // Создаем файл для удаления
        Path testFile = tempDir.resolve("ads").resolve("ad_999.jpg");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, "ad content".getBytes());
        assertTrue(Files.exists(testFile));

        // When
        imageService.deleteImage(imagePath);

        // Then
        assertFalse(Files.exists(testFile));
    }

    @Test
    void getFileExtension_WithNormalFilename_ShouldReturnExtension() {
        // Given
        String originalFilename = "image.jpg";
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);

        // Use reflection to test private method
        String result = (String) ReflectionTestUtils.invokeMethod(imageService, "getFileExtension", multipartFile);

        // Then
        assertEquals(".jpg", result);
    }

    @Test
    void getFileExtension_WithMultipleDots_ShouldReturnLastExtension() {
        // Given
        String originalFilename = "image.backup.jpg";
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);

        // Use reflection to test private method
        String result = (String) ReflectionTestUtils.invokeMethod(imageService, "getFileExtension", multipartFile);

        // Then
        assertEquals(".jpg", result);
    }

    @Test
    void getFileExtension_WithNoExtension_ShouldReturnDefault() {
        // Given
        String originalFilename = "avatar";
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);

        // Use reflection to test private method
        String result = (String) ReflectionTestUtils.invokeMethod(imageService, "getFileExtension", multipartFile);

        // Then
        assertEquals(".jpg", result);
    }

    @Test
    void getFileExtension_WithNullFilename_ShouldReturnDefault() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        // Use reflection to test private method
        String result = (String) ReflectionTestUtils.invokeMethod(imageService, "getFileExtension", multipartFile);

        // Then
        assertEquals(".jpg", result);
    }

    @Test
    void generateFilename_ShouldCreateUniqueFilename() {
        // Use reflection to test private method
        String result1 = (String) ReflectionTestUtils.invokeMethod(
                imageService, "generateFilename", "prefix", ".jpg"
        );
        String result2 = (String) ReflectionTestUtils.invokeMethod(
                imageService, "generateFilename", "prefix", ".jpg"
        );

        // Then - filenames should be different due to UUID
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.startsWith("prefix_"));
        assertTrue(result1.endsWith(".jpg"));
        assertNotEquals(result1, result2);
    }

}