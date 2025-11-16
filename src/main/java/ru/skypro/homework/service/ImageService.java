package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.entity.AdEntity;
import ru.skypro.homework.model.entity.ImageEntity;
import ru.skypro.homework.model.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final ImageRepository imageRepository;

    @Value("${app.images.path:uploads/images}")
    private String imagesPath;

    public byte[] getUserAvatar(Integer userId) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IOException("User not found: " + userId));

        String imagePath = user.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            throw new IOException("User has no avatar: " + userId);
        }

        return getImageFile(imagePath);
    }

    public byte[] getAdImage(Integer adId) throws IOException {
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new IOException("Ad not found: " + adId));

        String imagePath = ad.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            throw new IOException("Ad has no image: " + adId);
        }

        return getImageFile(imagePath);
    }

    public String getImageContentType(Integer entityId, String type) {
        try {
            String imagePath = null;

            if ("user".equals(type)) {
                UserEntity user = userRepository.findById(entityId).orElse(null);
                imagePath = user != null ? user.getImagePath() : null;
            } else if ("ad".equals(type)) {
                AdEntity ad = adRepository.findById(entityId).orElse(null);
                imagePath = ad != null ? ad.getImagePath() : null;
            }

            if (imagePath != null) {
                return determineContentType(imagePath);
            }
        } catch (Exception e) {
            log.debug("Error determining content type for {} {}: {}", type, entityId, e.getMessage());
        }

        return "image/jpeg";
    }

    /**
     * Сохранить ImageEntity для объявления и вернуть сущность
     */
    public ImageEntity saveAdImageEntity(MultipartFile image, Integer adId) throws IOException {
        String prefix = adId != null ? "ad_" + adId : "ad_temp";
        String filename = generateFilename(prefix, getFileExtension(image));
        Path filePath = Paths.get(imagesPath, "ads", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        String imageUrl = "/images/ads/" + filename;

        // Создаем и сохраняем ImageEntity
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setFilePath(imageUrl);
        imageEntity.setContentType(image.getContentType());
        imageEntity.setFileSize(image.getSize());
        imageEntity.setOriginalFileName(image.getOriginalFilename());

        return imageRepository.save(imageEntity);
    }

    /**
     * Удалить ImageEntity по ID
     */
    public void deleteImageEntity(Integer imageId) {
        try {
            ImageEntity imageEntity = imageRepository.findById(imageId)
                    .orElseThrow(() -> new IOException("Image entity not found: " + imageId));

            // Удаляем файл с диска
            String relativePath = imageEntity.getFilePath().replaceFirst("^/images/", "");
            Path filePath = Paths.get(imagesPath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Image file deleted: {}", filePath);
            }

            // Удаляем из БД
            imageRepository.deleteById(imageId);
            log.info("Image entity deleted: {}", imageId);

        } catch (IOException e) {
            log.warn("Failed to delete image file for entity id: {}", imageId, e);
            // Все равно удаляем запись из БД
            imageRepository.deleteById(imageId);
        } catch (Exception e) {
            log.error("Error deleting image entity: {}", imageId, e);
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    /**
     * Получить файл изображения с диска
     */
    private byte[] getImageFile(String imagePath) throws IOException {
        String relativePath = imagePath.replaceFirst("^/images/", "");
        Path filePath = Paths.get(imagesPath, relativePath);

        if (!Files.exists(filePath)) {
            throw new IOException("Image file not found: " + filePath);
        }

        return Files.readAllBytes(filePath);
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg";
    }

    private String generateFilename(String prefix, String extension) {
        return prefix + "_" + System.currentTimeMillis() + extension;
    }

    private String determineContentType(String filePath) {
        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filePath.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }

    public String saveUserImage(MultipartFile image, String userEmail) throws IOException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IOException("User not found: " + userEmail));

        String filename = generateFilename("user_" + user.getId(), getFileExtension(image));
        Path filePath = Paths.get(imagesPath, "users", filename);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, image.getBytes());

        String imageUrl = "/images/users/" + filename;

        // Создаем и сохраняем ImageEntity
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setFilePath(imageUrl);
        imageEntity.setContentType(image.getContentType());
        imageEntity.setFileSize(image.getSize());
        imageEntity.setOriginalFileName(image.getOriginalFilename());

        ImageEntity savedImage = imageRepository.save(imageEntity);

        // Обновляем пользователя
        user.setImage(savedImage);
        user.setImagePath(imageUrl); // Для обратной совместимости
        userRepository.save(user);

        log.info("User image saved: {}", imageUrl);
        return imageUrl;
    }

}