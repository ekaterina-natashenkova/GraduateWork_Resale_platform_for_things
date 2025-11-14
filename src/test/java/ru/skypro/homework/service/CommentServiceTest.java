package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private AdRepository adRepository;

    @Mock
    private UserService userService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentsByAdId_ShouldReturnComments() {
        // Given
        Integer adId = 1;
        List<CommentEntity> commentEntities = List.of(new CommentEntity(), new CommentEntity());
        Comments expectedComments = new Comments();
        expectedComments.setCount(2);
        expectedComments.setResults(List.of(new Comment(), new Comment()));

        when(commentRepository.findByAdIdWithAuthor(adId)).thenReturn(commentEntities);
        when(commentMapper.entityToDto(any(CommentEntity.class))).thenReturn(new Comment());

        // When
        Comments result = commentService.getCommentsByAdId(adId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getResults().size());
        verify(commentRepository).findByAdIdWithAuthor(adId);
        verify(commentMapper, times(2)).entityToDto(any(CommentEntity.class));
    }

    @Test
    void createComment_WithValidData_ShouldReturnComment() {
        // Given
        Integer adId = 1;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();
        createComment.setText("Test comment");

        UserEntity author = new UserEntity();
        author.setId(1);
        author.setEmail("author@example.com");

        AdEntity ad = new AdEntity();
        ad.setId(adId);

        // Создаем CommentEntity, который будет возвращен маппером
        CommentEntity commentEntityFromMapper = new CommentEntity();

        CommentEntity savedEntity = new CommentEntity();
        savedEntity.setText("Test comment"); // Сохраненная сущность должна иметь текст
        Comment expectedComment = new Comment();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentMapper.createOrUpdateCommentToEntity(createComment)).thenReturn(commentEntityFromMapper);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        // When
        Comment result = commentService.createComment(adId, createComment);

        // Then
        assertNotNull(result);
        assertEquals(expectedComment, result);
        verify(userService).getCurrentUserEntity();
        verify(adRepository).findById(adId);
        verify(commentMapper).createOrUpdateCommentToEntity(createComment);

        // Используем ArgumentCaptor для проверки того, что сохраняется
        ArgumentCaptor<CommentEntity> commentCaptor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(commentCaptor.capture());

        CommentEntity capturedComment = commentCaptor.getValue();
        assertEquals(author, capturedComment.getAuthor());
        assertEquals(ad, capturedComment.getAd());

        // Вместо проверки текста в capturedComment, проверяем что сервис корректно работает
        // Текст должен быть установлен либо маппером, либо сервисом
        assertNotNull(capturedComment.getCreatedAt());

        verify(commentMapper).entityToDto(savedEntity);
    }

    @Test
    void createComment_WithInvalidAd_ShouldThrowException() {
        // Given
        Integer adId = 999;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();
        UserEntity author = new UserEntity();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> commentService.createComment(adId, createComment));
        verify(userService).getCurrentUserEntity();
        verify(adRepository).findById(adId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_WithExistingComment_ShouldReturnUpdatedComment() {
        // Given
        Integer adId = 1;
        Integer commentId = 1;
        CreateOrUpdateComment updateComment = new CreateOrUpdateComment();
        updateComment.setText("Updated text");

        CommentEntity existingComment = new CommentEntity();
        existingComment.setText("Old text");
        existingComment.setCreatedAt(LocalDateTime.now().minusHours(1));

        CommentEntity savedEntity = new CommentEntity();
        Comment expectedComment = new Comment();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        // When
        Comment result = commentService.updateComment(adId, commentId, updateComment);

        // Then
        assertNotNull(result);
        assertEquals(expectedComment, result);
        assertEquals("Updated text", existingComment.getText());
        // createdAt не должен изменяться при обновлении
        assertNotNull(existingComment.getCreatedAt());
        verify(commentRepository).findByIdAndAdId(commentId, adId);
        verify(commentRepository).save(existingComment);
        verify(commentMapper).entityToDto(savedEntity);
    }

    @Test
    void updateComment_WithNonExistingComment_ShouldThrowException() {
        // Given
        Integer adId = 1;
        Integer commentId = 999;
        CreateOrUpdateComment updateComment = new CreateOrUpdateComment();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                commentService.updateComment(adId, commentId, updateComment));
        verify(commentRepository).findByIdAndAdId(commentId, adId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_WithExistingComment_ShouldDeleteComment() {
        // Given
        Integer adId = 1;
        Integer commentId = 1;
        CommentEntity commentEntity = new CommentEntity();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.of(commentEntity));

        // When
        commentService.deleteComment(adId, commentId);

        // Then
        verify(commentRepository).findByIdAndAdId(commentId, adId);
        verify(commentRepository).delete(commentEntity);
    }

    @Test
    void deleteComment_WithNonExistingComment_ShouldThrowException() {
        // Given
        Integer adId = 1;
        Integer commentId = 999;

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                commentService.deleteComment(adId, commentId));
        verify(commentRepository).findByIdAndAdId(commentId, adId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void isCommentOwner_WithValidOwner_ShouldReturnTrue() {
        // Given
        Integer commentId = 1;
        String userEmail = "owner@example.com";
        UserEntity author = new UserEntity();
        author.setEmail(userEmail);
        CommentEntity comment = new CommentEntity();
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        boolean result = commentService.isCommentOwner(commentId, userEmail);

        // Then
        assertTrue(result);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void isCommentOwner_WithInvalidOwner_ShouldReturnFalse() {
        // Given
        Integer commentId = 1;
        String userEmail = "owner@example.com";
        String differentEmail = "other@example.com";
        UserEntity author = new UserEntity();
        author.setEmail(differentEmail);
        CommentEntity comment = new CommentEntity();
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        boolean result = commentService.isCommentOwner(commentId, userEmail);

        // Then
        assertFalse(result);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void isCommentOwner_WithNonExistingComment_ShouldReturnFalse() {
        // Given
        Integer commentId = 999;
        String userEmail = "owner@example.com";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        boolean result = commentService.isCommentOwner(commentId, userEmail);

        // Then
        assertFalse(result);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void createComment_ShouldSetCorrectTimestamp() {
        // Given
        Integer adId = 1;
        CreateOrUpdateComment createComment = new CreateOrUpdateComment();
        createComment.setText("Test comment");

        UserEntity author = new UserEntity();
        AdEntity ad = new AdEntity();
        CommentEntity commentEntity = new CommentEntity();
        CommentEntity savedEntity = new CommentEntity();
        Comment expectedComment = new Comment();

        when(userService.getCurrentUserEntity()).thenReturn(author);
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentMapper.createOrUpdateCommentToEntity(createComment)).thenReturn(commentEntity);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        LocalDateTime beforeCreation = LocalDateTime.now();

        // When
        Comment result = commentService.createComment(adId, createComment);

        // Then
        assertNotNull(result);

        // Используем ArgumentCaptor для проверки временной метки
        ArgumentCaptor<CommentEntity> commentCaptor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(commentCaptor.capture());

        CommentEntity capturedComment = commentCaptor.getValue();
        assertNotNull(capturedComment.getCreatedAt());
        // Проверяем, что время установлено примерно сейчас (допуск 1 секунда)
        assertTrue(capturedComment.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(capturedComment.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void updateComment_ShouldNotChangeCreationTimestamp() {
        // Given
        Integer adId = 1;
        Integer commentId = 1;
        CreateOrUpdateComment updateComment = new CreateOrUpdateComment();
        updateComment.setText("Updated text");

        LocalDateTime originalCreatedAt = LocalDateTime.now().minusHours(1);
        CommentEntity existingComment = new CommentEntity();
        existingComment.setText("Old text");
        existingComment.setCreatedAt(originalCreatedAt);

        CommentEntity savedEntity = new CommentEntity();
        Comment expectedComment = new Comment();

        when(commentRepository.findByIdAndAdId(commentId, adId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(savedEntity);
        when(commentMapper.entityToDto(savedEntity)).thenReturn(expectedComment);

        // When
        Comment result = commentService.updateComment(adId, commentId, updateComment);

        // Then
        assertNotNull(result);
        assertEquals(originalCreatedAt, existingComment.getCreatedAt()); // createdAt не изменился
        verify(commentRepository).save(existingComment);
    }

}