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
        log.info("Login attempt for user: {}", userName);

        return userRepository.findByEmail(userName)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean register(Register register) {
        log.info("Registration attempt for user: {}", register.getUsername());

        if (userRepository.existsByEmail(register.getUsername())) {
            log.warn("Registration failed: user {} already exists", register.getUsername());
            return false;
        }

        try {
            // Создаем пользователя в нашей основной таблице
            UserEntity userEntity = createUserEntityFromRegister(register);
            userRepository.save(userEntity);

            log.info("User {} successfully registered in database", register.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Registration error for user {}", register.getUsername(), e);
            return false;
        }
    }
    /**
     * Создает UserEntity из Register DTO
     */
    private UserEntity createUserEntityFromRegister(Register register) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(register.getUsername());
        userEntity.setEmail(register.getUsername());
        userEntity.setPassword(passwordEncoder.encode(register.getPassword()));
        userEntity.setFirstName(register.getFirstName());
        userEntity.setLastName(register.getLastName());
        userEntity.setPhone(register.getPhone());
        userEntity.setRole(register.getRole());
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        return userEntity;
    }

}