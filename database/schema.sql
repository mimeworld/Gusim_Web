-- 구도와 심도 MySQL 8.0+ schema
-- 사진 파일 자체는 DB에 저장하지 않습니다. object_storage_key / public_url에
-- S3, Cloudinary, NAS 등의 스토리지 위치를 저장하세요.

CREATE DATABASE IF NOT EXISTS gudo_simdo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE gudo_simdo;

-- 기수와 직책을 별도 테이블로 관리해 사용자 정보의 오탈자와 중복을 방지합니다.
CREATE TABLE club_generations (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  generation_number SMALLINT UNSIGNED NOT NULL,
  started_on DATE NOT NULL,
  ended_on DATE NULL,
  name VARCHAR(50) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_generation_number (generation_number),
  CONSTRAINT chk_generation_dates CHECK (ended_on IS NULL OR ended_on >= started_on)
) ENGINE=InnoDB;

CREATE TABLE club_positions (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_position_name (name)
) ENGINE=InnoDB;

-- login_id는 학번과 독립된 서비스 로그인 ID입니다.
-- password_hash에는 bcrypt 또는 argon2의 해시만 저장합니다. 평문 비밀번호는 금지합니다.
CREATE TABLE users (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  login_id VARCHAR(50) NOT NULL,
  name VARCHAR(50) NOT NULL,
  student_number VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  generation_id BIGINT UNSIGNED NULL,
  position_id BIGINT UNSIGNED NULL,
  bio VARCHAR(500) NULL,
  profile_image_url VARCHAR(2048) NULL,
  status ENUM('ACTIVE', 'INACTIVE', 'WITHDRAWN') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_users_login_id (login_id),
  UNIQUE KEY uq_users_student_number (student_number),
  KEY ix_users_generation_id (generation_id),
  KEY ix_users_position_id (position_id),
  CONSTRAINT fk_users_generation FOREIGN KEY (generation_id) REFERENCES club_generations(id) ON DELETE SET NULL,
  CONSTRAINT fk_users_position FOREIGN KEY (position_id) REFERENCES club_positions(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE equipment_manufacturers (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_manufacturer_name (name)
) ENGINE=InnoDB;

CREATE TABLE user_equipment (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  manufacturer_id BIGINT UNSIGNED NOT NULL,
  category ENUM('CAMERA', 'LENS', 'FLASH', 'ACCESSORY') NOT NULL,
  model_name VARCHAR(150) NOT NULL,
  description VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY ix_user_equipment_user_id (user_id),
  KEY ix_user_equipment_manufacturer_id (manufacturer_id),
  CONSTRAINT fk_equipment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_equipment_manufacturer FOREIGN KEY (manufacturer_id) REFERENCES equipment_manufacturers(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 사용자 프로필에 수동으로 표시할 태그입니다. 제조사 태그는 user_equipment JOIN으로 자동 계산합니다.
CREATE TABLE tags (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  color CHAR(7) NOT NULL DEFAULT '#3EAE57',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tag_name (name),
  CONSTRAINT chk_tag_color CHECK (color REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB;

CREATE TABLE user_tags (
  user_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, tag_id),
  KEY ix_user_tags_tag_id (tag_id),
  CONSTRAINT fk_user_tags_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 사진 공유 게시판
CREATE TABLE photo_posts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  author_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NULL,
  visibility ENUM('PUBLIC', 'MEMBERS') NOT NULL DEFAULT 'MEMBERS',
  status ENUM('PUBLISHED', 'HIDDEN', 'DELETED') NOT NULL DEFAULT 'PUBLISHED',
  published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY ix_photo_posts_author_published (author_id, published_at DESC),
  KEY ix_photo_posts_feed (status, visibility, published_at DESC),
  CONSTRAINT fk_photo_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 게시글당 여러 장의 사진을 허용하며 sort_order가 대표/표시 순서를 결정합니다.
CREATE TABLE photo_post_images (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  photo_post_id BIGINT UNSIGNED NOT NULL,
  object_storage_key VARCHAR(1024) NOT NULL,
  public_url VARCHAR(2048) NOT NULL,
  alt_text VARCHAR(255) NULL,
  width INT UNSIGNED NULL,
  height INT UNSIGNED NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_photo_images_post_order (photo_post_id, sort_order),
  CONSTRAINT fk_photo_images_post FOREIGN KEY (photo_post_id) REFERENCES photo_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE photo_post_tags (
  photo_post_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (photo_post_id, tag_id),
  KEY ix_photo_post_tags_tag_id (tag_id),
  CONSTRAINT fk_post_tags_post FOREIGN KEY (photo_post_id) REFERENCES photo_posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 기수별 활동 기록: 활동 한 건에 설명과 여러 장의 사진을 연결합니다.
CREATE TABLE club_activities (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  generation_id BIGINT UNSIGNED NOT NULL,
  author_id BIGINT UNSIGNED NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NULL,
  activity_date DATE NULL,
  status ENUM('PUBLISHED', 'DRAFT', 'HIDDEN') NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY ix_activities_generation_date (generation_id, activity_date DESC),
  CONSTRAINT fk_activities_generation FOREIGN KEY (generation_id) REFERENCES club_generations(id) ON DELETE RESTRICT,
  CONSTRAINT fk_activities_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE club_activity_images (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT UNSIGNED NOT NULL,
  object_storage_key VARCHAR(1024) NOT NULL,
  public_url VARCHAR(2048) NOT NULL,
  alt_text VARCHAR(255) NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_activity_images_order (activity_id, sort_order),
  CONSTRAINT fk_activity_images_activity FOREIGN KEY (activity_id) REFERENCES club_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 선택 확장: 로그인 유지 기능을 서버 세션 대신 refresh token으로 구현할 때 사용합니다.
CREATE TABLE user_refresh_tokens (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  revoked_at DATETIME NULL,
  UNIQUE KEY uq_refresh_token_hash (token_hash),
  KEY ix_refresh_tokens_user_id (user_id),
  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 초깃값 예시
INSERT INTO club_positions (name, sort_order) VALUES
  ('회장', 1), ('부회장', 2), ('운영진', 3), ('부원', 99);

INSERT INTO equipment_manufacturers (name) VALUES
  ('Canon'), ('Nikon'), ('Sony'), ('Fujifilm'), ('Leica'), ('Panasonic'), ('Sigma'), ('Tamron'), ('기타');
