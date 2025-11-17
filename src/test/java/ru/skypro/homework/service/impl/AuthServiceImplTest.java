package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.model.enums.Role;
import ru.skypro.homework.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Register validRegister;
    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        // Setup valid registration data
        validRegister = new Register();
        validRegister.setUsername("test@example.com");
        validRegister.setPassword("password123");
        validRegister.setFirstName("John");
        validRegister.setLastName("Doe");
        validRegister.setPhone("+79991234567");
        validRegister.setRole(Role.USER);

        // Setup existing user
        existingUser = new UserEntity();
        existingUser.setId(1);
        existingUser.setEmail("test@example.com");
        existingUser.setUsername("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setPhone("+79991234567");
        existingUser.setRole(Role.USER);
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // ========== LOGIN TESTS ==========

    @Test
    void login_WithValidCredentials_ShouldReturnTrue() {
        // Given
        String username = "test@example.com";
        String password = "correctPassword";

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(true);

        // When
        boolean result = authService.login(username, password);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(username);
        verify(passwordEncoder).matches(password, existingUser.getPassword());
    }

    @Test
    void login_WithValidCredentialsAndWhitespace_ShouldTrimAndReturnTrue() {
        // Given
        String username = "  test@example.com  ";
        String password = "correctPassword";
        String trimmedUsername = "test@example.com";

        when(userRepository.findByEmail(trimmedUsername)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(true);

        // When
        boolean result = authService.login(username, password);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(trimmedUsername);
        verify(passwordEncoder).matches(password, existingUser.getPassword());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnFalse() {
        // Given
        String username = "test@example.com";
        String password = "wrongPassword";

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(false);

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(username);
        verify(passwordEncoder).matches(password, existingUser.getPassword());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnFalse() {
        // Given
        String username = "nonexistent@example.com";
        String password = "anyPassword";

        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithNullUsername_ShouldReturnFalse() {
        // Given
        String username = null;
        String password = "password";

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithEmptyUsername_ShouldReturnFalse() {
        // Given
        String username = "";
        String password = "password";

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithBlankUsername_ShouldReturnFalse() {
        // Given
        String username = "   ";
        String password = "password";

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithNullPassword_ShouldReturnFalse() {
        // Given
        String username = "test@example.com";
        String password = null;

        // When
        boolean result = authService.login(username, password);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ========== REGISTER TESTS ==========

    @Test
    void register_WithNewUser_ShouldReturnTrueAndSaveUser() {
        // Given
        when(userRepository.existsByEmail(validRegister.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(validRegister.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        // When
        boolean result = authService.register(validRegister);

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail(validRegister.getUsername());
        verify(passwordEncoder).encode(validRegister.getPassword());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_WithNewUserAndWhitespace_ShouldTrimAndSaveUser() {
        // Given
        Register registerWithWhitespace = new Register();
        registerWithWhitespace.setUsername("  test@example.com  ");
        registerWithWhitespace.setPassword("  password123  ");
        registerWithWhitespace.setFirstName("  John  ");
        registerWithWhitespace.setLastName("  Doe  ");
        registerWithWhitespace.setPhone("  +79991234567  ");
        registerWithWhitespace.setRole(Role.USER);

        String trimmedUsername = "test@example.com";

        when(userRepository.existsByEmail(trimmedUsername)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals(trimmedUsername, savedUser.getUsername());
            assertEquals(trimmedUsername, savedUser.getEmail());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertEquals("John", savedUser.getFirstName());
            assertEquals("Doe", savedUser.getLastName());
            assertEquals("+79991234567", savedUser.getPhone());
            return savedUser;
        });

        // When
        boolean result = authService.register(registerWithWhitespace);

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail(trimmedUsername);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_WithExistingUser_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail(validRegister.getUsername())).thenReturn(true);

        // When
        boolean result = authService.register(validRegister);

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail(validRegister.getUsername());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_WithNullRegister_ShouldReturnFalse() {
        // When
        boolean result = authService.register(null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_WithNullUsername_ShouldReturnFalse() {
        // Given
        Register registerWithNullUsername = new Register();
        registerWithNullUsername.setUsername(null);
        registerWithNullUsername.setPassword("password");
        registerWithNullUsername.setFirstName("John");
        registerWithNullUsername.setLastName("Doe");
        registerWithNullUsername.setPhone("+79991234567");
        registerWithNullUsername.setRole(Role.USER);

        // When
        boolean result = authService.register(registerWithNullUsername);

        // Then
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_WithEmptyUsername_ShouldReturnFalse() {
        // Given
        Register registerWithEmptyUsername = new Register();
        registerWithEmptyUsername.setUsername("");
        registerWithEmptyUsername.setPassword("password");
        registerWithEmptyUsername.setFirstName("John");
        registerWithEmptyUsername.setLastName("Doe");
        registerWithEmptyUsername.setPhone("+79991234567");
        registerWithEmptyUsername.setRole(Role.USER);

        // When
        boolean result = authService.register(registerWithEmptyUsername);

        // Then
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_WithBlankUsername_ShouldReturnFalse() {
        // Given
        Register registerWithBlankUsername = new Register();
        registerWithBlankUsername.setUsername("   ");
        registerWithBlankUsername.setPassword("password");
        registerWithBlankUsername.setFirstName("John");
        registerWithBlankUsername.setLastName("Doe");
        registerWithBlankUsername.setPhone("+79991234567");
        registerWithBlankUsername.setRole(Role.USER);

        // When
        boolean result = authService.register(registerWithBlankUsername);

        // Then
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_WithAnyNullField_ShouldReturnFalse() {
        // Test various invalid register objects
        assertAll(
                () -> assertFalse(authService.register(createRegisterWithNullField("password"))),
                () -> assertFalse(authService.register(createRegisterWithNullField("firstName"))),
                () -> assertFalse(authService.register(createRegisterWithNullField("lastName"))),
                () -> assertFalse(authService.register(createRegisterWithNullField("phone"))),
                () -> assertFalse(authService.register(createRegisterWithNullField("role")))
        );
    }

    private Register createRegisterWithNullField(String nullField) {
        Register register = new Register();
        register.setUsername("test@example.com");
        register.setPassword("password");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+79991234567");
        register.setRole(Role.USER);

        switch (nullField) {
            case "password": register.setPassword(null); break;
            case "firstName": register.setFirstName(null); break;
            case "lastName": register.setLastName(null); break;
            case "phone": register.setPhone(null); break;
            case "role": register.setRole(null); break;
        }

        return register;
    }

    @Test
    void register_WithAdminRole_ShouldSaveWithAdminRole() {
        // Given
        Register adminRegister = new Register();
        adminRegister.setUsername("admin@example.com");
        adminRegister.setPassword("adminPassword");
        adminRegister.setFirstName("Admin");
        adminRegister.setLastName("User");
        adminRegister.setPhone("+79998887766");
        adminRegister.setRole(Role.ADMIN);

        when(userRepository.existsByEmail(adminRegister.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(adminRegister.getPassword())).thenReturn("encodedAdminPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals(Role.ADMIN, savedUser.getRole());
            return savedUser;
        });

        // When
        boolean result = authService.register(adminRegister);

        // Then
        assertTrue(result);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_WhenRepositoryThrowsException_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail(validRegister.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(validRegister.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = authService.register(validRegister);

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail(validRegister.getUsername());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_ShouldSetCorrectUserFieldsWithTrimmedValues() {
        // Given
        when(userRepository.existsByEmail(validRegister.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(validRegister.getPassword())).thenReturn("encodedPassword");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity userToSave = invocation.getArgument(0);

            // Verify all fields are set correctly with trimmed values
            assertEquals(validRegister.getUsername(), userToSave.getUsername());
            assertEquals(validRegister.getUsername(), userToSave.getEmail());
            assertEquals("encodedPassword", userToSave.getPassword());
            assertEquals(validRegister.getFirstName(), userToSave.getFirstName());
            assertEquals(validRegister.getLastName(), userToSave.getLastName());
            assertEquals(validRegister.getPhone(), userToSave.getPhone());
            assertEquals(validRegister.getRole(), userToSave.getRole());
            assertNotNull(userToSave.getCreatedAt());
            assertNotNull(userToSave.getUpdatedAt());

            return userToSave;
        });

        // When
        boolean result = authService.register(validRegister);

        // Then
        assertTrue(result);
        verify(userRepository).save(any(UserEntity.class));
    }

    // ========== PRIVATE METHOD TESTS ==========

    @Test
    void createUserEntityFromRegister_ShouldMapAllFieldsCorrectlyWithTrim() {
        // Given
        Register testRegister = new Register();
        testRegister.setUsername("  mapping@test.com  ");
        testRegister.setPassword("  mappingPassword  ");
        testRegister.setFirstName("  Mapping  ");
        testRegister.setLastName("  Test  ");
        testRegister.setPhone("  +79991112233  ");
        testRegister.setRole(Role.USER);

        when(passwordEncoder.encode("mappingPassword")).thenReturn("encodedMappingPassword");

        // Use reflection to test private method
        UserEntity result = (UserEntity) org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                authService, "createUserEntityFromRegister", testRegister
        );

        // Then
        assertNotNull(result);
        assertEquals("mapping@test.com", result.getUsername());
        assertEquals("mapping@test.com", result.getEmail());
        assertEquals("encodedMappingPassword", result.getPassword());
        assertEquals("Mapping", result.getFirstName());
        assertEquals("Test", result.getLastName());
        assertEquals("+79991112233", result.getPhone());
        assertEquals(Role.USER, result.getRole());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(passwordEncoder).encode("mappingPassword");
    }

}