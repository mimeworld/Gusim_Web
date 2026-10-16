-- V4까지 적용한 기존 MySQL DB에 한 번만 적용하세요. 신규 DB에는 schema.sql만 적용.
ALTER TABLE posts ADD COLUMN board_category VARCHAR(20) NULL;
UPDATE posts SET board_category='CHAT' WHERE category='BOARD';
CREATE TABLE post_images (
 post_id BIGINT NOT NULL, sort_order INT NOT NULL, image_url VARCHAR(2048) NOT NULL,
 PRIMARY KEY(post_id,sort_order),
 CONSTRAINT fk_post_image FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE
);
INSERT INTO post_images(post_id,sort_order,image_url)
 SELECT id,0,image FROM posts WHERE image IS NOT NULL AND image <> '';
