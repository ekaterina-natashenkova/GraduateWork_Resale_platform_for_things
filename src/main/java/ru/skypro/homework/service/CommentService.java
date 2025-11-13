package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final CommentMapper commentMapper;

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

    public Optional<Comment> createComment(Integer adId, Integer authorId, CreateOrUpdateComment createComment) {
        Optional<UserEntity> authorOpt = userRepository.findById(authorId);
        Optional<AdEntity> adOpt = adRepository.findById(adId);

        if (authorOpt.isEmpty() || adOpt.isEmpty()) {
            return Optional.empty();
        }

        CommentEntity commentEntity = commentMapper.createOrUpdateCommentToEntity(createComment);
        commentEntity.setAuthor(authorOpt.get());
        commentEntity.setAd(adOpt.get());
        commentEntity.setCreatedAt(LocalDateTime.now());

        CommentEntity savedEntity = commentRepository.save(commentEntity);
        return Optional.of(commentMapper.entityToDto(savedEntity));
    }

    public Optional<Comment> updateComment(Integer commentId, Integer adId, CreateOrUpdateComment updateComment) {
        return commentRepository.findByIdAndAdId(commentId, adId)
                .map(commentEntity -> {
                    commentEntity.setText(updateComment.getText());
                    CommentEntity savedEntity = commentRepository.save(commentEntity);
                    return commentMapper.entityToDto(savedEntity);
                });
    }

    public boolean deleteComment(Integer commentId, Integer adId) {
        Optional<CommentEntity> commentOpt = commentRepository.findByIdAndAdId(commentId, adId);
        if (commentOpt.isPresent()) {
            commentRepository.delete(commentOpt.get());
            return true;
        }
        return false;
    }

    public boolean isCommentAuthor(Integer commentId, Integer authorId) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getAuthor().getId().equals(authorId))
                .orElse(false);
    }

}
