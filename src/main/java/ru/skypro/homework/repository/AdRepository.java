package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.entity.AdEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<AdEntity, Integer> {

    List<AdEntity> findByAuthorId(Integer authorId);

    @Query("SELECT a FROM AdEntity a WHERE a.author.id = :authorId AND a.id = :adId")
    Optional<AdEntity> findByAuthorIdAndAdId(@Param("authorId") Integer authorId,
                                             @Param("adId") Integer adId);

    List<AdEntity> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT a FROM AdEntity a LEFT JOIN FETCH a.author WHERE a.id = :id")
    Optional<AdEntity> findByIdWithAuthor(@Param("id") Integer id);

}
