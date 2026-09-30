// API 교체 지점: DB 연결 후 fetch(`${import.meta.env.VITE_API_URL}/...`)로 변경합니다.
// 현재 화면은 AppContext가 localStorage를 통해 임시 상태를 유지합니다.
export const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:4000/api';
