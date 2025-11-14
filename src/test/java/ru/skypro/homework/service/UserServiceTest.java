package ru.skypro.homework.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithExistingUser_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        User expectedUser = new User();

        setupSecurityContextDirectly(email, "ROLE_USER");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDto(userEntity)).thenReturn(expectedUser);

        // When
        User result = userService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepository).findByEmail(email);
        verify(userMapper).entityToDto(userEntity);
    }

    @Test
    void getCurrentUser_WithNonExistingUser_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        setupSecurityContextDirectly(email, "ROLE_USER");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getCurrentUser());
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUserEntity_WithExistingUser_ShouldReturnUserEntity() {
        // Given
        String email = "test@example.com";
        UserEntity expectedEntity = new UserEntity();
        expectedEntity.setEmail(email);

        setupSecurityContextDirectly(email, "ROLE_USER");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedEntity));

        // When
        UserEntity result = userService.getCurrentUserEntity();

        // Then
        assertNotNull(result);
        assertEquals(expectedEntity, result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUserEntity_WithNonExistingUser_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        setupSecurityContextDirectly(email, "ROLE_USER");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getCurrentUserEntity());
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUserEmail_ShouldReturnEmailFromSecurityContext() {
        // Given
        String expectedEmail = "user@example.com";
        setupSecurityContextDirectly(expectedEmail, "ROLE_USER");

        // When
        String result = userService.getCurrentUserEmail();

        // Then
        assertEquals(expectedEmail, result);
    }

    @Test
    void isCurrentUserAdmin_WithAdminRole_ShouldReturnTrue() {
        // Given
        setupSecurityContextDirectly("admin@example.com", "ROLE_ADMIN");

        // When
        boolean result = userService.isCurrentUserAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    void isCurrentUserAdmin_WithUserRole_ShouldReturnFalse() {
        // Given
        setupSecurityContextDirectly("user@example.com", "ROLE_USER");

        // When
        boolean result = userService.isCurrentUserAdmin();

        // Then
        assertFalse(result);
    }

    // Остальные тесты остаются без изменений...

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Given
        Integer userId = 1;
        UserEntity userEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDto(userEntity)).thenReturn(expectedUser);

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findById(userId);
        verify(userMapper).entityToDto(userEntity);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
        verify(userMapper, never()).entityToDto(any());
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDto(userEntity)).thenReturn(expectedUser);

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByEmail(email);
        verify(userMapper).entityToDto(userEntity);
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).entityToDto(any());
    }

    @Test
    void updateUser_WithExistingId_ShouldReturnUpdatedUser() {
        // Given
        Integer userId = 1;
        User userDto = new User();
        userDto.setEmail("updated@example.com");

        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(userId);
        existingEntity.setEmail("old@example.com");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(userId);
        savedEntity.setEmail("updated@example.com");

        User expectedUser = new User();
        expectedUser.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(savedEntity);
        when(userMapper.entityToDto(savedEntity)).thenReturn(expectedUser);

        // When
        Optional<User> result = userService.updateUser(userId, userDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findById(userId);
        verify(userMapper).updateEntityFromDto(userDto, existingEntity);
        verify(userRepository).save(existingEntity);
        verify(userMapper).entityToDto(savedEntity);
    }

    @Test
    void updateUser_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Integer userId = 999;
        User userDto = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateUser(userId, userDto);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
        verify(userMapper, never()).updateEntityFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void existsByEmail_ShouldReturnRepositoryResult() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void createUser_WithNewEmail_ShouldReturnCreatedUser() {
        // Given
        User userDto = new User();
        userDto.setEmail("new@example.com");

        UserEntity userEntity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userMapper.dtoToEntity(userDto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.entityToDto(savedEntity)).thenReturn(expectedUser);

        // When
        User result = userService.createUser(userDto);

        // Then
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userMapper).dtoToEntity(userDto);
        verify(userRepository).save(userEntity);
        verify(userMapper).entityToDto(savedEntity);
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Given
        User userDto = new User();
        userDto.setEmail("existing@example.com");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(userDto));
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userMapper, never()).dtoToEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserEntityById_WithExistingId_ShouldReturnUserEntity() {
        // Given
        Integer userId = 1;
        UserEntity expectedEntity = new UserEntity();
        expectedEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedEntity));

        // When
        Optional<UserEntity> result = userService.getUserEntityById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<UserEntity> result = userService.getUserEntityById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityByEmail_WithExistingEmail_ShouldReturnUserEntity() {
        // Given
        String email = "test@example.com";
        UserEntity expectedEntity = new UserEntity();
        expectedEntity.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedEntity));

        // When
        Optional<UserEntity> result = userService.getUserEntityByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserEntityByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<UserEntity> result = userService.getUserEntityByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void updateUserImage_WithExistingUser_ShouldUpdateImage() {
        // Given
        String email = "test@example.com";
        String imageUrl = "/images/users/avatar.jpg";
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        // When
        userService.updateUserImage(email, imageUrl);

        // Then
        assertEquals(imageUrl, userEntity.getImagePath());
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(userEntity);
    }

    @Test
    void updateUserImage_WithNonExistingUser_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        String imageUrl = "/images/users/avatar.jpg";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUserImage(email, imageUrl));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    /**
     * Вспомогательный метод для прямой настройки SecurityContext
     */
    private void setupSecurityContextDirectly(String username, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

}