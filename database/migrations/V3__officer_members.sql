-- V2까지 적용한 DB에 한 번만 적용. 먼저 백업하세요.
ALTER TABLE officers ADD COLUMN member_id BIGINT NULL,
 ADD CONSTRAINT uq_officer_member UNIQUE (member_id),
 ADD CONSTRAINT fk_officer_member FOREIGN KEY (member_id) REFERENCES users(id);
-- 이름만으로 자동 연결하지 않습니다(동명이인 방지).
-- 관리자 > 임원진 편집에서 각 기존 카드를 실제 회원과 연결하면 관리자 권한도 함께 부여됩니다.
-- 모두 연결한 뒤 아래 검사를 수행하고 결과가 0일 때 NOT NULL 제약을 적용할 수 있습니다.
-- SELECT COUNT(*) FROM officers WHERE member_id IS NULL;
-- ALTER TABLE officers MODIFY member_id BIGINT NOT NULL;
