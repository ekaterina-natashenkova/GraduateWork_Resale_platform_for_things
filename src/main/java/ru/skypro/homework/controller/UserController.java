package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.dto.NewPassword;
import ru.skypro.homework.model.dto.UpdateUser;
import ru.skypro.homework.model.dto.User;
import ru.skypro.homework.security.CustomUserDetailsManager;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;
    private final CustomUserDetailsManager userDetailsManager;


    @Operation(
            summary = "Обновление пароля",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PostMapping("/set_password")
    public ResponseEntity<Void> setPassword(@RequestBody NewPassword newPassword,
                                            Authentication authentication) {
        log.info("Called setPassword for user: {}", authentication.getName());

        try {
            userDetailsManager.changePassword(
                    authentication.getName(),
                    newPassword.getCurrentPassword(),
                    newPassword.getNewPassword()
            );
            log.info("Password successfully changed for user: {}", authentication.getName());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("Password change failed for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error changing password for user {}", authentication.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Получение информации об авторизованном пользователе",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<User> getUser(Authentication authentication) {
        log.info("Called getUser for user: {}", authentication.getName());
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @Operation(
            summary = "Обновление информации об авторизованном пользователе",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PatchMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody UpdateUser updateUser,
                                           Authentication authentication) {
        log.info("Called updateUser for user: {}", authentication.getName());

        User currentUser = userService.getCurrentUser();

        // Создаем User DTO из UpdateUser
        User userDto = new User();
        userDto.setFirstName(updateUser.getFirstName());
        userDto.setLastName(updateUser.getLastName());
        userDto.setPhone(updateUser.getPhone());

        User updatedUser = userService.updateUser(currentUser.getId(), userDto)
                .orElseThrow(() -> new RuntimeException("Failed to update user"));

        log.info("User {} successfully updated", authentication.getName());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Обновление аватара авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateUserImage(@RequestParam("image") MultipartFile image,
                                                  Authentication authentication) {
        log.info("Called updateUserImage for user: {}", authentication.getName());

        try {
            String imageUrl = imageService.saveUserImage(image, authentication.getName());
            log.info("User image successfully updated for user: {}", authentication.getName());
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            log.error("Failed to save user image for user: {}", authentication.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
