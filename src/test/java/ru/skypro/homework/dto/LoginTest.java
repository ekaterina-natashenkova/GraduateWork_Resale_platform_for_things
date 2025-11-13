package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.Login;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

    @Test
    void shouldCreateLoginWithAllFields() {
        // Given
        Login login = new Login();

        // When
        login.setUsername("user@example.com");
        login.setPassword("securePassword123");

        // Then
        assertEquals("user@example.com", login.getUsername());
        assertEquals("securePassword123", login.getPassword());
    }

    @Test
    void shouldHandleEdgeCaseValues() {
        // Given
        Login login = new Login();

        // When
        login.setUsername("a");  // min length edge case
        login.setPassword("12345678");  // min length edge case

        // Then
        assertEquals("a", login.getUsername());
        assertEquals("12345678", login.getPassword());
    }

}