package ru.skypro.homework.model.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ads")
@EntityListeners(AuditingEntityListener.class)
public class AdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String description;

    @Column(name = "image_path", length = 300)
    private String imagePath;

    @Column(name = "image_size")
    private Integer imageSize;

    @Column(name = "image_type", length = 300)
    private String imageType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @OneToMany(mappedBy = "ad", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImageEntity> images = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_image_id")
    private ImageEntity image;

    /**
     * Получить главное изображение
     */
    public ImageEntity getImage() {
        return this.image != null ? this.image :
                (!this.images.isEmpty() ? this.images.get(0) : null);
    }

    /**
     * Установить главное изображение
     */
    public void setImage(ImageEntity image) {
        this.image = image;
        if (image != null) {
            image.setAd(this);
            // Также обновляем imagePath для обратной совместимости
            this.imagePath = image.getFilePath();
        }
    }

    /**
     * Добавить изображение к объявлению
     */
    public void addImage(ImageEntity image) {
        image.setAd(this);
        this.images.add(image);
        // Если это первое изображение, устанавливаем как главное
        if (this.images.size() == 1) {
            this.image = image;
            this.imagePath = image.getFilePath();
        }
    }

    // Геттер для обратной совместимости
    public String getImagePath() {
        return this.image != null ? this.image.getFilePath() : this.imagePath;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
