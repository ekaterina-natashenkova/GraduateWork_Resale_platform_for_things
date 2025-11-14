package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;

    /**
     * Проверка, является ли пользователь владельцем комментария - используется в @PreAuthorize
     */
    public boolean isCommentOwner(Integer commentId, String userEmail) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getAuthor().getEmail().equals(userEmail))
                .orElse(false);
    }

    public Comments getCommentsByAdId(Integer adId) {
        List<CommentEntity> commentEntities = commentRepository.findByAdIdWithAuthor(adId);
        List<Comment> comments = commentEntities.stream()
                .map(commentMapper::entityToDto)
                .collect(Collectors.toList());

        Comments result = new Comments();
        result.setCount(comments.size());
        result.setResults(comments);
        return result;
    }

    /**
     * Создать комментарий (автор определяется из SecurityContext)
     */
    public Comment createComment(Integer adId, CreateOrUpdateComment createComment) {
        UserEntity author = userService.getCurrentUserEntity();
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        CommentEntity commentEntity = commentMapper.createOrUpdateCommentToEntity(createComment);
        commentEntity.setAuthor(author);
        commentEntity.setAd(ad);
        commentEntity.setCreatedAt(LocalDateTime.now());

        CommentEntity savedEntity = commentRepository.save(commentEntity);
        return commentMapper.entityToDto(savedEntity);
    }

    /**
     * Обновить комментарий с проверкой прав через @PreAuthorize
     */
    @PreAuthorize("hasRole('ADMIN') or @commentService.isCommentOwner(#commentId, authentication.name)")
    public Comment updateComment(Integer adId, Integer commentId, CreateOrUpdateComment updateComment) {
        CommentEntity commentEntity = commentRepository.findByIdAndAdId(commentId, adId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        commentEntity.setText(updateComment.getText());
        CommentEntity savedEntity = commentRepository.save(commentEntity);
        return commentMapper.entityToDto(savedEntity);
    }

    /**
     * Удалить комментарий с проверкой прав через @PreAuthorize
     */
    @PreAuthorize("hasRole('ADMIN') or @commentService.isCommentOwner(#commentId, authentication.name)")
    public void deleteComment(Integer adId, Integer commentId) {
        CommentEntity commentEntity = commentRepository.findByIdAndAdId(commentId, adId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(commentEntity);
    }

}
