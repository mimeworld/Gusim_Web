-- MySQL 8.4; 새 데이터베이스에 한 번 실행. 기존 스키마의 업그레이드 스크립트가 아닙니다.
CREATE DATABASE IF NOT EXISTS gudo_simdo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE gudo_simdo;
CREATE TABLE users (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(50) NOT NULL,
 student_number VARCHAR(20) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 role VARCHAR(20) NOT NULL DEFAULT 'USER',
 bio VARCHAR(500), avatar VARCHAR(2048), position VARCHAR(50),
 active BOOLEAN NOT NULL DEFAULT TRUE, session_version INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_role CHECK (role IN ('USER','ADMIN'))
);
CREATE TABLE posts (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 author_id BIGINT NOT NULL, author VARCHAR(50) NOT NULL,
 title VARCHAR(200) NOT NULL, content LONGTEXT,
 category VARCHAR(20) NOT NULL, board_category VARCHAR(20), image VARCHAR(2048), date TIMESTAMP(6) NOT NULL,
 CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(id),
 CONSTRAINT chk_category CHECK (category IN ('PHOTO','CRITIQUE','OT','BOARD')),
 INDEX ix_posts_category_date (category,date), INDEX ix_posts_author (author_id)
);
CREATE TABLE post_images (
 post_id BIGINT NOT NULL, sort_order INT NOT NULL, image_url VARCHAR(2048) NOT NULL,
 PRIMARY KEY(post_id,sort_order),
 CONSTRAINT fk_post_image FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE TABLE equipment_tags (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL,
 type VARCHAR(30) NOT NULL, maker VARCHAR(100) NOT NULL, model VARCHAR(150) NOT NULL, description VARCHAR(1000),
 CONSTRAINT fk_equipment_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE post_tags (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE
);
CREATE TABLE post_tag_links (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, post_id BIGINT NOT NULL, tag_id BIGINT NOT NULL,
 UNIQUE KEY uq_post_tag (post_id,tag_id),
 FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE,
 FOREIGN KEY(tag_id) REFERENCES post_tags(id) ON DELETE CASCADE
);
CREATE TABLE officers (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 member_id BIGINT NOT NULL UNIQUE,
 name VARCHAR(50) NOT NULL, position VARCHAR(50) NOT NULL,
 bio VARCHAR(500) NOT NULL, photo VARCHAR(2048) NOT NULL,
 sort_order INT NOT NULL DEFAULT 0,
 CONSTRAINT fk_officer_member FOREIGN KEY (member_id) REFERENCES users(id)
);
CREATE TABLE site_pages (
 slug VARCHAR(30) PRIMARY KEY, title VARCHAR(200) NOT NULL, content LONGTEXT NOT NULL
);
-- 최초 관리자: 서버 환경변수로 생성. 비밀번호 평문을 SQL에 넣지 않습니다.
