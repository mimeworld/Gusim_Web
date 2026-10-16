import { useState } from "react";
import { PageTitle } from "../../components";
import MembersPanel from "./MembersPanel";
import OfficersPanel from "./OfficersPanel";
import OtPanel from "./OtPanel";
export default function AdminPage() {
  const [tab, setTab] = useState("members");
  return (
    <>
      <PageTitle eyebrow="ADMINISTRATION" title="동아리 관리">
        <p>회원과 공개 임원진 소개, 신입 부원 안내를 관리합니다.</p>
      </PageTitle>
      <main className="shell page-body">
        <div
          className="mb-8 flex flex-wrap gap-3"
          role="tablist"
          aria-label="관리 항목"
        >
          {[
            ["members", "회원 관리"],
            ["officers", "임원진 편집"],
            ["ot", "OT 편집"],
          ].map(([id, label]) => (
            <button
              key={id}
              role="tab"
              id={`tab-${id}`}
              aria-controls={`panel-${id}`}
              aria-selected={tab === id}
              className={tab === id ? "button" : "secondary"}
              onClick={() => setTab(id)}
            >
              {label}
            </button>
          ))}
        </div>
        <section
          role="tabpanel"
          id={`panel-${tab}`}
          aria-labelledby={`tab-${tab}`}
        >
          {tab === "members" ? (
            <MembersPanel />
          ) : tab === "officers" ? (
            <OfficersPanel />
          ) : (
            <OtPanel />
          )}
        </section>
      </main>
    </>
  );
}
