package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateOrUpdateCommentTest {

    @Test
    void shouldCreateCreateOrUpdateComment() {
        // Given
        CreateOrUpdateComment comment = new CreateOrUpdateComment();

        // When
        comment.setText("Это комментарий к объявлению");

        // Then
        assertEquals("Это комментарий к объявлению", comment.getText());
    }

    @Test
    void shouldHandleLongText() {
        // Given
        CreateOrUpdateComment comment = new CreateOrUpdateComment();
        String longText = "Очень длинный текст комментария, который может превышать обычную длину";

        // When
        comment.setText(longText);

        // Then
        assertEquals(longText, comment.getText());
    }

}