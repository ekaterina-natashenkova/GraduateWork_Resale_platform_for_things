package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.dto.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Test
    void setPassword_ShouldReturnOk() {
        // Given
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword");
        newPassword.setNewPassword("newPassword");

        // When
        ResponseEntity<?> response = userController.setPassword(newPassword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void setPassword_WithNullPasswords_ShouldReturnOk() {
        // Given
        NewPassword newPassword = new NewPassword();

        // When
        ResponseEntity<?> response = userController.setPassword(newPassword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getUser_ShouldReturnOkWithUser() {
        // When
        ResponseEntity<User> response = userController.getUser();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(User.class, response.getBody());
    }

    @Test
    void updateUser_ShouldReturnOkWithUser() {
        // Given
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("John");
        updateUser.setLastName("Doe");
        updateUser.setPhone("+79991234567");

        // When
        ResponseEntity<User> response = userController.updateUser(updateUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(User.class, response.getBody());
    }

    @Test
    void updateUser_WithNullData_ShouldReturnOk() {
        // Given
        UpdateUser updateUser = new UpdateUser();

        // When
        ResponseEntity<User> response = userController.updateUser(updateUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateUserImage_ShouldReturnOk() {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "avatar.jpg", "image/jpeg", "test image content".getBytes()
        );

        // When
        ResponseEntity<?> response = userController.updateUserImage(imageFile);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateUserImage_WithNullFile_ShouldReturnOk() {
        // When
        ResponseEntity<?> response = userController.updateUserImage(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}