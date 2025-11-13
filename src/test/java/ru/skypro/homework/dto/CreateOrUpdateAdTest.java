package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;

import static org.junit.jupiter.api.Assertions.*;

class CreateOrUpdateAdTest {

    @Test
    void shouldCreateCreateOrUpdateAdWithAllFields() {
        // Given
        CreateOrUpdateAd ad = new CreateOrUpdateAd();

        // When
        ad.setTitle("MacBook Pro");
        ad.setPrice(150000);
        ad.setDescription("Мощный ноутбук для работы");

        // Then
        assertEquals("MacBook Pro", ad.getTitle());
        assertEquals(150000, ad.getPrice());
        assertEquals("Мощный ноутбук для работы", ad.getDescription());
    }

    @Test
    void shouldHaveNoArgsConstructor() {
        // When
        CreateOrUpdateAd ad = new CreateOrUpdateAd();

        // Then
        assertNotNull(ad);
        assertNull(ad.getTitle());
        assertNull(ad.getPrice());
        assertNull(ad.getDescription());
    }

}