import { Navigate, useLocation } from "react-router-dom";
import { useApp } from "../context";
export default function Protected({ children, admin = false }) {
  const { profile, loading } = useApp();
  const location = useLocation();
  if (loading) return <p className="shell page-body">로그인 확인 중…</p>;
  if (!profile)
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (admin && profile.role !== "ADMIN")
    return (
      <main className="shell page-body">
        <p role="alert" className="alert">
          관리자만 접근할 수 있습니다.
        </p>
      </main>
    );
  return children;
}
