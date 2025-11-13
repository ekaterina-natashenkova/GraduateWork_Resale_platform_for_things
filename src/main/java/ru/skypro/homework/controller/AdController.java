package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.Ads;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Объявления")
public class AdController {

    @Operation(
            summary = "Получение всех объявлений",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = Ads.class))
                    )
            }
    )

    @GetMapping
    public ResponseEntity<Ads> getAllAds() {
        log.info("Called getAllAds");
        Ads ads = new Ads();
        return ResponseEntity.ok(ads);
    }

    @Operation(
            summary = "Добавление объявления",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Created",
                            content = @Content(schema = @Schema(implementation = Ad.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping
    public ResponseEntity<Ad> addAd(HttpServletRequest request,
                                    @RequestParam(value = "title", required = false) String title,
                                    @RequestParam(value = "price", required = false) String priceStr,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam(value = "image", required = false) MultipartFile image) {

        log.info("=== POST /ads DIAGNOSTICS ===");
        log.info("Content-Type: {}", request.getContentType());
        log.info("Parameter names: {}", request.getParameterMap().keySet());

        log.info("Title param: {}", title);
        log.info("Price param: {}", priceStr);
        log.info("Description param: {}", description);
        log.info("Image param: {}", image != null ? image.getOriginalFilename() : "null");

        // Преобразуем price
        Integer price = null;
        if (priceStr != null) {
            try {
                price = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid price format: {}", priceStr);
            }
        }

        Ad ad = new Ad();
        ad.setTitle(title);
        ad.setPrice(price);

        return ResponseEntity.status(HttpStatus.CREATED).body(ad);
    }

    // более компактный вариант не работает в postman
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Ad> addAd(@RequestPart("properties") CreateOrUpdateAd properties,
//                                    @RequestPart("image") MultipartFile image) {
//        log.info("Called addAd with title: {}", properties.getTitle());
//        Ad ad = new Ad();
//        return ResponseEntity.status(HttpStatus.CREATED).body(ad);
//    }

    @Operation(
            summary = "Получение информации об объявлении",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = ExtendedAd.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAds(@PathVariable("id") Integer id) {
        log.info("Called getAds with id: {}", id);
        ExtendedAd extendedAd = new ExtendedAd();
        return ResponseEntity.ok(extendedAd);
    }

    @Operation(
            summary = "Удаление объявления",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAd(@PathVariable("id") Integer id) {
        log.info("Called removeAd with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Обновление информации об объявлении",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = Ad.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<Ad> updateAds(@PathVariable("id") Integer id,
                                        @RequestBody CreateOrUpdateAd createOrUpdateAd) {
        log.info("Called updateAds with id: {}", id);
        Ad ad = new Ad();
        return ResponseEntity.ok(ad);
    }

    @Operation(
            summary = "Получение объявлений авторизованного пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = Ads.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe() {
        log.info("Called getAdsMe");
        Ads ads = new Ads();
        return ResponseEntity.ok(ads);
    }

    @Operation(
            summary = "Обновление картинки объявления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PatchMapping(value = "/{id}/image") // убрала параметр (ругается postman) consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    public ResponseEntity<?> updateImage(@PathVariable("id") Integer id,
                                         @RequestParam("image") MultipartFile image) {
        log.info("Called updateImage with id: {}", id);
        return ResponseEntity.ok().build();
    }

}
