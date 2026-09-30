# 구도와 심도 데이터베이스 명세

## 핵심 관계

```text
club_generations 1 ─── N users N ─── 1 club_positions
users 1 ─── N user_equipment N ─── 1 equipment_manufacturers
users N ─── N tags                 (user_tags)
users 1 ─── N photo_posts 1 ─── N photo_post_images
photo_posts N ─── N tags           (photo_post_tags)
club_generations 1 ─── N club_activities 1 ─── N club_activity_images
```

## 테이블 역할

| 테이블 | 역할 |
| --- | --- |
| `users` | 로그인, 이름·학번·기수·직책·프로필 |
| `club_generations`, `club_positions` | 기수와 직책의 기준값 |
| `equipment_manufacturers`, `user_equipment` | 제조사와 회원 장비 목록 |
| `tags`, `user_tags` | 프로필용 태그. 색상은 `#RRGGBB` 형식 |
| `photo_posts`, `photo_post_images`, `photo_post_tags` | 사진 공유 갤러리, 복수 사진, 사진 태그 |
| `club_activities`, `club_activity_images` | 기수별 동아리 활동 기록과 사진 |
| `user_refresh_tokens` | 선택 사항: 로그인 유지용 refresh token 해시 |

## 화면별 조회 방법

- **마이페이지 제조사 태그**: `user_equipment → equipment_manufacturers`를 `DISTINCT` 조회합니다. 수동 태그와 중복 저장하지 않습니다.
- **사진 공유 격자**: `photo_posts`의 `status='PUBLISHED'`만 최신순 조회하고, 각 게시글의 `sort_order=0` 이미지를 썸네일로 씁니다.
- **내가 올린 사진 전체보기**: 현재 사용자 ID로 `photo_posts.author_id`를 필터링합니다.
- **활동 사진**: `club_activities`를 `generation_id`로 묶어 기수별로 렌더링합니다.

## 실행

MySQL 8.0 이상에서 아래를 실행합니다.

```bash
mysql -u root -p < database/schema.sql
```

애플리케이션용 DB 계정은 별도로 만들고 최소 권한만 부여하세요. `password_hash`와 `token_hash`에는 절대 평문 비밀번호·토큰을 저장하지 않습니다.
