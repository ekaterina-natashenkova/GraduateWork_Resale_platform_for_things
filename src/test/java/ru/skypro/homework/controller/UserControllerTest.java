package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.security.CustomUserDetailsManager;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.model.dto.NewPassword;
import ru.skypro.homework.model.dto.UpdateUser;
import ru.skypro.homework.model.dto.User;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Mock
    private CustomUserDetailsManager userDetailsManager;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Настраиваем мок аутентификации для всех тестов
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void setPassword_ShouldReturnOk() {
        // Given
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword");
        newPassword.setNewPassword("newPassword");

        // When
        ResponseEntity<Void> response = userController.setPassword(newPassword, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем, что метод был вызван с правильными параметрами
        verify(userDetailsManager).changePassword(
                "test@example.com",
                "oldPassword",
                "newPassword"
        );
    }

    @Test
    void setPassword_WithNullPasswords_ShouldReturnOk() {
        // Given
        NewPassword newPassword = new NewPassword();

        // When
        ResponseEntity<Void> response = userController.setPassword(newPassword, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void setPassword_WhenPasswordIncorrect_ShouldReturnBadRequest() {
        // Given
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("wrongPassword");
        newPassword.setNewPassword("newPassword");

        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userDetailsManager).changePassword(anyString(), anyString(), anyString());

        // When
        ResponseEntity<Void> response = userController.setPassword(newPassword, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getUser_ShouldReturnOkWithUser() {
        // Given
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        when(userService.getCurrentUser()).thenReturn(mockUser);

        // When
        ResponseEntity<User> response = userController.getUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void updateUser_ShouldReturnOkWithUser() {
        // Given
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("John");
        updateUser.setLastName("Doe");
        updateUser.setPhone("+79991234567");

        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("test@example.com");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setPhone("+79991234567");

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userService.updateUser(eq(1), any(User.class))).thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<User> response = userController.updateUser(updateUser, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
    }

    @Test
    void updateUser_WithNullData_ShouldReturnOk() {
        // Given
        UpdateUser updateUser = new UpdateUser();

        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("test@example.com");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("test@example.com");

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userService.updateUser(eq(1), any(User.class))).thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<User> response = userController.updateUser(updateUser, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("John");

        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("test@example.com");

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userService.updateUser(eq(1), any(User.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.updateUser(updateUser, authentication);
        });
    }

    @Test
    void updateUserImage_ShouldReturnOk() throws IOException {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "avatar.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(imageService.saveUserImage(any(MultipartFile.class), eq("test@example.com")))
                .thenReturn("/images/users/test_avatar.jpg");

        // When
        ResponseEntity<String> response = userController.updateUserImage(imageFile, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("/images/users/test_avatar.jpg", response.getBody());

        verify(imageService).saveUserImage(imageFile, "test@example.com");
    }

    @Test
    void updateUserImage_WhenIOException_ShouldReturnBadRequest() throws IOException {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "avatar.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(imageService.saveUserImage(any(MultipartFile.class), anyString()))
                .thenThrow(new IOException("File save failed"));

        // When
        ResponseEntity<String> response = userController.updateUserImage(imageFile, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}