import MemberBadges from "../../components/MemberBadges";
import { useEffect, useState } from "react";
import { api } from "../../services/community";
import { useApp } from "../../context";
const blank = {
  name: "",
  studentNumber: "",
  password: "gusim1234",
  role: "USER",
  active: true,
};
export default function MembersPanel() {
  const { profile, refreshProfile } = useApp();
  const [members, setMembers] = useState([]),
    [form, setForm] = useState(blank),
    [editing, setEditing] = useState(null),
    [error, setError] = useState(""),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false),
    [password, setPassword] = useState("");
  const refresh = () => api("/admin/members").then(setMembers);
  useEffect(() => {
    refresh().catch((e) => setError(e.message));
  }, []);
  function edit(member) {
    setEditing(member.id);
    setForm({ ...member, password: "" });
    setPassword("");
    setMessage("");
    setError("");
  }
  function reset() {
    setEditing(null);
    setForm(blank);
    setPassword("");
  }
  const field = (name) => ({
    value: form[name],
    onChange: (e) => setForm({ ...form, [name]: e.target.value }),
  });
  async function save(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const body = editing
        ? {
            name: form.name,
            studentNumber: form.studentNumber,
            role: form.role,
            active: form.active,
          }
        : {
            name: form.name,
            studentNumber: form.studentNumber,
            role: form.role,
            password: form.password,
          };
      await api(editing ? `/admin/members/${editing}` : "/admin/members", {
        method: editing ? "PUT" : "POST",
        body: JSON.stringify(body),
      });
      await refresh();
      if (editing === profile.id) await refreshProfile();
      reset();
      setMessage("회원 정보를 저장했습니다.");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  async function changePassword(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await api(`/admin/members/${editing}/password`, {
        method: "PUT",
        body: JSON.stringify({ password }),
      });
      setPassword("");
      setMessage(
        "비밀번호를 초기화했습니다. 해당 회원은 다시 로그인해야 합니다.",
      );
      if (editing === profile.id) await refreshProfile();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="space-y-6">
      {error && (
        <p role="alert" className="alert">
          {error}
        </p>
      )}
      {message && (
        <p role="status" className="notice">
          {message}
        </p>
      )}
      <div className="grid items-start gap-8 lg:grid-cols-[340px_1fr]">
        <div className="space-y-5">
          <form className="panel space-y-4" onSubmit={save}>
            <h2>{editing ? "회원 수정" : "회원 추가"}</h2>
            <label>
              이름
              <input {...field("name")} required maxLength={50} />
            </label>
            <label>
              학번
              <input
                {...field("studentNumber")}
                required
                pattern="[0-9]{4,20}"
              />
            </label>
            {!editing && (
              <p className="notice">
                초기 비밀번호는 gusim1234로 지정됩니다. 회원은 로그인 후
                비밀번호를 변경할 수 있습니다. 비밀번호는 8자 이상 60자
                이하입니다.
              </p>
            )}
            <label>
              권한
              <select {...field("role")} disabled={editing === profile.id}>
                <option value="USER">일반 사용자</option>
                <option value="ADMIN">관리자</option>
              </select>
            </label>
            {editing && (
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={form.active}
                  disabled={editing === profile.id}
                  onChange={(e) =>
                    setForm({ ...form, active: e.target.checked })
                  }
                />
                활성 계정
              </label>
            )}
            <p className="text-xs leading-6 text-slate-500">
              관리자로 지정하면 회원과 페이지를 관리할 수 있습니다. 비활성
              계정은 로그인할 수 없습니다.
            </p>
            <div className="flex gap-2">
              <button className="button" disabled={busy}>
                저장
              </button>
              {editing && (
                <button
                  type="button"
                  className="secondary"
                  onClick={reset}
                  disabled={busy}
                >
                  취소
                </button>
              )}
            </div>
          </form>
          {editing && (
            <form className="panel space-y-4" onSubmit={changePassword}>
              <h2>비밀번호 초기화</h2>
              <p className="text-sm text-slate-500">
                8자 이상 60자 이하, UTF-8 최대 72바이트로 입력해주세요.
              </p>
              <label>
                새 비밀번호
                <input
                  type="password"
                  autoComplete="new-password"
                  required
                  minLength={8}
                  maxLength={60}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </label>
              <button className="secondary" disabled={busy}>
                비밀번호 변경
              </button>
            </form>
          )}
        </div>
        <section className="min-w-0">
          <h2 className="mb-4">
            회원 목록{" "}
            <span className="text-sm text-slate-500">{members.length}명</span>
          </h2>
          <div className="space-y-3">
            {members.map((member) => (
              <article
                key={member.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 p-4"
              >
                <div>
                  <h3 className="font-semibold">
                    {member.name}{" "}
                    {member.id === profile.id && (
                      <span className="text-xs text-club-600">(나)</span>
                    )}
                  </h3>
                  <p className="mt-1 text-sm text-slate-500">
                    {member.studentNumber} ·{" "}
                    {member.role === "ADMIN" ? "관리자" : "일반 사용자"} ·{" "}
                    {member.active ? "활성" : "비활성"}
                  </p>
                  <MemberBadges tags={member.tags} className="mt-2" />
                </div>
                <button
                  className="secondary"
                  onClick={() => edit(member)}
                  disabled={busy}
                  aria-label={`${member.name} 회원 수정`}
                >
                  수정
                </button>
              </article>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
