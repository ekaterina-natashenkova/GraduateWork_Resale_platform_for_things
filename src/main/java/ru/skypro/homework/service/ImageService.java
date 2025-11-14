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
        String filename = generateFilename("user_" + userEmail, getFileExtension(image));
        Path filePath = Paths.get(imagesPath, "users", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        String imageUrl = "/images/users/" + filename;

        // Обновляем путь к изображению в базе данных
        userService.updateUserImage(userEmail, imageUrl);

        log.info("User image saved: {}", imageUrl);
        return imageUrl;
    }

    /**
     * Сохранить изображение объявления
     */
    public String saveAdImage(MultipartFile image, Integer adId) throws IOException {
        String prefix = adId != null ? "ad_" + adId : "ad_temp";
        String filename = generateFilename(prefix, getFileExtension(image));
        Path filePath = Paths.get(imagesPath, "ads", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        String imageUrl = "/images/ads/" + filename;
        log.info("Ad image saved: {}", imageUrl);
        return imageUrl;
    }

    /**
     * Получить расширение файла
     */
    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg"; // default extension
    }

    private String generateFilename(String prefix, String extension) {
        return prefix + "_" + UUID.randomUUID() + extension;
    }

    /**
     * Получить изображение по пути
     */
    public byte[] getImage(String imagePath) throws IOException {
        String relativePath = imagePath.replaceFirst("^/images/", "");
        Path filePath = Paths.get(imagesPath, relativePath);

        if (!Files.exists(filePath)) {
            throw new IOException("Image not found: " + filePath);
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Удалить изображение
     */
    public void deleteImage(String imagePath) throws IOException {
        String relativePath = imagePath.replaceFirst("^/images/", "");
        Path filePath = Paths.get(imagesPath, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Image deleted: {}", imagePath);
        }
    }

}
