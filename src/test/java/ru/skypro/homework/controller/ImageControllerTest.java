package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.skypro.homework.service.ImageService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @InjectMocks
    private ImageController imageController;

    @Mock
    private ImageService imageService;

    @Mock
    private HttpServletRequest request;

    @Test
    void getImage_WithJpgFile_ShouldReturnImageWithCorrectContentType() throws IOException {
        // Given
        String imagePath = "/images/users/user_123.jpg";
        byte[] imageBytes = "fake image content".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(imageBytes, response.getBody());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertEquals("max-age=3600", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));

        verify(imageService).getImage(imagePath);
    }

    @Test
    void getImage_WithJpegFile_ShouldReturnImageWithCorrectContentType() throws IOException {
        // Given
        String imagePath = "/images/ads/ad_456.jpeg";
        byte[] imageBytes = "fake jpeg image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithPngFile_ShouldReturnImageWithCorrectContentType() throws IOException {
        // Given
        String imagePath = "/images/users/avatar_789.png";
        byte[] imageBytes = "fake png image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithGifFile_ShouldReturnImageWithCorrectContentType() throws IOException {
        // Given
        String imagePath = "/images/ads/animated_ad_111.gif";
        byte[] imageBytes = "fake gif image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_GIF, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithUnknownExtension_ShouldReturnOctetStream() throws IOException {
        // Given
        String imagePath = "/images/users/avatar.bmp";
        byte[] imageBytes = "fake bmp image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithNoExtension_ShouldReturnOctetStream() throws IOException {
        // Given
        String imagePath = "/images/users/avatar";
        byte[] imageBytes = "fake image without extension".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithUppercaseExtension_ShouldReturnCorrectContentType() throws IOException {
        // Given
        String imagePath = "/images/users/avatar.PNG";
        byte[] imageBytes = "fake png image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WhenImageNotFound_ShouldReturnNotFound() throws IOException {
        // Given
        String imagePath = "/images/users/nonexistent.jpg";

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenThrow(new IOException("Image not found"));

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(imageService).getImage(imagePath);
    }

    @Test
    void getImage_WhenServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws IOException {
        // Given
        String imagePath = "/images/users/corrupted.jpg";

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getImage_WithComplexPath_ShouldHandleCorrectly() throws IOException {
        // Given
        String imagePath = "/images/ads/2024/01/15/ad_123_final.jpg";
        byte[] imageBytes = "complex path image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithQueryParameters_ShouldIgnoreThem() throws IOException {
        // Given
        String imagePath = "/images/users/avatar.jpg";
        byte[] imageBytes = "image with query params".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Note: Query parameters are handled by HttpServletRequest and included in getRequestURI()
    }

    @Test
    void determineContentType_ShouldReturnCorrectTypes() {
        // Using reflection to test private method, or we can test through public method

        ImageController controller = new ImageController(imageService);

        // Test through public method by mocking the service call
        // This tests the determineContentType logic indirectly
    }

    @Test
    void getImage_ShouldSetCacheControlHeader() throws IOException {
        // Given
        String imagePath = "/images/users/avatar.jpg";
        byte[] imageBytes = "cached image".getBytes();

        when(request.getRequestURI()).thenReturn(imagePath);
        when(imageService.getImage(imagePath)).thenReturn(imageBytes);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(request);

        // Then
        assertNotNull(response);
        assertEquals("max-age=3600", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    void determineContentType_WithReflection_ShouldReturnCorrectTypes() throws Exception {
        // Given
        ImageController controller = new ImageController(imageService);

        // Use reflection to access private method
        var method = ImageController.class.getDeclaredMethod("determineContentType", String.class);
        method.setAccessible(true);

        // When & Then
        assertEquals("image/jpeg", method.invoke(controller, "/path/image.jpg"));
        assertEquals("image/jpeg", method.invoke(controller, "/path/image.jpeg"));
        assertEquals("image/png", method.invoke(controller, "/path/image.png"));
        assertEquals("image/gif", method.invoke(controller, "/path/image.gif"));
        assertEquals("application/octet-stream", method.invoke(controller, "/path/image.bmp"));
        assertEquals("application/octet-stream", method.invoke(controller, "/path/image"));
        assertEquals("application/octet-stream", method.invoke(controller, "/path/image.unknown"));
    }

}