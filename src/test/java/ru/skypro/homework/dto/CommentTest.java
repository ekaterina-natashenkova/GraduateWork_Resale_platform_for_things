package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void shouldCreateCommentWithAllFields() {
        // Given
        Comment comment = new Comment();

        // When
        comment.setAuthor(123);
        comment.setAuthorImage("/images/avatar.jpg");
        comment.setAuthorFirstName("Maria");
        comment.setCreatedAt(1700000000000L);
        comment.setPk(1);
        comment.setText("Отличный товар!");

        // Then
        assertEquals(123, comment.getAuthor());
        assertEquals("/images/avatar.jpg", comment.getAuthorImage());
        assertEquals("Maria", comment.getAuthorFirstName());
        assertEquals(1700000000000L, comment.getCreatedAt());
        assertEquals(1, comment.getPk());
        assertEquals("Отличный товар!", comment.getText());
    }

}