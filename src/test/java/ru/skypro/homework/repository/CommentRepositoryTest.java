package ru.skypro.homework.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.CommentEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.model.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity createValidUser(String username, String email, String phone) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedPassword123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private AdEntity createValidAd(UserEntity author, String title) {
        AdEntity ad = new AdEntity();
        ad.setTitle(title);
        ad.setDescription("Test description for " + title);
        ad.setPrice(1000);
        ad.setImagePath("/images/test.jpg");
        ad.setAuthor(author);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());
        return adRepository.save(ad);
    }

    private CommentEntity createValidComment(UserEntity author, AdEntity ad, String text) {
        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setAd(ad);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Test
    void findByAdId_WithExistingAd_ShouldReturnComments() {

        UserEntity author = createValidUser("author1", "author1@example.com", "+79991234578");
        UserEntity commenter = createValidUser("commenter1", "commenter1@example.com", "+79991234579");
        AdEntity ad = createValidAd(author, "Test Ad");
        createValidComment(commenter, ad, "First comment");
        createValidComment(commenter, ad, "Second comment");

        List<CommentEntity> result = commentRepository.findByAdId(ad.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(comment -> comment.getAd().getId().equals(ad.getId())));
    }

    @Test
    void findByAdId_WithNonExistingAd_ShouldReturnEmptyList() {

        List<CommentEntity> result = commentRepository.findByAdId(999);


        assertTrue(result.isEmpty());
    }

    @Test
    void findByAdIdWithAuthor_WithExistingAd_ShouldReturnCommentsWithAuthors() {

        UserEntity author = createValidUser("author2", "author2@example.com", "+79991234580");
        UserEntity commenter1 = createValidUser("commenter2", "commenter2@example.com", "+79991234581");
        UserEntity commenter2 = createValidUser("commenter3", "commenter3@example.com", "+79991234582");
        AdEntity ad = createValidAd(author, "Test Ad");
        createValidComment(commenter1, ad, "Comment 1");
        createValidComment(commenter2, ad, "Comment 2");

        List<CommentEntity> result = commentRepository.findByAdIdWithAuthor(ad.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(comment ->
                comment.getAd().getId().equals(ad.getId()) &&
                        comment.getAuthor() != null
        ));
    }

    @Test
    void findByAdIdAndAuthorIdAndCommentId_WithValidData_ShouldReturnComment() {

        UserEntity author = createValidUser("author3", "author3@example.com", "+79991234583");
        UserEntity commenter = createValidUser("commenter4", "commenter4@example.com", "+79991234584");
        AdEntity ad = createValidAd(author, "Test Ad");
        CommentEntity comment = createValidComment(commenter, ad, "Specific comment");

        Optional<CommentEntity> result = commentRepository.findByAdIdAndAuthorIdAndCommentId(
                ad.getId(), commenter.getId(), comment.getId());

        assertTrue(result.isPresent());
        assertEquals(comment.getId(), result.get().getId());
        assertEquals(ad.getId(), result.get().getAd().getId());
        assertEquals(commenter.getId(), result.get().getAuthor().getId());
    }

    @Test
    void findByAdIdAndAuthorIdAndCommentId_WithInvalidData_ShouldReturnEmpty() {

        UserEntity author = createValidUser("author4", "author4@example.com", "+79991234585");
        UserEntity commenter = createValidUser("commenter5", "commenter5@example.com", "+79991234586");
        AdEntity ad = createValidAd(author, "Test Ad");
        createValidComment(commenter, ad, "Comment");

        Optional<CommentEntity> result = commentRepository.findByAdIdAndAuthorIdAndCommentId(
                999, commenter.getId(), 1);

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdAndAdId_WithValidIdAndAdId_ShouldReturnComment() {

        UserEntity author = createValidUser("author5", "author5@example.com", "+79991234587");
        UserEntity commenter = createValidUser("commenter6", "commenter6@example.com", "+79991234588");
        AdEntity ad = createValidAd(author, "Test Ad");
        CommentEntity comment = createValidComment(commenter, ad, "Test comment");

        Optional<CommentEntity> result = commentRepository.findByIdAndAdId(comment.getId(), ad.getId());

        assertTrue(result.isPresent());
        assertEquals(comment.getId(), result.get().getId());
        assertEquals(ad.getId(), result.get().getAd().getId());
    }

    @Test
    void findByIdAndAdId_WithInvalidCommentId_ShouldReturnEmpty() {

        UserEntity author = createValidUser("author6", "author6@example.com", "+79991234589");
        AdEntity ad = createValidAd(author, "Test Ad");

        Optional<CommentEntity> result = commentRepository.findByIdAndAdId(999, ad.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void deleteByAdId_WithExistingAd_ShouldDeleteComments() {

        UserEntity author = createValidUser("author7", "author7@example.com", "+79991234590");
        UserEntity commenter = createValidUser("commenter7", "commenter7@example.com", "+79991234591");
        AdEntity ad = createValidAd(author, "Test Ad");
        createValidComment(commenter, ad, "Comment to delete");
        createValidComment(commenter, ad, "Another comment");

        assertEquals(2, commentRepository.findByAdId(ad.getId()).size());

        commentRepository.deleteByAdId(ad.getId());

        assertEquals(0, commentRepository.findByAdId(ad.getId()).size());
    }

    @Test
    void deleteByAdId_WithNonExistingAd_ShouldDoNothing() {

        assertDoesNotThrow(() -> commentRepository.deleteByAdId(999));
    }

}