package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.skypro.homework.dto.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Test
    void getComments_ShouldReturnOkWithEmptyComments() {
        // Given
        Integer adId = 1;

        // When
        ResponseEntity<Comments> response = commentController.getComments(adId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getCount());
        assertNull(response.getBody().getResults());
    }

    @Test
    void addComment_ShouldReturnOkWithComment() {
        // Given
        Integer adId = 1;
        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();
        createOrUpdateComment.setText("Test comment text");

        // When
        ResponseEntity<Comment> response = commentController.addComment(adId, createOrUpdateComment);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Comment.class, response.getBody());
    }

    @Test
    void addComment_WithNullComment_ShouldReturnOk() {
        // Given
        Integer adId = 1;
        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();

        // When
        ResponseEntity<Comment> response = commentController.addComment(adId, createOrUpdateComment);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteComment_ShouldReturnOk() {
        // Given
        Integer adId = 1;
        Integer commentId = 1;

        // When
        ResponseEntity<Void> response = commentController.deleteComment(adId, commentId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateComment_ShouldReturnOkWithComment() {
        // Given
        Integer adId = 1;
        Integer commentId = 1;
        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();
        createOrUpdateComment.setText("Updated comment text");

        // When
        ResponseEntity<Comment> response = commentController.updateComment(adId, commentId, createOrUpdateComment);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Comment.class, response.getBody());
    }

    @Test
    void updateComment_WithDifferentIds_ShouldReturnOk() {
        // Given
        Integer adId = 999;
        Integer commentId = 888;
        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();

        // When
        ResponseEntity<Comment> response = commentController.updateComment(adId, commentId, createOrUpdateComment);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

}