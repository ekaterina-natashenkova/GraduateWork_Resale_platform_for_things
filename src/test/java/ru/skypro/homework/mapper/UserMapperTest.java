package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.model.enums.Role;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void entityToDto_ShouldMapAllFieldsCorrectly() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setId(1);
        entity.setFirstName("John");
        entity.setLastName("Doe");
        entity.setEmail("john.doe@example.com");
        entity.setPhone("+79991234567");
        entity.setImagePath("/images/user1.jpg");
        entity.setRole(Role.USER); // если есть enum Role

        // Act
        User dto = userMapper.entityToDto(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("+79991234567", dto.getPhone());
        assertEquals("/images/user1.jpg", dto.getImage());
        assertEquals(Role.USER, dto.getRole());
    }

    @Test
    void entityToDto_WithNullEntity_ShouldReturnNull() {
        // Act
        User dto = userMapper.entityToDto(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void entityToDto_WithNullImagePath_ShouldSetNullImage() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setId(2);
        entity.setFirstName("Jane");
        entity.setLastName("Smith");
        entity.setEmail("jane@example.com");
        entity.setImagePath(null);

        // Act
        User dto = userMapper.entityToDto(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(2, dto.getId());
        assertEquals("Jane", dto.getFirstName());
        assertNull(dto.getImage());
    }

    @Test
    void dtoToEntity_ShouldMapAllFieldsExceptIgnored() {
        // Arrange
        User dto = new User();
        dto.setId(10);
        dto.setFirstName("Alice");
        dto.setLastName("Johnson");
        dto.setEmail("alice@example.com");
        dto.setPhone("+79997654321");
        dto.setImage("/images/alice.jpg");
        dto.setRole(Role.ADMIN);

        // Act
        UserEntity entity = userMapper.dtoToEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals("Alice", entity.getFirstName());
        assertEquals("Johnson", entity.getLastName());
        assertEquals("alice@example.com", entity.getEmail());
        assertEquals("+79997654321", entity.getPhone());
        assertEquals("/images/alice.jpg", entity.getImagePath());
        assertEquals(Role.ADMIN, entity.getRole());

        // Ignored fields should be null
        assertNull(entity.getPassword());
        assertNull(entity.getAds());
        assertNull(entity.getComments());
        // Note: ID is not ignored in this mapping
    }

    @Test
    void dtoToEntity_WithNullDto_ShouldReturnNull() {
        // Act
        UserEntity entity = userMapper.dtoToEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    void dtoToEntity_WithNullImage_ShouldSetNullImagePath() {
        // Arrange
        User dto = new User();
        dto.setId(3);
        dto.setFirstName("Bob");
        dto.setLastName("Brown");
        dto.setEmail("bob@example.com");
        dto.setImage(null);

        // Act
        UserEntity entity = userMapper.dtoToEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals("Bob", entity.getFirstName());
        assertNull(entity.getImagePath());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyAllowedFields() {
        // Arrange
        User dto = new User();
        dto.setId(999); // Should be ignored
        dto.setFirstName("UpdatedFirstName");
        dto.setLastName("UpdatedLastName");
        dto.setEmail("updated@example.com");
        dto.setPhone("+79998887766");
        dto.setImage("/images/updated.jpg");

        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(100); // Should remain
        existingEntity.setFirstName("OriginalFirstName");
        existingEntity.setLastName("OriginalLastName");
        existingEntity.setEmail("original@example.com");
        existingEntity.setPhone("+79991112233");
        existingEntity.setImagePath("/images/original.jpg");
        existingEntity.setPassword("encodedPassword");
        existingEntity.setAds(new ArrayList<>());
        existingEntity.setComments(new ArrayList<>());

        // Act
        userMapper.updateEntityFromDto(dto, existingEntity);

        // Assert
        // Updated fields
        assertEquals("UpdatedFirstName", existingEntity.getFirstName());
        assertEquals("UpdatedLastName", existingEntity.getLastName());
        assertEquals("updated@example.com", existingEntity.getEmail());
        assertEquals("+79998887766", existingEntity.getPhone());

        // Ignored fields should remain unchanged
        assertEquals(100, existingEntity.getId());
        assertEquals("/images/original.jpg", existingEntity.getImagePath());
        assertEquals("encodedPassword", existingEntity.getPassword());
        assertNotNull(existingEntity.getAds());
        assertNotNull(existingEntity.getComments());
    }

    @Test
    void updateEntityFromDto_WithNullDto_ShouldNotChangeEntity() {
        // Arrange
        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(50);
        existingEntity.setFirstName("Original");
        existingEntity.setLastName("User");
        existingEntity.setEmail("original@example.com");

        // Act
        userMapper.updateEntityFromDto(null, existingEntity);

        // Assert
        assertEquals(50, existingEntity.getId());
        assertEquals("Original", existingEntity.getFirstName());
        assertEquals("User", existingEntity.getLastName());
        assertEquals("original@example.com", existingEntity.getEmail());
    }

    @Test
    void updateEntityFromDto_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        User dto = new User();
        dto.setFirstName("PartialUpdate"); // Only first name provided
        dto.setLastName("ShouldRemain"); // Явно устанавливаем остальные поля, чтобы они не были null
        dto.setEmail("remain@example.com");
        dto.setPhone("+79990001122");
        dto.setImage("/images/remain.jpg");

        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(200);
        existingEntity.setFirstName("Original");
        existingEntity.setLastName("ShouldRemain");
        existingEntity.setEmail("remain@example.com");
        existingEntity.setPhone("+79990001122");
        existingEntity.setImagePath("/images/remain.jpg");

        // Act
        userMapper.updateEntityFromDto(dto, existingEntity);

        // Assert
        assertEquals("PartialUpdate", existingEntity.getFirstName());
        assertEquals("ShouldRemain", existingEntity.getLastName());
        assertEquals("remain@example.com", existingEntity.getEmail());
        assertEquals("+79990001122", existingEntity.getPhone());
        assertEquals("/images/remain.jpg", existingEntity.getImagePath());
    }

}