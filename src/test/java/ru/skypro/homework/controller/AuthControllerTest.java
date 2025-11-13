package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.skypro.homework.model.dto.Login;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_WithValidCredentials_ShouldReturnOk() {
        // Given
        Login login = new Login();
        login.setUsername("user@example.com");
        login.setPassword("password123");

        when(authService.login("user@example.com", "password123")).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.login(login);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).login("user@example.com", "password123");
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() {
        // Given
        Login login = new Login();
        login.setUsername("user@example.com");
        login.setPassword("wrongpassword");

        when(authService.login("user@example.com", "wrongpassword")).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.login(login);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService, times(1)).login("user@example.com", "wrongpassword");
    }

    @Test
    void login_WithNullCredentials_ShouldCallService() {
        // Given
        Login login = new Login();
        login.setUsername(null);
        login.setPassword(null);

        when(authService.login(null, null)).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.login(login);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService, times(1)).login(null, null);
    }

    @Test
    void register_WithValidData_ShouldReturnCreated() {
        // Given
        Register register = new Register();
        register.setUsername("newuser@example.com");
        register.setPassword("password123");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+79991234567");

        when(authService.register(register)).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.register(register);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService, times(1)).register(register);
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() {
        // Given
        Register register = new Register();
        register.setUsername("existing@example.com");

        when(authService.register(register)).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.register(register);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(authService, times(1)).register(register);
    }

    @Test
    void register_WithNullData_ShouldCallService() {
        // Given
        Register register = new Register();

        when(authService.register(register)).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.register(register);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(authService, times(1)).register(register);
    }

}