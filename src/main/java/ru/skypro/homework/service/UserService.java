package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Получить текущего аутентифицированного пользователя
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получить Entity текущего пользователя
     */
    public UserEntity getCurrentUserEntity() {
        String email = getCurrentUserEmail();
        return getUserEntityByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получить email текущего пользователя из SecurityContext
     */
    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Проверить, является ли текущий пользователь администратором
     */
    public boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(userMapper::entityToDto);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::entityToDto);
    }

    public Optional<User> updateUser(Integer id, User userDto) {
        return userRepository.findById(id)
                .map(userEntity -> {
                    userMapper.updateEntityFromDto(userDto, userEntity);
                    UserEntity savedEntity = userRepository.save(userEntity);
                    return userMapper.entityToDto(savedEntity);
                });
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Создать пользователя (без установки пароля - этим занимается Spring Security при регистрации)
     */
    public User createUser(User userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        UserEntity userEntity = userMapper.dtoToEntity(userDto);

        UserEntity savedEntity = userRepository.save(userEntity);
        return userMapper.entityToDto(savedEntity);
    }

    public Optional<UserEntity> getUserEntityById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Обновить аватар пользователя
     */
    public void updateUserImage(String email, String imageUrl) {
        UserEntity userEntity = getUserEntityByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userEntity.setImagePath(imageUrl);
        userRepository.save(userEntity);
    }

}
