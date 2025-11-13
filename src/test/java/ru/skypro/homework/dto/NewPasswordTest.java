package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.NewPassword;

import static org.junit.jupiter.api.Assertions.*;

class NewPasswordTest {

    @Test
    void shouldCreateNewPasswordWithAllFields() {
        // Given
        NewPassword newPassword = new NewPassword();

        // When
        newPassword.setCurrentPassword("oldPassword123");
        newPassword.setNewPassword("newPassword456");

        // Then
        assertEquals("oldPassword123", newPassword.getCurrentPassword());
        assertEquals("newPassword456", newPassword.getNewPassword());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        NewPassword newPassword = new NewPassword();

        // When
        newPassword.setCurrentPassword(null);
        newPassword.setNewPassword(null);

        // Then
        assertNull(newPassword.getCurrentPassword());
        assertNull(newPassword.getNewPassword());
    }

    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        // Given
        NewPassword password1 = new NewPassword();
        password1.setCurrentPassword("password1");
        password1.setNewPassword("password2");

        NewPassword password2 = new NewPassword();
        password2.setCurrentPassword("password1");
        password2.setNewPassword("password2");

        // Then
        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }

    @Test
    void toStringShouldContainAllFields() {
        // Given
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPass");
        newPassword.setNewPassword("newPass");

        // When
        String result = newPassword.toString();

        // Then
        assertTrue(result.contains("oldPass"));
        assertTrue(result.contains("newPass"));
    }

}