package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;

import static org.junit.jupiter.api.Assertions.*;

class AdMapperTest {

    private final AdMapper adMapper = Mappers.getMapper(AdMapper.class);

    @Test
    void entityToAdDto_ShouldMapAllFieldsCorrectly() {

        UserEntity author = new UserEntity();
        author.setId(5);

        AdEntity entity = new AdEntity();
        entity.setId(20);
        entity.setTitle("Test Ad");
        entity.setDescription("Test Description");
        entity.setPrice(1000);
        entity.setImagePath("/images/ad20.jpg");
        entity.setAuthor(author);

        Ad dto = adMapper.entityToAdDto(entity);

        assertNotNull(dto);
        assertEquals(20, dto.getPk());
        assertEquals(5, dto.getAuthor());
        assertEquals("Test Ad", dto.getTitle());
        assertEquals(1000, dto.getPrice());
        assertEquals("/images/ad20.jpg", dto.getImage());
    }

    @Test
    void entityToAdDto_WithNullEntity_ShouldReturnNull() {

        Ad dto = adMapper.entityToAdDto(null);

        assertNull(dto);
    }

    @Test
    void entityToAdDto_WithNullAuthor_ShouldHandleGracefully() {

        AdEntity entity = new AdEntity();
        entity.setId(1);
        entity.setTitle("Ad without author");
        entity.setDescription("Description");
        entity.setPrice(500);
        entity.setImagePath("/images/test.jpg");

        Ad dto = adMapper.entityToAdDto(entity);

        assertNotNull(dto);
        assertEquals(1, dto.getPk());
        assertEquals("Ad without author", dto.getTitle());
        assertNull(dto.getAuthor());
    }

    @Test
    void entityToExtendedAdDto_ShouldMapAllFieldsCorrectly() {

        UserEntity author = new UserEntity();
        author.setId(10);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john.doe@example.com");
        author.setPhone("+79991234567");

        AdEntity entity = new AdEntity();
        entity.setId(30);
        entity.setTitle("Extended Ad");
        entity.setDescription("Extended Description");
        entity.setPrice(2000);
        entity.setImagePath("/images/extended.jpg");
        entity.setAuthor(author);

        ExtendedAd dto = adMapper.entityToExtendedAdDto(entity);

        assertNotNull(dto);
        assertEquals(30, dto.getPk());
        assertEquals("John", dto.getAuthorFirstName());
        assertEquals("Doe", dto.getAuthorLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("+79991234567", dto.getPhone());
        assertEquals("Extended Ad", dto.getTitle());
        assertEquals("Extended Description", dto.getDescription());
        assertEquals(2000, dto.getPrice());
        assertEquals("/images/extended.jpg", dto.getImage());
    }

    @Test
    void entityToExtendedAdDto_WithNullAuthor_ShouldHandleGracefully() {

        AdEntity entity = new AdEntity();
        entity.setId(2);
        entity.setTitle("Ad with null author");
        entity.setDescription("Desc");
        entity.setPrice(300);
        entity.setImagePath("/images/null.jpg");

        ExtendedAd dto = adMapper.entityToExtendedAdDto(entity);

        assertNotNull(dto);
        assertEquals(2, dto.getPk());
        assertEquals("Ad with null author", dto.getTitle());
        assertNull(dto.getAuthorFirstName());
        assertNull(dto.getAuthorLastName());
        assertNull(dto.getEmail());
        assertNull(dto.getPhone());
    }

    @Test
    void createOrUpdateAdToEntity_ShouldMapFieldsCorrectly() {

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("New Ad");
        dto.setDescription("New Description");
        dto.setPrice(1500);

        AdEntity entity = adMapper.createOrUpdateAdToEntity(dto);

        assertNotNull(entity);
        assertEquals("New Ad", entity.getTitle());
        assertEquals("New Description", entity.getDescription());
        assertEquals(1500, entity.getPrice());
        assertNull(entity.getId());
        assertNull(entity.getImagePath());
        assertNull(entity.getAuthor());
        assertNull(entity.getComments());
    }

    @Test
    void createOrUpdateAdToEntity_WithNullDto_ShouldReturnNull() {

        AdEntity entity = adMapper.createOrUpdateAdToEntity(null);

        assertNull(entity);
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyAllowedFields() {

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setPrice(2500);

        AdEntity existingEntity = new AdEntity();
        existingEntity.setId(100);
        existingEntity.setTitle("Original Title");
        existingEntity.setDescription("Original Description");
        existingEntity.setPrice(1000);
        existingEntity.setImagePath("/images/original.jpg");

        UserEntity author = new UserEntity();
        author.setId(1);
        existingEntity.setAuthor(author);

        adMapper.updateEntityFromDto(dto, existingEntity);

        assertEquals("Updated Title", existingEntity.getTitle());
        assertEquals("Updated Description", existingEntity.getDescription());
        assertEquals(2500, existingEntity.getPrice());

        assertEquals(100, existingEntity.getId());
        assertEquals("/images/original.jpg", existingEntity.getImagePath());
        assertEquals(1, existingEntity.getAuthor().getId());
    }

    @Test
    void updateEntityFromDto_WithNullDto_ShouldNotChangeEntity() {

        AdEntity existingEntity = new AdEntity();
        existingEntity.setId(50);
        existingEntity.setTitle("Original");
        existingEntity.setDescription("Original Desc");
        existingEntity.setPrice(500);

        adMapper.updateEntityFromDto(null, existingEntity);

        assertEquals(50, existingEntity.getId());
        assertEquals("Original", existingEntity.getTitle());
        assertEquals("Original Desc", existingEntity.getDescription());
        assertEquals(500, existingEntity.getPrice());
    }

}