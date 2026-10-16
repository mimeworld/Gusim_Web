# 데이터베이스

- 신규 DB: `schema.sql`.
- 직전 버전 DB: 백업 후 `migrations/V2__admin_content.sql`, `migrations/V3__officer_members.sql`을 순서대로 한 번만 적용. V2 적용 상태면 V3부터 적용. 장비 설명은 V4__equipment_description.sql도 적용.
- 기존 임원 카드는 관리자가 실제 회원과 연결해야 권한·태그가 부여됩니다.
- 자동 스키마 수정은 하지 않으며 JPA는 `validate`로 실행합니다.
- 접속 설정: `backend/src/main/resources/application-db.yml`.
- 최초 관리자: 환경변수로 생성. 평문 비밀번호를 SQL에 저장하지 않습니다.

전체 API·입출력·ER 관계 설명·Docker 실행은 루트 README.md를 참고하세요.

- 다중 사진/게시판 분류: V5__multiple_post_images.sql. 기존 단일 사진은 첫 사진으로, 기존 게시판 분류는 CHAT(잡담)으로 이관합니다.
