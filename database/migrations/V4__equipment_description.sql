-- V3까지 적용한 기존 DB에 한 번만 적용. 새 DB에는 schema.sql 사용.
ALTER TABLE equipment_tags ADD COLUMN description VARCHAR(1000) NULL;
