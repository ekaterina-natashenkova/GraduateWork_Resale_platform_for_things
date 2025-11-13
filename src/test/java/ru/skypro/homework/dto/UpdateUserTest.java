package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.UpdateUser;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserTest {

    @Test
    void shouldCreateUpdateUserWithAllFields() {
        // Given
        UpdateUser updateUser = new UpdateUser();

        // When
        updateUser.setFirstName("Petr");
        updateUser.setLastName("Sidorov");
        updateUser.setPhone("+79997776655");

        // Then
        assertEquals("Petr", updateUser.getFirstName());
        assertEquals("Sidorov", updateUser.getLastName());
        assertEquals("+79997776655", updateUser.getPhone());
    }

    @Test
    void shouldHandleEmptyValues() {
        // Given
        UpdateUser updateUser = new UpdateUser();

        // When
        updateUser.setFirstName("");
        updateUser.setLastName("");
        updateUser.setPhone("");

        // Then
        assertEquals("", updateUser.getFirstName());
        assertEquals("", updateUser.getLastName());
        assertEquals("", updateUser.getPhone());
    }

}