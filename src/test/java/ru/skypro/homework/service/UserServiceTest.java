package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

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

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {

        Integer userId = 1;
        UserEntity userEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDto(userEntity)).thenReturn(expectedUser);

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnEmpty() {

        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() {

        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDto(userEntity)).thenReturn(expectedUser);

        Optional<User> result = userService.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldReturnEmpty() {

        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail(email);

        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void updateUser_WithExistingId_ShouldReturnUpdatedUser() {

        Integer userId = 1;
        User userDto = new User();
        UserEntity existingEntity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        User expectedUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(savedEntity);
        when(userMapper.entityToDto(savedEntity)).thenReturn(expectedUser);

        Optional<User> result = userService.updateUser(userId, userDto);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userMapper).updateEntityFromDto(userDto, existingEntity);
        verify(userRepository).save(existingEntity);
    }

    @Test
    void updateUser_WithNonExistingId_ShouldReturnEmpty() {

        Integer userId = 999;
        User userDto = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(userId, userDto);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void existsByEmail_ShouldReturnRepositoryResult() {

        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {

        User userDto = new User();
        String encodedPassword = "encodedPassword";
        UserEntity userEntity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        User expectedUser = new User();

        when(userMapper.dtoToEntity(userDto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.entityToDto(savedEntity)).thenReturn(expectedUser);

        User result = userService.createUser(userDto, encodedPassword);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        assertEquals(encodedPassword, userEntity.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    void getUserEntityById_WithExistingId_ShouldReturnUserEntity() {

        Integer userId = 1;
        UserEntity expectedEntity = new UserEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedEntity));

        Optional<UserEntity> result = userService.getUserEntityById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityById_WithNonExistingId_ShouldReturnEmpty() {

        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserEntityById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

}