package ru.skypro.homework.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.model.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AdRepositoryTest {

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

    @Test
    void findByAuthorId_WithExistingAuthor_ShouldReturnAds() {

        UserEntity author = createValidUser("author1", "author1@example.com", "+79991234572");
        createValidAd(author, "Ad 1");
        createValidAd(author, "Ad 2");

        List<AdEntity> result = adRepository.findByAuthorId(author.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ad -> ad.getAuthor().getId().equals(author.getId())));
    }

    @Test
    void findByAuthorId_WithNonExistingAuthor_ShouldReturnEmptyList() {

        List<AdEntity> result = adRepository.findByAuthorId(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByAuthorIdAndAdId_WithValidAuthorAndAd_ShouldReturnAd() {

        UserEntity author = createValidUser("author2", "author2@example.com", "+79991234573");
        AdEntity ad = createValidAd(author, "Specific Ad");

        Optional<AdEntity> result = adRepository.findByAuthorIdAndAdId(author.getId(), ad.getId());

        assertTrue(result.isPresent());
        assertEquals(ad.getId(), result.get().getId());
        assertEquals(author.getId(), result.get().getAuthor().getId());
    }

    @Test
    void findByAuthorIdAndAdId_WithInvalidAuthor_ShouldReturnEmpty() {

        UserEntity author = createValidUser("author3", "author3@example.com", "+79991234574");
        AdEntity ad = createValidAd(author, "Test Ad");

        Optional<AdEntity> result = adRepository.findByAuthorIdAndAdId(999, ad.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void findByTitleContainingIgnoreCase_WithMatchingTitle_ShouldReturnAds() {

        UserEntity author = createValidUser("author4", "author4@example.com", "+79991234575");
        createValidAd(author, "iPhone for sale");
        createValidAd(author, "IPHONE Case");
        createValidAd(author, "Samsung phone");

        List<AdEntity> result = adRepository.findByTitleContainingIgnoreCase("iphone");

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(ad -> ad.getTitle().toLowerCase().contains("iphone")));
    }

    @Test
    void findByTitleContainingIgnoreCase_WithNonMatchingTitle_ShouldReturnEmpty() {

        UserEntity author = createValidUser("author5", "author5@example.com", "+79991234576");
        createValidAd(author, "Test Ad");

        List<AdEntity> result = adRepository.findByTitleContainingIgnoreCase("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdWithAuthor_WithExistingId_ShouldReturnAdWithAuthor() {

        UserEntity author = createValidUser("author6", "author6@example.com", "+79991234577");
        AdEntity ad = createValidAd(author, "Ad with Author");

        Optional<AdEntity> result = adRepository.findByIdWithAuthor(ad.getId());

        assertTrue(result.isPresent());
        assertEquals(ad.getId(), result.get().getId());
        assertNotNull(result.get().getAuthor());
        assertEquals(author.getId(), result.get().getAuthor().getId());
        assertEquals(author.getEmail(), result.get().getAuthor().getEmail());
    }

    @Test
    void findByIdWithAuthor_WithNonExistingId_ShouldReturnEmpty() {

        Optional<AdEntity> result = adRepository.findByIdWithAuthor(999);

        assertFalse(result.isPresent());
    }

}