import { useEffect, useState } from "react";
import { api } from "../services/community";
import { PageTitle } from "../components";
import OfficerCard from "../components/OfficerCard";
export default function Officers() {
  const [members, setMembers] = useState(null),
    [error, setError] = useState("");
  useEffect(() => {
    api("/officers")
      .then(setMembers)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <>
      <PageTitle eyebrow="OUR TEAM" title="동아리 임원진">
        <p>함께하는 순간을 준비하는 사람들</p>
      </PageTitle>
      <main className="shell page-body">
        {error ? (
          <p role="alert" className="alert">
            {error}
          </p>
        ) : members === null ? (
          <p>불러오는 중…</p>
        ) : members.length ? (
          <div className="grid gap-5 md:grid-cols-2">
            {members.map((officer) => (
              <OfficerCard key={officer.id} officer={officer} />
            ))}
          </div>
        ) : (
          <div className="empty">
            <h2>임원진 소개를 준비하고 있어요.</h2>
          </div>
        )}
      </main>
    </>
  );
}
