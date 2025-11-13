package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.entity.CommentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findByAdId(Integer adId);

    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.author WHERE c.ad.id = :adId ORDER BY c.createdAt DESC")
    List<CommentEntity> findByAdIdWithAuthor(@Param("adId") Integer adId);

    @Query("SELECT c FROM CommentEntity c WHERE c.ad.id = :adId AND c.author.id = :authorId AND c.id = :commentId")
    Optional<CommentEntity> findByAdIdAndAuthorIdAndCommentId(@Param("adId") Integer adId,
                                                              @Param("authorId") Integer authorId,
                                                              @Param("commentId") Integer commentId);

    void deleteByAdId(Integer adId);

    Optional<CommentEntity> findByIdAndAdId(Integer id, Integer adId);

}
