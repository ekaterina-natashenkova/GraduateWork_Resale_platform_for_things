package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.model.enums.Role;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    @Test
    void shouldCreateRegisterWithRoleField() {
        // Given
        Register register = new Register();

        // When
        register.setUsername("test@example.com");
        register.setPassword("password123");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+79991234567");
        register.setRole(Role.USER);  // Используем отдельный класс Role

        // Then
        assertNotNull(register);
        assertEquals("test@example.com", register.getUsername());
        assertEquals("password123", register.getPassword());
        assertEquals("John", register.getFirstName());
        assertEquals("Doe", register.getLastName());
        assertEquals("+79991234567", register.getPhone());
        assertEquals(Role.USER, register.getRole());
    }

    @Test
    void shouldCreateRegisterWithAdminRole() {
        // Given
        Register register = new Register();

        // When
        register.setRole(Role.ADMIN);

        // Then
        assertEquals(Role.ADMIN, register.getRole());
    }

    @Test
    void shouldHandleNullRole() {
        // Given
        Register register = new Register();

        // When
        register.setRole(null);

        // Then
        assertNull(register.getRole());
    }

}