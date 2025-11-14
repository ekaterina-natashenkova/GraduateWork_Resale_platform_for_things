package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.ImageService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/**")
    public ResponseEntity<byte[]> getImage(HttpServletRequest request) {
        String imagePath = request.getRequestURI();
        log.info("Image request for path: {}", imagePath);

        try {
            byte[] imageBytes = imageService.getImage(imagePath);

            // Определяем Content-Type по расширению файла, используя вспомогательный метод
            String contentType = determineContentType(imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Кэширование на 1 час
                    .body(imageBytes);

        } catch (IOException e) {
            log.warn("Image not found: {}", imagePath);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error loading image: {}", imagePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Определяет Content-Type по расширению файла
     */
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

}