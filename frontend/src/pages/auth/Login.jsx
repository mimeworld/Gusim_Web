import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useApp } from "../../context";
import { PageTitle } from "../../components";
export default function Login() {
  const { login } = useApp();
  const navigate = useNavigate(),
    location = useLocation();
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await login(Object.fromEntries(new FormData(event.currentTarget)));
      navigate(location.state?.from || "/mypage", { replace: true });
    } catch (error) {
      setError(error.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageTitle eyebrow="MEMBERS ONLY" title="로그인">
        <p>동아리 관리자가 발급한 계정으로 로그인해주세요.</p>
      </PageTitle>
      <main className="shell page-body">
        <form onSubmit={submit} className="panel mx-auto max-w-md space-y-5">
          <label>
            학번
            <input
              name="studentNumber"
              autoComplete="username"
              required
              pattern="[0-9]{4,20}"
            />
          </label>
          <label>
            비밀번호
            <input
              name="password"
              type="password"
              autoComplete="current-password"
              required
              maxLength={60}
            />
          </label>
          {error && (
            <p role="alert" className="alert">
              {error}
            </p>
          )}
          <button className="button w-full" disabled={busy}>
            {busy ? "확인 중…" : "로그인"}
          </button>
          <p className="text-sm leading-6 text-slate-500">
            계정 발급 또는 비밀번호 초기화는 동아리 관리자에게 문의해주세요.
          </p>
        </form>
      </main>
    </>
  );
}
