package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

    public User createUser(User userDto, String encodedPassword) {
        UserEntity userEntity = userMapper.dtoToEntity(userDto);
        userEntity.setPassword(encodedPassword);
        UserEntity savedEntity = userRepository.save(userEntity);
        return userMapper.entityToDto(savedEntity);
    }

    public Optional<UserEntity> getUserEntityById(Integer id) {
        return userRepository.findById(id);
    }

}
