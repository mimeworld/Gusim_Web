# 구도와 심도

React 프론트엔드와 Node.js API를 분리한 사진 동아리 커뮤니티입니다.

## 실행

```bash
npm install --prefix frontend
npm install --prefix backend
npm run dev       # 프론트엔드
npm run server    # 백엔드 API
```

프론트엔드는 현재 로컬 저장소를 임시 데이터 소스로 사용합니다. 백엔드와 MySQL을 붙일 때는 루트의 `.env.example`을 복사해 환경변수를 설정하고, `frontend/src/services/community.js`의 각 함수를 API 호출로 바꾸면 됩니다.
