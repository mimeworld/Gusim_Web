-- 이전 답변의 schema.sql로 생성한 MySQL DB에만 한 번 적용.
-- 새로운 DB에는 schema.sql만 사용. 적용 전 백업하세요.
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
                  ADD COLUMN session_version INT NOT NULL DEFAULT 0;
CREATE TABLE officers (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(50) NOT NULL, position VARCHAR(50) NOT NULL,
 bio VARCHAR(500) NOT NULL, photo VARCHAR(2048) NOT NULL,
 sort_order INT NOT NULL DEFAULT 0
);
CREATE TABLE site_pages (
 slug VARCHAR(30) PRIMARY KEY, title VARCHAR(200) NOT NULL, content LONGTEXT NOT NULL
);
-- 기존 직책/프로필 사진을 최초 임원진 소개로 복사. 이후 두 정보는 독립적으로 수정.
INSERT INTO officers (name,position,bio,photo,sort_order)
 SELECT name,position,COALESCE(bio,''),COALESCE(avatar,''),0
 FROM users WHERE position IS NOT NULL AND position <> '';
-- 기존 OT 글은 posts에 보존되지만 새 OT 페이지로 자동 합치지 않습니다.
-- 관리자가 내용을 검토해 /admin의 OT 편집 화면에서 반영하세요.
