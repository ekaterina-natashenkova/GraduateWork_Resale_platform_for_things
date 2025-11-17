--liquibase formatted sql

-- changeset ekaterina-natashenkova:5
-- Переносим аватары пользователей из image_path в таблицу images
INSERT INTO images (file_path, content_type, original_file_name, created_at)
SELECT
    u.image_path,
    'image/jpeg',
    'avatar.jpg',
    u.created_at
FROM users u
WHERE u.image_path IS NOT NULL
  AND u.image_path != ''
  AND NOT EXISTS (SELECT 1 FROM images i WHERE i.file_path = u.image_path);

-- Связываем пользователей с их изображениями
UPDATE users u
SET image_id = i.id
FROM images i
WHERE u.image_path = i.file_path
  AND u.image_path IS NOT NULL
  AND u.image_path != '';

-- Переносим изображения объявлений
INSERT INTO images (file_path, content_type, original_file_name, ad_id, created_at)
SELECT
    a.image_path,
    COALESCE(a.image_type, 'image/jpeg'),
    'ad_image.jpg',
    a.id,
    a.created_at
FROM ads a
WHERE a.image_path IS NOT NULL
  AND a.image_path != ''
  AND NOT EXISTS (SELECT 1 FROM images i WHERE i.file_path = a.image_path);

-- changeset ekaterina-natashenkova:6
-- Добавляем колонку для главного изображения объявления
ALTER TABLE ads ADD COLUMN main_image_id INTEGER;

ALTER TABLE ads
ADD CONSTRAINT fk_ads_main_image
    FOREIGN KEY (main_image_id)
    REFERENCES images(id)
    ON DELETE SET NULL;

COMMENT ON COLUMN ads.main_image_id IS 'ID главного изображения объявления';