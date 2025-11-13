package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdsTest {

    @Test
    void shouldCreateAdsWithEmptyList() {
        // Given
        Ads ads = new Ads();

        // When
        ads.setCount(0);
        ads.setResults(List.of());

        // Then
        assertEquals(0, ads.getCount());
        assertTrue(ads.getResults().isEmpty());
    }

    @Test
    void shouldCreateAdsWithAdList() {
        // Given
        Ads ads = new Ads();
        Ad ad1 = new Ad();
        ad1.setPk(1);
        ad1.setTitle("Ad 1");

        Ad ad2 = new Ad();
        ad2.setPk(2);
        ad2.setTitle("Ad 2");

        List<Ad> adList = Arrays.asList(ad1, ad2);

        // When
        ads.setCount(2);
        ads.setResults(adList);

        // Then
        assertEquals(2, ads.getCount());
        assertEquals(2, ads.getResults().size());
        assertEquals("Ad 1", ads.getResults().get(0).getTitle());
        assertEquals("Ad 2", ads.getResults().get(1).getTitle());
    }

}