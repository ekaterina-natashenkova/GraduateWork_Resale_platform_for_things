package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.enums.Role;
import ru.skypro.homework.model.dto.User;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithRoleField() {
        // Given
        User user = new User();

        // When
        user.setId(1);
        user.setEmail("user@example.com");
        user.setFirstName("Ivan");
        user.setLastName("Petrov");
        user.setPhone("+79998887766");
        user.setImage("/images/avatar.jpg");
        user.setRole(Role.ADMIN);  // Используем отдельный класс Role

        // Then
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Petrov", user.getLastName());
        assertEquals("+79998887766", user.getPhone());
        assertEquals("/images/avatar.jpg", user.getImage());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldCreateUserWithUserRole() {
        // Given
        User user = new User();

        // When
        user.setRole(Role.USER);

        // Then
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void shouldHandleRoleChanges() {
        // Given
        User user = new User();
        user.setRole(Role.USER);

        // When
        user.setRole(Role.ADMIN);

        // Then
        assertEquals(Role.ADMIN, user.getRole());
    }

}