--liquibase formatted sql

-- changeset ekaterina-natashenkova:3
CREATE TABLE images (
    id SERIAL PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    file_size BIGINT,
    content_type VARCHAR(100),
    original_file_name VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ad_id INTEGER,

    CONSTRAINT fk_images_ad
        FOREIGN KEY (ad_id)
        REFERENCES ads(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE images IS 'Таблица для хранения метаданных изображений';
COMMENT ON COLUMN images.id IS 'Уникальный идентификатор изображения';
COMMENT ON COLUMN images.file_path IS 'Путь к файлу изображения';
COMMENT ON COLUMN images.file_size IS 'Размер файла в байтах';
COMMENT ON COLUMN images.content_type IS 'MIME-тип изображения (image/jpeg, image/png, etc.)';
COMMENT ON COLUMN images.original_file_name IS 'Оригинальное имя файла';
COMMENT ON COLUMN images.created_at IS 'Временная метка создания записи';
COMMENT ON COLUMN images.ad_id IS 'ID объявления, к которому привязано изображение';

-- Добавляем колонку image_id в таблицу users для связи с аватаром
ALTER TABLE users ADD COLUMN image_id INTEGER;

ALTER TABLE users
ADD CONSTRAINT fk_users_image
    FOREIGN KEY (image_id)
    REFERENCES images(id)
    ON DELETE SET NULL;

COMMENT ON COLUMN users.image_id IS 'ID изображения-аватара пользователя';

-- Создаем индексы для быстрого поиска
CREATE INDEX idx_images_file_path ON images(file_path);
COMMENT ON INDEX idx_images_file_path IS 'Индекс для быстрого поиска изображений по пути';

CREATE INDEX idx_images_ad_id ON images(ad_id);
COMMENT ON INDEX idx_images_ad_id IS 'Индекс для поиска изображений по объявлению';

CREATE INDEX idx_users_image_id ON users(image_id);
COMMENT ON INDEX idx_users_image_id IS 'Индекс для поиска пользователей по изображению';

-- changeset ekaterina-natashenkova:4
-- Обновляем комментарии к существующим колонкам для ясности
COMMENT ON COLUMN users.image_path IS 'Ссылка на аватар (устаревшее поле, использовать image_id)';
COMMENT ON COLUMN ads.image_path IS 'Ссылка на главное изображение объявления (устаревшее поле, использовать связь с images)';