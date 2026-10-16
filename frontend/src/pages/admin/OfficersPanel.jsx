import { useEffect, useState } from "react";
import { api, upload } from "../../services/community";
import { useApp } from "../../context";
import OfficerCard from "../../components/OfficerCard";
const blank = { memberId: "", position: "", bio: "", photo: "", sortOrder: 0 };
export default function OfficersPanel() {
  const { refreshProfile } = useApp();
  const [officers, setOfficers] = useState([]),
    [members, setMembers] = useState([]),
    [form, setForm] = useState(blank),
    [editing, setEditing] = useState(null),
    [error, setError] = useState(""),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false),
    [file, setFile] = useState(null),
    [deleteId, setDeleteId] = useState(null);
  const refresh = async () => {
    const [officers, members] = await Promise.all([
      api("/officers"),
      api("/admin/members"),
    ]);
    setOfficers(officers);
    setMembers(members);
  };
  useEffect(() => {
    refresh().catch((e) => setError(e.message));
  }, []);
  function reset() {
    setForm(blank);
    setEditing(null);
    setFile(null);
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
      const photo = file ? await upload(file) : form.photo;
      await api(editing ? `/admin/officers/${editing}` : "/admin/officers", {
        method: editing ? "PUT" : "POST",
        body: JSON.stringify({
          memberId: Number(form.memberId),
          position: form.position,
          bio: form.bio,
          photo,
          sortOrder: Number(form.sortOrder),
        }),
      });
      await refresh();
      await refreshProfile();
      reset();
      event.target?.reset?.();
      setMessage(
        "임원 지정을 저장했습니다. 해당 회원에게 관리자 권한과 직책 태그가 적용됩니다.",
      );
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  async function remove() {
    setBusy(true);
    try {
      await api(`/admin/officers/${deleteId}`, { method: "DELETE" });
      if (editing === deleteId) reset();
      await refresh();
      await refreshProfile();
      setDeleteId(null);
      setMessage(
        "임원 지정과 태그를 해제했습니다. 관리자 권한은 회원 관리에서 별도로 변경할 수 있습니다.",
      );
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="space-y-6">
      {error && (
        <p className="alert" role="alert">
          {error}
        </p>
      )}
      {message && (
        <p className="notice" role="status">
          {message}
        </p>
      )}
      <div className="grid items-start gap-8 lg:grid-cols-2">
        <form className="panel space-y-4" onSubmit={save}>
          <h2>{editing ? "임원진 수정" : "임원진 추가"}</h2>
          <label>
            기존 회원 선택
            <select {...field("memberId")} required disabled={busy}>
              <option value="">임원으로 지정할 회원을 선택해주세요</option>
              {members
                .filter(
                  (member) =>
                    member.active &&
                    !officers.some(
                      (officer) =>
                        officer.memberId === member.id &&
                        officer.id !== editing,
                    ),
                )
                .map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.name} · {member.studentNumber}
                  </option>
                ))}
            </select>
          </label>
          <p className="notice">
            임원 지정 시 관리자 권한이 부여되고, 회원에게 지정한 직책 이름만
            태그로 표시됩니다.
          </p>
          <label>
            직책
            <input {...field("position")} required maxLength={50} />
          </label>
          <label>
            소개
            <textarea {...field("bio")} maxLength={500} />
          </label>
          <label>
            카드 왼쪽 소개 사진
            <input
              key={editing ?? "new"}
              type="file"
              accept="image/jpeg,image/png"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
            />
          </label>
          <p className="text-xs text-slate-500">
            개인 프로필과 별도로 저장됩니다. JPEG/PNG, 최대 50MB.
          </p>
          {form.photo && (
            <div className="flex items-center gap-3">
              <img
                src={form.photo}
                alt="현재 소개 사진"
                className="h-20 w-16 rounded object-cover"
              />
              <button
                type="button"
                className="text-button"
                onClick={() => {
                  setForm({ ...form, photo: "" });
                  setFile(null);
                }}
              >
                기존 사진 제거
              </button>
            </div>
          )}
          <label>
            표시 순서 (작은 숫자 먼저)
            <input
              type="number"
              min="0"
              max="999"
              {...field("sortOrder")}
              required
            />
          </label>
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
        <div className="space-y-5">
          {officers.map((officer) => (
            <div key={officer.id}>
              <OfficerCard officer={officer} />
              <div className="mt-2 flex justify-end gap-2">
                <button
                  className="text-button"
                  disabled={busy}
                  onClick={() => {
                    setEditing(officer.id);
                    setForm({ ...officer, memberId: officer.memberId || "" });
                    setFile(null);
                  }}
                  aria-label={`${officer.name} 임원진 수정`}
                >
                  수정
                </button>
                <button
                  className="text-button text-red-700"
                  disabled={busy}
                  onClick={() => setDeleteId(officer.id)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
          {!officers.length && (
            <p className="empty">등록된 임원진이 없습니다.</p>
          )}
          {deleteId && (
            <div className="panel space-y-4" role="alert">
              <p>
                선택한 임원 지정과 소개를 삭제할까요? 직책 태그는 해제되며
                관리자 권한은 유지됩니다.
              </p>
              <button className="button" onClick={remove} disabled={busy}>
                삭제 확인
              </button>
              <button className="text-button" onClick={() => setDeleteId(null)}>
                취소
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
