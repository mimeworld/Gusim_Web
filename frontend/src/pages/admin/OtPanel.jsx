import { useEffect, useState } from "react";
import { api } from "../../services/community";
export default function OtPanel() {
  const [form, setForm] = useState(null),
    [error, setError] = useState(""),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    api("/pages/ot")
      .then(setForm)
      .catch((e) => setError(e.message));
  }, []);
  async function save(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");
    try {
      setForm(
        await api("/admin/pages/ot", {
          method: "PUT",
          body: JSON.stringify({ title: form.title, content: form.content }),
        }),
      );
      setMessage("OT 안내를 저장했습니다.");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="max-w-3xl space-y-5">
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
      {form ? (
        <form className="panel space-y-5" onSubmit={save}>
          <label>
            OT 제목
            <input
              required
              maxLength={200}
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
          </label>
          <label>
            안내 내용
            <textarea
              className="min-h-80"
              maxLength={20000}
              value={form.content}
              onChange={(e) => setForm({ ...form, content: e.target.value })}
            />
          </label>
          <p className="text-sm text-slate-500">
            줄바꿈이 유지되는 일반 텍스트입니다. 저장하면 모든 부원에게
            적용됩니다.
          </p>
          <button className="button" disabled={busy}>
            OT 저장
          </button>
        </form>
      ) : (
        <p>불러오는 중…</p>
      )}
    </div>
  );
}
