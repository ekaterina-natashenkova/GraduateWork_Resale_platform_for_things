--liquibase formatted sql

-- changeset ekaterina-natashenkova:2
CREATE INDEX idx_users_username ON users(username);
COMMENT ON INDEX idx_users_username IS 'Индекс для поиска пользователей по username';

CREATE INDEX idx_users_role ON users(role);
COMMENT ON INDEX idx_users_role IS 'Индекс для фильтрации пользователей по role';

CREATE INDEX idx_ads_author_id ON ads(author_id);
COMMENT ON INDEX idx_ads_author_id IS 'Индекс для быстрого поиска объявлений по автору';

CREATE INDEX idx_ads_price ON ads(price);
COMMENT ON INDEX idx_ads_price IS 'Индекс для сортировки и фильтрации объявлений по цене';

CREATE INDEX idx_ads_created_at ON ads(created_at);
COMMENT ON INDEX idx_ads_created_at IS 'Индекс для сортировки объявлений по дате создания';

CREATE INDEX idx_comments_ad_id ON comments(ad_id);
COMMENT ON INDEX idx_comments_ad_id IS 'Индекс для быстрого поиска комментариев по объявлению';

CREATE INDEX idx_comments_author_id ON comments(author_id);
COMMENT ON INDEX idx_comments_author_id IS 'Индекс для быстрого поиска комментариев по автору'';

CREATE INDEX idx_comments_created_at ON comments(created_at);
COMMENT ON INDEX idx_comments_created_at IS 'Индекс для сортировки комментариев по дате создания';
