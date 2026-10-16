import { BOARD_CATEGORIES } from "../services/postOptions";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams, useLocation } from "react-router-dom";
import { api } from "../services/community";
import { PageTitle } from "../components";
import PostAuthor from "../components/PostAuthor";
import { useApp } from "../context";
export default function PostDetail() {
  const { id } = useParams();
  const location = useLocation(),
    nav = useNavigate();
  const { profile, refreshPosts } = useApp();
  const [post, setPost] = useState(null),
    [error, setError] = useState(""),
    [confirm, setConfirm] = useState(false),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    let active = true;
    setPost(null);
    setError("");
    api(`/posts/${id}`)
      .then((data) => {
        if (active) setPost(data);
      })
      .catch((e) => {
        if (!active) return;
        setError(e.message);
        if (e.status === 401)
          nav("/login", { state: { from: location.pathname }, replace: true });
      });
    return () => {
      active = false;
    };
  }, [id]);
  const list =
    { PHOTO: "/share", CRITIQUE: "/critique", BOARD: "/board" }[
      post?.category
    ] || "/share";
  async function remove() {
    setBusy(true);
    try {
      await api(`/posts/${id}`, { method: "DELETE" });
      if (post.category === "PHOTO") await refreshPosts();
      nav(list, { replace: true });
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageTitle eyebrow="OUR STORIES" title={post?.title || "게시글 상세"} />
      <main className="shell page-body">
        {error && (
          <p className="alert mb-5" role="alert">
            {error}
          </p>
        )}
        {post ? (
          <article className="space-y-7">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <PostAuthor post={post} />
              <Link className="secondary" to={list}>
                목록으로
              </Link>
            </div>
            {post.category === "BOARD" && (
              <span className="inline-block rounded-full bg-club-50 px-3 py-1 text-sm text-club-700">
                {BOARD_CATEGORIES[post.boardCategory] || "잡담"}
              </span>
            )}
            {(post.images?.length
              ? post.images
              : post.image
                ? [post.image]
                : []
            ).map((image, index, images) => (
              <figure key={image} className="space-y-3">
                <a
                  href={image}
                  target="_blank"
                  rel="noreferrer"
                  aria-label={`${index + 1}번 전체 사진 새 창으로 보기`}
                >
                  <img
                    src={image}
                    alt={`${post.title} · 사진 ${index + 1}`}
                    className="h-auto w-full rounded-xl border border-slate-100"
                  />
                </a>
                {images.length > 1 && (
                  <figcaption className="text-center text-sm text-slate-500">
                    사진 {index + 1} / {images.length}
                  </figcaption>
                )}
              </figure>
            ))}
            <p className="whitespace-pre-wrap break-words leading-8">
              {post.content}
            </p>
            {profile?.id === post.authorId && (
              <div className="space-y-4 border-t border-slate-200 pt-6">
                <Link className="secondary" to={`/posts/${post.id}/edit`}>
                  내 글 수정
                </Link>
                {confirm ? (
                  <div className="panel space-y-4" role="alert">
                    <p>이 글을 삭제할까요? 삭제한 글은 복구할 수 없습니다.</p>
                    <button className="button" disabled={busy} onClick={remove}>
                      삭제 확인
                    </button>
                    <button
                      className="text-button"
                      disabled={busy}
                      onClick={() => setConfirm(false)}
                    >
                      취소
                    </button>
                  </div>
                ) : (
                  <button
                    className="secondary text-red-700"
                    onClick={() => setConfirm(true)}
                  >
                    내 글 삭제
                  </button>
                )}
              </div>
            )}
          </article>
        ) : (
          !error && <p>불러오는 중…</p>
        )}
      </main>
    </>
  );
}
