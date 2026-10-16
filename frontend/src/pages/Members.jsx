import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../services/community";
import { PageTitle } from "../components";
import Avatar from "../components/Avatar";
function MemberGrid({ members }) {
  return members.length ? (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {members.map((member) => (
        <Link
          key={member.id}
          to={`/members/${member.id}`}
          className="panel flex items-center gap-4 transition hover:border-club-600"
        >
          <Avatar src={member.avatar} name={member.name} className="size-14" />
          <div className="min-w-0">
            <h3 className="font-semibold">{member.name}</h3>
            <p className="mt-1 text-sm text-club-700">
              {member.position || "일반 회원"}
            </p>
            <p className="mt-2 line-clamp-2 break-words text-xs text-slate-500">
              {member.bio}
            </p>
          </div>
        </Link>
      ))}
    </div>
  ) : (
    <p className="empty">등록된 부원이 없습니다.</p>
  );
}
export default function Members() {
  const [data, setData] = useState(null),
    [error, setError] = useState(""),
    [tab, setTab] = useState("all");
  useEffect(() => {
    api("/members")
      .then(setData)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <>
      <PageTitle eyebrow="OUR MEMBERS" title="부원 목록">
        <p>함께 사진을 기록하는 구도와 심도 부원들입니다.</p>
      </PageTitle>
      <main className="shell page-body">
        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : !data ? (
          <p>불러오는 중…</p>
        ) : (
          <>
            <div
              className="mb-8 flex flex-wrap gap-3"
              role="tablist"
              aria-label="부원 구분"
            >
              {[
                [
                  "all",
                  `전체 부원 (${data.officers.length + data.members.length})`,
                ],
                ["officers", `임원진 (${data.officers.length})`],
                ["members", `일반 부원 (${data.members.length})`],
              ].map(([id, label]) => (
                <button
                  key={id}
                  id={`members-tab-${id}`}
                  role="tab"
                  aria-selected={tab === id}
                  aria-controls="members-panel"
                  className={tab === id ? "button" : "secondary"}
                  onClick={() => setTab(id)}
                >
                  {label}
                </button>
              ))}
            </div>
            <div
              role="tabpanel"
              id="members-panel"
              aria-labelledby={`members-tab-${tab}`}
              className="space-y-12"
            >
              {(tab === "all" || tab === "officers") && (
                <section>
                  <h2 className="mb-5">임원진</h2>
                  <MemberGrid members={data.officers} />
                </section>
              )}
              {(tab === "all" || tab === "members") && (
                <section>
                  <h2 className="mb-5">일반 부원</h2>
                  <MemberGrid members={data.members} />
                </section>
              )}
            </div>
          </>
        )}
      </main>
    </>
  );
}
