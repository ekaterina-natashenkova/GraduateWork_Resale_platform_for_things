package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean login(String userName, String password) {
        // Валидация входных данных
        if (userName == null || userName.trim().isEmpty() || password == null) {
            log.warn("Login failed: username or password is null or empty");
            return false;
        }

        String trimmedUsername = userName.trim();
        log.info("Login attempt for user: {}", trimmedUsername);

        return userRepository.findByEmail(trimmedUsername)
                .map(user -> {
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        log.warn("Login failed: incorrect password for user {}", trimmedUsername);
                    } else {
                        log.info("Login successful for user: {}", trimmedUsername);
                    }
                    return matches;
                })
                .orElseGet(() -> {
                    log.warn("Login failed: user {} not found", trimmedUsername);
                    return false;
                });
    }

    @Override
    @Transactional
    public boolean register(Register register) {
        // Валидация входных данных
        if (register == null) {
            log.warn("Registration failed: register object is null");
            return false;
        }

        String username = register.getUsername();
        if (username == null || username.trim().isEmpty()) {
            log.warn("Registration failed: username is null or empty");
            return false;
        }

        String trimmedUsername = username.trim();
        log.info("Registration attempt for user: {}", trimmedUsername);

        // Проверка существования пользователя
        if (userRepository.existsByEmail(trimmedUsername)) {
            log.warn("Registration failed: user {} already exists", trimmedUsername);
            return false;
        }

        // Валидация обязательных полей
        if (!isValidRegister(register)) {
            return false;
        }

        try {
            // Создаем пользователя в нашей основной таблице
            UserEntity userEntity = createUserEntityFromRegister(register);
            userRepository.save(userEntity);

            log.info("User {} successfully registered in database", trimmedUsername);
            return true;

        } catch (Exception e) {
            log.error("Registration error for user {}", trimmedUsername, e);
            return false;
        }
    }

    /**
     * Валидирует все обязательные поля Register
     */
    private boolean isValidRegister(Register register) {
        if (register.getPassword() == null || register.getPassword().trim().isEmpty()) {
            log.warn("Registration failed: password is null or empty for user {}", register.getUsername());
            return false;
        }

        if (register.getFirstName() == null || register.getFirstName().trim().isEmpty()) {
            log.warn("Registration failed: first name is null or empty for user {}", register.getUsername());
            return false;
        }

        if (register.getLastName() == null || register.getLastName().trim().isEmpty()) {
            log.warn("Registration failed: last name is null or empty for user {}", register.getUsername());
            return false;
        }

        if (register.getPhone() == null || register.getPhone().trim().isEmpty()) {
            log.warn("Registration failed: phone is null or empty for user {}", register.getUsername());
            return false;
        }

        if (register.getRole() == null) {
            log.warn("Registration failed: role is null for user {}", register.getUsername());
            return false;
        }

        return true;
    }

    /**
     * Создает UserEntity из Register DTO
     */
    private UserEntity createUserEntityFromRegister(Register register) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(register.getUsername().trim());
        userEntity.setEmail(register.getUsername().trim());
        userEntity.setPassword(passwordEncoder.encode(register.getPassword().trim()));
        userEntity.setFirstName(register.getFirstName().trim());
        userEntity.setLastName(register.getLastName().trim());
        userEntity.setPhone(register.getPhone().trim());
        userEntity.setRole(register.getRole());
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        return userEntity;
    }

}