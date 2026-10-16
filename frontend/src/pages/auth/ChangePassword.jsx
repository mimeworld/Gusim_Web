import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../services/community";
import { PageTitle } from "../../components";
import { useApp } from "../../context";
export default function ChangePassword() {
  const { clearSession } = useApp();
  const nav = useNavigate();
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  async function submit(event) {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    if (data.newPassword !== data.confirmPassword) {
      setError("새 비밀번호 확인이 일치하지 않습니다.");
      window.dispatchEvent(
        new CustomEvent("api-error", {
          detail: "새 비밀번호 확인이 일치하지 않습니다.",
        }),
      );
      return;
    }
    setBusy(true);
    try {
      await api("/me/password", {
        method: "PUT",
        body: JSON.stringify({
          currentPassword: data.currentPassword,
          newPassword: data.newPassword,
        }),
      });
      clearSession();
      nav("/login", { replace: true });
      window.dispatchEvent(
        new CustomEvent("api-notice", {
          detail: "비밀번호를 변경했습니다. 새 비밀번호로 다시 로그인해주세요.",
        }),
      );
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageTitle eyebrow="ACCOUNT SECURITY" title="비밀번호 변경" />
      <main className="shell page-body">
        <form className="panel mx-auto max-w-md space-y-5" onSubmit={submit}>
          <p className="notice">
            초기 비밀번호는 gusim1234입니다. 새 비밀번호는 8자 이상 60자 이하로
            입력해주세요. UTF-8 기준 최대 72바이트를 지원합니다.
          </p>
          <label>
            현재 비밀번호
            <input
              name="currentPassword"
              type="password"
              required
              autoComplete="current-password"
            />
          </label>
          <label>
            새 비밀번호
            <input
              name="newPassword"
              type="password"
              required
              minLength={8}
              maxLength={60}
              autoComplete="new-password"
            />
          </label>
          <label>
            새 비밀번호 확인
            <input
              name="confirmPassword"
              type="password"
              required
              minLength={8}
              maxLength={60}
              autoComplete="new-password"
            />
          </label>
          {error && (
            <p className="alert" role="alert">
              {error}
            </p>
          )}
          <button className="button" disabled={busy}>
            비밀번호 변경
          </button>
        </form>
      </main>
    </>
  );
}
