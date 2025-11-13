package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.Ad;

import static org.junit.jupiter.api.Assertions.*;

class AdTest {

    @Test
    void shouldCreateAdWithAllFields() {
        // Given
        Ad ad = new Ad();

        // When
        ad.setAuthor(123);
        ad.setImage("/images/ad1.jpg");
        ad.setPk(1);
        ad.setPrice(50000);
        ad.setTitle("iPhone 13");

        // Then
        assertEquals(123, ad.getAuthor());
        assertEquals("/images/ad1.jpg", ad.getImage());
        assertEquals(1, ad.getPk());
        assertEquals(50000, ad.getPrice());
        assertEquals("iPhone 13", ad.getTitle());
    }

    @Test
    void shouldHandleZeroAndNullValues() {
        // Given
        Ad ad = new Ad();

        // When
        ad.setAuthor(0);
        ad.setImage(null);
        ad.setPk(0);
        ad.setPrice(0);
        ad.setTitle(null);

        // Then
        assertEquals(0, ad.getAuthor());
        assertNull(ad.getImage());
        assertEquals(0, ad.getPk());
        assertEquals(0, ad.getPrice());
        assertNull(ad.getTitle());
    }

}