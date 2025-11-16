package ru.skypro.homework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ImageStorageInitializer {

    @Value("${app.images.path:uploads/images}")
    private String imagesPath;

    @Value("${app.images.auto-create-dir:true}")
    private boolean autoCreateDir;

    @PostConstruct
    public void initializeImageStorage() {
        if (!autoCreateDir) {
            log.info("Auto directory creation is disabled");
            return;
        }

        try {
            createDirectory("users");
            createDirectory("ads");
            log.info("Image storage successfully initialized at: {}", imagesPath);

        } catch (IOException e) {
            log.error("Failed to initialize image storage at: {}", imagesPath, e);
            // Не бросаем исключение, чтобы приложение могло запуститься
        }
    }

    private void createDirectory(String subdirectory) throws IOException {
        Path directoryPath = Paths.get(imagesPath, subdirectory);

        if (Files.exists(directoryPath)) {
            log.debug("Directory already exists: {}", directoryPath);
            return;
        }

        Files.createDirectories(directoryPath);
        log.info("📁 Created image directory: {}", directoryPath);
    }

}