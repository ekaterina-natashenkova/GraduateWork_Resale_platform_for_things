package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdControllerTest {

    @InjectMocks
    private AdController adController;

    @Test
    void getAllAds_ShouldReturnOkWithEmptyAds() {
        // When
        ResponseEntity<Ads> response = adController.getAllAds();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getCount());
        assertNull(response.getBody().getResults());
    }

    @Test
    void addAd_WithValidParameters_ShouldReturnCreated() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data");

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        // When
        ResponseEntity<Ad> response = adController.addAd(
                request, "Test Ad", "1000", "Test Description", imageFile
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Ad", response.getBody().getTitle());
        assertEquals(1000, response.getBody().getPrice());
    }

    @Test
    void addAd_WithInvalidPrice_ShouldReturnCreatedWithNullPrice() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        ResponseEntity<Ad> response = adController.addAd(
                request, "Test Ad", "invalid", "Test Description", null
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Ad", response.getBody().getTitle());
        assertNull(response.getBody().getPrice()); // Price should be null due to parsing error
    }

    @Test
    void addAd_WithNullParameters_ShouldReturnCreated() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        ResponseEntity<Ad> response = adController.addAd(request, null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getTitle());
        assertNull(response.getBody().getPrice());
    }

    @Test
    void getAds_ShouldReturnOkWithExtendedAd() {
        // Given
        Integer adId = 1;

        // When
        ResponseEntity<ExtendedAd> response = adController.getAds(adId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ExtendedAd.class, response.getBody());
    }

    @Test
    void removeAd_ShouldReturnNoContent() {
        // Given
        Integer adId = 1;

        // When
        ResponseEntity<Void> response = adController.removeAd(adId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateAds_ShouldReturnOkWithAd() {
        // Given
        Integer adId = 1;
        CreateOrUpdateAd updateAd = new CreateOrUpdateAd();
        updateAd.setTitle("Updated Title");
        updateAd.setPrice(2000);
        updateAd.setDescription("Updated Description");

        // When
        ResponseEntity<Ad> response = adController.updateAds(adId, updateAd);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Ad.class, response.getBody());
    }

    @Test
    void getAdsMe_ShouldReturnOkWithEmptyAds() {
        // When
        ResponseEntity<Ads> response = adController.getAdsMe();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getCount());
        assertNull(response.getBody().getResults());
    }

    @Test
    void updateImage_ShouldReturnOk() {
        // Given
        Integer adId = 1;
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        // When
        ResponseEntity<?> response = adController.updateImage(adId, imageFile);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}