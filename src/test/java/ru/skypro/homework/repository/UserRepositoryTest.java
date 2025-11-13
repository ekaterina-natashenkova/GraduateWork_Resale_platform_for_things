package ru.skypro.homework.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

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
        return user;
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {

        UserEntity user = createValidUser("user1", "test@example.com", "+79991234567");
        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> result = userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("user1", result.get().getUsername());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {

        Optional<UserEntity> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {

        UserEntity user = createValidUser("user2", "existing@example.com", "+79991234568");
        userRepository.save(user);

        boolean result = userRepository.existsByEmail("existing@example.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {

        boolean result = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }

    @Test
    void findByIdAndEmail_WithValidIdAndEmail_ShouldReturnUser() {

        UserEntity user = createValidUser("user3", "user@example.com", "+79991234569");
        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> result = userRepository.findByIdAndEmail(
                savedUser.getId(), "user@example.com");

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void findByIdAndEmail_WithInvalidId_ShouldReturnEmpty() {

        UserEntity user = createValidUser("user4", "user4@example.com", "+79991234570");
        userRepository.save(user);

        Optional<UserEntity> result = userRepository.findByIdAndEmail(999, "user4@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdAndEmail_WithInvalidEmail_ShouldReturnEmpty() {

        UserEntity user = createValidUser("user5", "user5@example.com", "+79991234571");
        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> result = userRepository.findByIdAndEmail(
                savedUser.getId(), "wrong@example.com");

        assertFalse(result.isPresent());
    }

}