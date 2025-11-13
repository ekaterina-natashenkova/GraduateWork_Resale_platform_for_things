--liquibase formatted sql

-- changeset ekaterina-natashenkova:1
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(300) NOT NULL UNIQUE,
    password VARCHAR(300) NOT NULL,
    first_name VARCHAR(300) NOT NULL,
    last_name VARCHAR(300) NOT NULL,
    email VARCHAR(300) NOT NULL UNIQUE,
    phone VARCHAR(300) NOT NULL UNIQUE,
    role VARCHAR(300) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    image_path VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Таблица с данными пользователей';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN users.username IS 'Логин';
COMMENT ON COLUMN users.password IS 'Пароль';
COMMENT ON COLUMN users.first_name IS 'Имя';
COMMENT ON COLUMN users.last_name IS 'Фамилия';
COMMENT ON COLUMN users.email IS 'Электронный адрес';
COMMENT ON COLUMN users.phone IS 'Номер телефона в формате +7XXXXXXXXXX';
COMMENT ON COLUMN users.role IS 'Права пользователя (USER или ADMIN)';
COMMENT ON COLUMN users.image_path IS 'Ссылка на аватар';
COMMENT ON COLUMN users.created_at IS 'Временная метка создания пользователя';
COMMENT ON COLUMN users.updated_at IS 'Временная метка последнего обновления пользователя';

CREATE TABLE ads (
    id SERIAL PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    price INTEGER NOT NULL CHECK (price >= 0),
    description TEXT NOT NULL,
    image_path VARCHAR(300),
    image_size INTEGER,
    image_type VARCHAR(300),
    author_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ads_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE ads IS 'Таблица с объявлениями о перепродаже товаров';
COMMENT ON COLUMN ads.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN ads.title IS 'Заголовок объявления';
COMMENT ON COLUMN ads.price IS 'Цена товара в рублях (больше или равно 0)';
COMMENT ON COLUMN ads.description IS 'Подробное описание товара';
COMMENT ON COLUMN ads.image_path IS 'Ссылка на изображение';
COMMENT ON COLUMN ads.image_size IS 'Размер файла-изображения';
COMMENT ON COLUMN ads.image_type IS 'Тип файла-изображения';
COMMENT ON COLUMN ads.author_id IS 'ID пользователя, создавшего объявление';
COMMENT ON COLUMN ads.created_at IS 'Временная метка создания объявления';
COMMENT ON COLUMN ads.updated_at IS 'Временная метка последнего обновления объявления';

CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    author_id INTEGER NOT NULL,
    ad_id INTEGER NOT NULL,

    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comments_ad
        FOREIGN KEY (ad_id)
        REFERENCES ads(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE comments IS 'Таблица с комментариями к объявлениям о перепродаже товаров';
COMMENT ON COLUMN comments.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN comments.text IS 'Текст комментария';
COMMENT ON COLUMN comments.created_at IS 'Временная метка создания комментария';
COMMENT ON COLUMN comments.author_id IS 'ID пользователя, создавшего комментарий';
COMMENT ON COLUMN comments.ad_id IS 'ID объявления, к которому создан комментарий';
