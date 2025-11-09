package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedAdTest {

    @Test
    void shouldCreateExtendedAdWithAllFields() {
        // Given
        ExtendedAd extendedAd = new ExtendedAd();

        // When
        extendedAd.setPk(1);
        extendedAd.setAuthorFirstName("Ivan");
        extendedAd.setAuthorLastName("Petrov");
        extendedAd.setDescription("Отличный телефон");
        extendedAd.setEmail("ivan@example.com");
        extendedAd.setImage("/images/ad1.jpg");
        extendedAd.setPhone("+79991234567");
        extendedAd.setPrice(50000);
        extendedAd.setTitle("iPhone 13");

        // Then
        assertEquals(1, extendedAd.getPk());
        assertEquals("Ivan", extendedAd.getAuthorFirstName());
        assertEquals("Petrov", extendedAd.getAuthorLastName());
        assertEquals("Отличный телефон", extendedAd.getDescription());
        assertEquals("ivan@example.com", extendedAd.getEmail());
        assertEquals("/images/ad1.jpg", extendedAd.getImage());
        assertEquals("+79991234567", extendedAd.getPhone());
        assertEquals(50000, extendedAd.getPrice());
        assertEquals("iPhone 13", extendedAd.getTitle());
    }

}