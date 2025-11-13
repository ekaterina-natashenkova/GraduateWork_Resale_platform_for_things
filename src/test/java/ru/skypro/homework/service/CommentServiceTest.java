package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.dto.Comment;
import ru.skypro.homework.model.dto.Comments;
import ru.skypro.homework.model.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.CommentEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentsByAdId_ShouldReturnComments() {

        Integer adId = 1;
        List<CommentEntity> commentEntities = List.of(new CommentEntity(), new CommentEntity());
        List<Comment> comments = List.of(new Comment(), new Comment());

        when(commentRepository.findByAdIdWithAuthor(adId)).thenReturn(commentEntities);
        when(commentMapper.entityToDto(any(CommentEntity.class))).thenReturn(new Comment());

        Comments result = commentService.getCommentsByAdId(adId);

        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getResults().size());
        verify(commentRepository).findByAdIdWithAuthor(adId);
        verify(commentMapper, times(2)).entityToDto(any(CommentEntity.class));
    }

    void createComment_WithValidData_ShouldReturnComment() {

        Integer adId = 1;
        Integer authorId = 1;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();
        createComment.setText("Test comment");

        UserEntity author = new UserEntity();
        author.setId(authorId);
        AdEntity ad = new AdEntity();
        ad.setId(adId);

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText("Test comment"); // Устанавливаем текст вручную

        CommentEntity savedEntity = new CommentEntity();
        savedEntity.setText("Test comment");

        Comment expectedComment = new Comment();

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentMapper.createOrUpdateCommentToEntity(createComment)).thenReturn(commentEntity);
        when(commentRepository.save(commentEntity)).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        Optional<Comment> result = commentService.createComment(adId, authorId, createComment);

        assertTrue(result.isPresent());
        assertEquals(expectedComment, result.get());
        verify(userRepository).findById(authorId);
        verify(adRepository).findById(adId);
        verify(commentRepository).save(commentEntity);
        assertEquals(author, commentEntity.getAuthor());
        assertEquals(ad, commentEntity.getAd());
        assertEquals("Test comment", commentEntity.getText());
        assertNotNull(commentEntity.getCreatedAt());
    }

    @Test
    void createComment_WithInvalidUser_ShouldReturnEmpty() {

        Integer adId = 1;
        Integer authorId = 999;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.createComment(adId, authorId, createComment);

        assertFalse(result.isPresent());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WithInvalidAd_ShouldReturnEmpty() {

        Integer adId = 999;
        Integer authorId = 1;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();

        when(userRepository.findById(authorId)).thenReturn(Optional.of(new UserEntity()));
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.createComment(adId, authorId, createComment);

        assertFalse(result.isPresent());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_WithExistingComment_ShouldReturnUpdatedComment() {

        Integer commentId = 1;
        Integer adId = 1;
        CreateOrUpdateComment updateComment = new CreateOrUpdateComment();
        updateComment.setText("Updated text");

        CommentEntity existingComment = new CommentEntity();
        existingComment.setText("Old text");
        CommentEntity savedEntity = new CommentEntity();
        Comment expectedComment = new Comment();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        Optional<Comment> result = commentService.updateComment(commentId, adId, updateComment);

        assertTrue(result.isPresent());
        assertEquals(expectedComment, result.get());
        assertEquals("Updated text", existingComment.getText());
        verify(commentRepository).save(existingComment);
    }

    @Test
    void updateComment_WithNonExistingComment_ShouldReturnEmpty() {

        Integer commentId = 999;
        Integer adId = 1;
        CreateOrUpdateComment updateComment = new CreateOrUpdateComment();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.updateComment(commentId, adId, updateComment);

        assertFalse(result.isPresent());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_WithExistingComment_ShouldReturnTrue() {

        Integer commentId = 1;
        Integer adId = 1;
        CommentEntity comment = new CommentEntity();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.of(comment));

        boolean result = commentService.deleteComment(commentId, adId);

        assertTrue(result);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_WithNonExistingComment_ShouldReturnFalse() {

        Integer commentId = 999;
        Integer adId = 1;

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.empty());

        boolean result = commentService.deleteComment(commentId, adId);

        assertFalse(result);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void isCommentAuthor_WithValidAuthor_ShouldReturnTrue() {

        Integer commentId = 1;
        Integer authorId = 1;
        CommentEntity comment = new CommentEntity();
        UserEntity author = new UserEntity();
        author.setId(authorId);
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.isCommentAuthor(commentId, authorId);

        assertTrue(result);
    }

    @Test
    void isCommentAuthor_WithInvalidAuthor_ShouldReturnFalse() {

        Integer commentId = 1;
        Integer authorId = 1;
        CommentEntity comment = new CommentEntity();
        UserEntity author = new UserEntity();
        author.setId(999); // Different author ID
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.isCommentAuthor(commentId, authorId);

        assertFalse(result);
    }

    @Test
    void isCommentAuthor_WithNonExistingComment_ShouldReturnFalse() {

        Integer commentId = 999;
        Integer authorId = 1;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        boolean result = commentService.isCommentAuthor(commentId, authorId);

        assertFalse(result);
    }

}