package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "Изображения")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/ads/{adId}/image")
    public ResponseEntity<byte[]> getAdImage(@PathVariable Integer adId) {
        log.info("Request for ad image, adId: {}", adId);

        try {
            byte[] imageBytes = imageService.getAdImage(adId);
            String contentType = imageService.getImageContentType(adId, "ad");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(imageBytes);

        } catch (IOException e) {
            log.warn("Ad image not found for adId: {}", adId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error loading ad image for adId: {}", adId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/{userId}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Integer userId) {
        log.info("Request for user avatar, userId: {}", userId);

        try {
            byte[] imageBytes = imageService.getUserAvatar(userId);
            String contentType = imageService.getImageContentType(userId, "user");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(imageBytes);

        } catch (IOException e) {
            log.warn("User avatar not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error loading user avatar for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

}