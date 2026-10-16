import { BOARD_CATEGORIES } from "../services/postOptions";
import { Link } from "react-router-dom";
import { Camera } from "lucide-react";
import PostAuthor from "./PostAuthor";
export default function PostCard({ post }) {
  return (
    <article className="gallery-card transition hover:border-club-600 hover:shadow-sm">
      <Link
        to={`/posts/${post.id}`}
        className="block"
        aria-label={`${post.title} 상세 보기`}
      >
        {post.image ? (
          <img
            src={post.image}
            alt={post.title}
            loading="lazy"
            className="aspect-square w-full object-cover"
          />
        ) : (
          <div className="image-placeholder">
            <Camera />
          </div>
        )}
      </Link>
      <div>
        <PostAuthor post={post} />
        <Link
          to={`/posts/${post.id}`}
          className="mt-3 block"
          aria-label={`${post.title} 본문 보기`}
        >
          {post.category === "BOARD" && (
            <p className="text-xs font-medium text-club-700">
              {BOARD_CATEGORIES[post.boardCategory] || "잡담"}
            </p>
          )}
          <h3>{post.title}</h3>
          {post.images?.length > 1 && (
            <p className="mt-1 text-xs text-slate-500">
              사진 {post.images.length}장
            </p>
          )}
          <p className="mt-2 line-clamp-2 text-sm text-slate-500">
            {post.content}
          </p>
        </Link>
      </div>
    </article>
  );
}
