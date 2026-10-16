import EquipmentRow from "../components/EquipmentRow";
import { useState } from "react";
import { PageTitle } from "../components";
import { useApp } from "../context";
import { api } from "../services/community";
export default function Equipment() {
  const { profile, refreshProfile } = useApp();
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function add(e) {
    e.preventDefault();
    const form = e.target;
    setBusy(true);
    try {
      await api("/me/equipment", {
        method: "POST",
        body: JSON.stringify(Object.fromEntries(new FormData(form))),
      });
      await refreshProfile();
      form.reset();
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  async function remove(id) {
    try {
      await api("/me/equipment/" + id, { method: "DELETE" });
      await refreshProfile();
    } catch (e) {
      setError(e.message);
    }
  }
  return (
    <>
      <PageTitle eyebrow="MY EQUIPMENT" title="내 카메라 장비" />
      <main className="shell equipment">
        <form onSubmit={add} className="gear-form">
          <label>
            장비 분류
            <select name="type">
              {["카메라", "렌즈", "플래시", "기타장비"].map((x) => (
                <option key={x}>{x}</option>
              ))}
            </select>
          </label>
          <label>
            제조사
            <input name="maker" required maxLength={100} />
          </label>
          <label>
            모델명
            <input name="model" required maxLength={150} />
          </label>
          <label>
            장비 설명 (선택)
            <textarea name="description" maxLength={1000} />
          </label>
          <button className="button" disabled={busy}>
            장비 등록
          </button>
          {error && <p role="alert">{error}</p>}
        </form>
        <section>
          <h2>등록한 장비</h2>
          {profile.equipment.map((g) => (
            <EquipmentRow key={g.id} equipment={g}>
              <button onClick={() => remove(g.id)}>삭제</button>
            </EquipmentRow>
          ))}
        </section>
      </main>
    </>
  );
}
