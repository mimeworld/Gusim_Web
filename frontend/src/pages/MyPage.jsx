import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, upload } from "../services/community";
import { useApp } from "../context";
import MemberProfileLayout from "../components/MemberProfileLayout";
export default function MyPage() {
  const { profile, logout, refreshProfile } = useApp();
  const [photos, setPhotos] = useState([]),
    [photosLoading, setPhotosLoading] = useState(true),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    let active = true;
    setPhotosLoading(true);
    api(`/members/${profile.id}`)
      .then((data) => {
        if (active) setPhotos(data.photos);
      })
      .catch((e) => {
        if (active) setError(e.message);
      })
      .finally(() => {
        if (active) setPhotosLoading(false);
      });
    return () => {
      active = false;
    };
  }, [profile.id, profile.name, profile.avatar]);
  async function avatar(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setBusy(true);
    try {
      const avatar = await upload(file);
      await api("/me", {
        method: "PUT",
        body: JSON.stringify({ name: profile.name, bio: profile.bio, avatar }),
      });
      await refreshProfile();
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <MemberProfileLayout
      title="마이페이지"
      member={profile}
      photos={photos}
      photosLoading={photosLoading}
      error={error}
      privateDetails={
        <p className="text-sm text-slate-500">
          {profile.role === "ADMIN" ? "관리자" : "일반 부원"} ·{" "}
          {profile.studentNumber}
        </p>
      }
      equipmentAction={
        <Link className="text-button" to="/equipment">
          장비 관리 →
        </Link>
      }
      actions={
        <div className="space-y-4">
          <Link className="secondary" to="/mypage/password">
            비밀번호 변경
          </Link>
          <label>
            프로필 사진 변경
            <input
              type="file"
              accept="image/jpeg,image/png"
              onChange={avatar}
              disabled={busy}
            />
          </label>
          <button
            className="secondary"
            onClick={() => logout().catch((e) => setError(e.message))}
          >
            로그아웃
          </button>
        </div>
      }
    />
  );
}
