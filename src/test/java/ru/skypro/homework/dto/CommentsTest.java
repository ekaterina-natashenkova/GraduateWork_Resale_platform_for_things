package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentsTest {

    @Test
    void shouldCreateCommentsWithCommentList() {
        // Given
        Comments comments = new Comments();
        Comment comment1 = new Comment();
        comment1.setPk(1);
        comment1.setText("First comment");

        Comment comment2 = new Comment();
        comment2.setPk(2);
        comment2.setText("Second comment");

        List<Comment> commentList = Arrays.asList(comment1, comment2);

        // When
        comments.setCount(2);
        comments.setResults(commentList);

        // Then
        assertEquals(2, comments.getCount());
        assertEquals(2, comments.getResults().size());
        assertEquals("First comment", comments.getResults().get(0).getText());
        assertEquals("Second comment", comments.getResults().get(1).getText());
    }

    @Test
    void shouldHandleNullResults() {
        // Given
        Comments comments = new Comments();

        // When
        comments.setCount(0);
        comments.setResults(null);

        // Then
        assertEquals(0, comments.getCount());
        assertNull(comments.getResults());
    }

}