package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final UserService userService;

    @Value("${app.images.path:uploads/images}")
    private String imagesPath;

    /**
     * Сохранить аватар пользователя
     */
    public String saveUserImage(MultipartFile image, String userEmail) throws IOException {
        String filename = generateFilename(userEmail, image.getOriginalFilename());
        Path filePath = Paths.get(imagesPath, "users", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        String imageUrl = "/images/users/" + filename;

        // Обновляем путь к изображению в базе данных
        userService.updateUserImage(userEmail, imageUrl);

        return imageUrl;
    }

    /**
     * Сохранить изображение объявления
     */
    public String saveAdImage(MultipartFile image, Integer adId) throws IOException {
        String filename = generateFilename("ad_" + adId, image.getOriginalFilename());
        Path filePath = Paths.get(imagesPath, "ads", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        return "/images/ads/" + filename;
    }

    private String generateFilename(String prefix, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return prefix + "_" + UUID.randomUUID() + extension;
    }

    /**
     * Получить изображение по пути
     */
    public byte[] getImage(String imagePath) throws IOException {
        Path filePath = Paths.get(imagesPath, imagePath.replaceFirst("^/images/", ""));
        return Files.readAllBytes(filePath);
    }

}
