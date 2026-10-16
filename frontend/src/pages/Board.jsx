import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../services/community";
import { BOARD_CATEGORIES } from "../services/postOptions";
import { PageTitle } from "../components";
import PostCard from "../components/PostCard";
export default function Board({ category, title }) {
  const [params, setParams] = useSearchParams();
  const boardCategory =
    category === "BOARD" ? params.get("boardCategory") || "" : "";
  const [posts, setPosts] = useState(null),
    [error, setError] = useState("");
  const query = boardCategory
    ? "&boardCategory=" + encodeURIComponent(boardCategory)
    : "";
  const path =
    category === "CRITIQUE"
      ? "/critique/write"
      : "/board/write" +
        (boardCategory
          ? "?boardCategory=" + encodeURIComponent(boardCategory)
          : "");
  useEffect(() => {
    let active = true;
    setPosts(null);
    setError("");
    api("/posts?category=" + category + query)
      .then((data) => {
        if (active) setPosts(data);
      })
      .catch((e) => {
        if (active) setError(e.message);
      });
    return () => {
      active = false;
    };
  }, [category, boardCategory]);
  return (
    <>
      <PageTitle eyebrow="MEMBERS COMMUNITY" title={title} />
      <main className="shell page-body">
        <div className="section-head">
          <h2>{title} 목록</h2>
          <Link className="button" to={path}>
            {category === "CRITIQUE" ? "크리틱 작성" : "글 작성"}
          </Link>
        </div>
        {category === "BOARD" && (
          <nav
            aria-label="게시판 카테고리"
            className="mb-7 flex flex-wrap gap-2"
          >
            {[["", "전체"], ...Object.entries(BOARD_CATEGORIES)].map(
              ([value, label]) => (
                <button
                  key={value}
                  className={boardCategory === value ? "button" : "secondary"}
                  aria-pressed={boardCategory === value}
                  onClick={() =>
                    setParams(value ? { boardCategory: value } : {})
                  }
                >
                  {label}
                </button>
              ),
            )}
          </nav>
        )}
        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : posts === null ? (
          <p>불러오는 중…</p>
        ) : posts.length ? (
          <div className="photo-grid">
            {posts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        ) : (
          <div className="empty">아직 등록된 글이 없습니다.</div>
        )}
      </main>
    </>
  );
}
