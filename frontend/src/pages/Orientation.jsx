import { useEffect, useState } from "react";
import { api } from "../services/community";
import { PageTitle } from "../components";
export default function Orientation() {
  const [page, setPage] = useState(null),
    [error, setError] = useState("");
  useEffect(() => {
    api("/pages/ot")
      .then(setPage)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <>
      <PageTitle eyebrow="ORIENTATION" title={page?.title || "OT 안내"} />
      <main className="shell page-body">
        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : page ? (
          <article className="panel whitespace-pre-wrap break-words leading-8">
            {page.content}
          </article>
        ) : (
          <p>불러오는 중…</p>
        )}
      </main>
    </>
  );
}
