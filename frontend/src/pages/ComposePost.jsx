import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { api, uploadFiles } from "../services/community";
import { BOARD_CATEGORIES, attachmentError } from "../services/postOptions";
import { PageTitle } from "../components";
import { useApp } from "../context";
export default function ComposePost({ category, initialPost = null }) {
  const title = initialPost
    ? "글 수정"
    : {
        PHOTO: "사진 공유하기",
        CRITIQUE: "크리틱 작성",
        BOARD: "게시글 작성",
      }[category];
  const list = { PHOTO: "/share", CRITIQUE: "/critique", BOARD: "/board" }[
    category
  ];
  const { refreshPosts } = useApp();
  const nav = useNavigate();
  const [params] = useSearchParams();
  const [files, setFiles] = useState([]),
    [previews, setPreviews] = useState([]),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const [keptImages, setKeptImages] = useState(() =>
    (initialPost?.images?.length
      ? initialPost.images
      : initialPost?.image
        ? [initialPost.image]
        : []
    ).map((url, index) => ({
      url,
      size: initialPost.imageSizes?.[index] || 0,
    })),
  );
  const total =
    files.reduce((sum, file) => sum + file.size, 0) +
    keptImages.reduce((sum, image) => sum + image.size, 0);
  useEffect(() => {
    const urls = files.map((file) => URL.createObjectURL(file));
    setPreviews(urls);
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [files]);
  function choose(event) {
    const selected = Array.from(event.target.files || []);
    event.target.value = "";
    if (!selected.length) return;
    const next = category === "PHOTO" ? selected : [...files, ...selected];
    const unique = next.filter(
      (file, index) =>
        next.findIndex(
          (other) =>
            other.name === file.name &&
            other.size === file.size &&
            other.lastModified === file.lastModified,
        ) === index,
    );
    const message = attachmentError(
      category,
      [...(category === "PHOTO" ? [] : keptImages), ...unique],
      false,
    );
    if (message) {
      setError(message);
      window.dispatchEvent(new CustomEvent("api-error", { detail: message }));
      return;
    }
    setError("");
    setFiles(unique);
    if (category === "PHOTO") setKeptImages([]);
  }
  async function submit(event) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setBusy(true);
    setError("");
    try {
      const message = attachmentError(category, [...keptImages, ...files]);
      if (message) {
        window.dispatchEvent(new CustomEvent("api-error", { detail: message }));
        throw new Error(message);
      }
      const uploaded = files.length ? await uploadFiles(files, category) : [];
      const images = [...keptImages.map((image) => image.url), ...uploaded];
      const post = await api(
        initialPost ? `/posts/${initialPost.id}` : "/posts",
        {
          method: initialPost ? "PUT" : "POST",
          body: JSON.stringify({
            title: data.get("title"),
            content: data.get("content"),
            category,
            images,
            boardCategory:
              category === "BOARD" ? data.get("boardCategory") : null,
          }),
        },
      );
      if (category === "PHOTO") await refreshPosts();
      nav(`/posts/${post.id}`, { replace: !!initialPost });
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageTitle eyebrow="WRITE A STORY" title={title} />
      <main className="shell page-body">
        <form className="post-form mx-auto max-w-2xl" onSubmit={submit}>
          {category === "BOARD" && (
            <label>
              카테고리
              <select
                name="boardCategory"
                defaultValue={
                  initialPost?.boardCategory ||
                  (BOARD_CATEGORIES[params.get("boardCategory")]
                    ? params.get("boardCategory")
                    : "CHAT")
                }
                disabled={busy}
              >
                {Object.entries(BOARD_CATEGORIES).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label>
            첨부 사진 {category === "BOARD" ? "(선택)" : "(필수)"}
            <input
              type="file"
              name="photos"
              multiple={category !== "PHOTO"}
              accept="image/jpeg,image/png"
              disabled={busy}
              onChange={choose}
            />
          </label>
          <p className="text-sm leading-6 text-slate-500">
            {category === "PHOTO"
              ? "사진 1장, 최대 50MB"
              : category === "CRITIQUE"
                ? "사진 1~2장, 한 장당 최대 50MB"
                : "여러 장 첨부 가능, 한 장당 최대 50MB·합계 100MB 이하"}
            . JPEG/PNG 형식을 지원합니다.
          </p>
          <p className="text-sm font-medium text-club-700" aria-live="polite">
            선택한 사진 {files.length + keptImages.length}장 ·{" "}
            {(total / 1024 / 1024).toFixed(2)}MB /{" "}
            {category === "PHOTO" ? 50 : 100}MB
          </p>
          {keptImages.length > 0 && (
            <section
              aria-label="기존 첨부 사진"
              className="grid gap-4 sm:grid-cols-2"
            >
              {keptImages.map((image, index) => (
                <figure
                  key={image.url}
                  className="rounded-lg border border-slate-200 bg-white p-3"
                >
                  <img
                    src={image.url}
                    alt={`기존 사진 ${index + 1}`}
                    className="h-40 w-full object-contain"
                  />
                  <figcaption className="flex items-center justify-between text-sm">
                    <span>기존 사진 {index + 1}</span>
                    <button
                      type="button"
                      className="text-button"
                      disabled={busy}
                      onClick={() =>
                        setKeptImages(keptImages.filter((_, i) => i !== index))
                      }
                      aria-label={`기존 사진 ${index + 1} 제외`}
                    >
                      제외
                    </button>
                  </figcaption>
                </figure>
              ))}
            </section>
          )}
          {files.length > 0 && (
            <div className="grid gap-4 sm:grid-cols-2">
              {files.map((file, index) => (
                <figure
                  key={`${file.name}-${file.lastModified}-${index}`}
                  className="overflow-hidden rounded-lg border border-slate-200 bg-white"
                >
                  <img
                    src={previews[index]}
                    alt={`첨부 사진 ${index + 1} 미리보기`}
                    className="h-40 w-full object-contain"
                  />
                  <figcaption className="flex items-center justify-between gap-2 p-3">
                    <span className="truncate text-xs">
                      {index + 1}. {file.name}
                    </span>
                    <button
                      type="button"
                      className="text-button shrink-0"
                      aria-label={`${index + 1}번 사진 제외`}
                      disabled={busy}
                      onClick={() =>
                        setFiles(files.filter((_, i) => i !== index))
                      }
                    >
                      제외
                    </button>
                  </figcaption>
                </figure>
              ))}
            </div>
          )}
          <label>
            제목
            <input
              name="title"
              defaultValue={initialPost?.title || ""}
              required
              maxLength={200}
              disabled={busy}
            />
          </label>
          <label>
            내용
            <textarea
              name="content"
              defaultValue={initialPost?.content || ""}
              required
              maxLength={20000}
              disabled={busy}
            />
          </label>
          {error && (
            <p className="alert" role="alert">
              {error}
            </p>
          )}
          <div className="flex gap-3">
            <button className="button" disabled={busy}>
              {busy ? "저장 중…" : initialPost ? "수정 저장" : "등록"}
            </button>
            <Link
              className="secondary"
              to={initialPost ? `/posts/${initialPost.id}` : list}
            >
              목록으로
            </Link>
          </div>
        </form>
      </main>
    </>
  );
}
