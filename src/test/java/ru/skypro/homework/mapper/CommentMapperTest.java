package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.model.dto.Comment;
import ru.skypro.homework.model.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.CommentEntity;
import ru.skypro.homework.model.entity.UserEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void entityToDto_ShouldMapAllFieldsCorrectly() {

        UserEntity author = new UserEntity();
        author.setId(1);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setImagePath("/images/user1.jpg");

        AdEntity ad = new AdEntity();
        ad.setId(100);

        CommentEntity entity = new CommentEntity();
        entity.setId(10);
        entity.setText("This is a test comment");
        entity.setAuthor(author);
        entity.setAd(ad);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        Comment dto = commentMapper.entityToDto(entity);

        assertNotNull(dto);
        assertEquals(10, dto.getPk());
        assertEquals(1, dto.getAuthor());
        assertEquals("John", dto.getAuthorFirstName());
        assertEquals("/images/user1.jpg", dto.getAuthorImage());
        assertEquals("This is a test comment", dto.getText());

        LocalDateTime expectedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        long expectedMillis = expectedTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertEquals(expectedMillis, dto.getCreatedAt());
    }

    @Test
    void entityToDto_WithNullEntity_ShouldReturnNull() {

        Comment dto = commentMapper.entityToDto(null);

        assertNull(dto);
    }

    @Test
    void entityToDto_WithNullAuthor_ShouldHandleGracefully() {

        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setText("Test comment");
        entity.setCreatedAt(LocalDateTime.now());

        Comment dto = commentMapper.entityToDto(entity);

        assertNotNull(dto);
        assertEquals(1, dto.getPk());
        assertEquals("Test comment", dto.getText());
        assertNull(dto.getAuthor());
        assertNull(dto.getAuthorFirstName());
        assertNull(dto.getAuthorImage());
    }

    @Test
    void createOrUpdateCommentToEntity_ShouldMapTextCorrectly() {

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("New comment text");

        CommentEntity entity = commentMapper.createOrUpdateCommentToEntity(dto);

        assertNotNull(entity);
        assertEquals("New comment text", entity.getText());
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getAuthor());
        assertNull(entity.getAd());
    }

    @Test
    void createOrUpdateCommentToEntity_WithNullDto_ShouldReturnNull() {

        CommentEntity entity = commentMapper.createOrUpdateCommentToEntity(null);

        assertNull(entity);
    }

    @Test
    void createOrUpdateCommentToEntity_WithNullText_ShouldSetNullText() {

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText(null);

        CommentEntity entity = commentMapper.createOrUpdateCommentToEntity(dto);

        assertNotNull(entity);
        assertNull(entity.getText());
    }

}