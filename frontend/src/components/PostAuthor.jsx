import { Link } from "react-router-dom";
import Avatar from "./Avatar";
export default function PostAuthor({ post }) {
  return (
    <div>
      <div className="flex items-center gap-3">
        <Link
          to={`/members/${post.authorId}`}
          aria-label={`${post.author} 프로필로 이동`}
        >
          <Avatar src={post.authorAvatar} name={post.author} />
        </Link>
        <Link
          to={`/members/${post.authorId}`}
          className="text-sm font-semibold text-slate-800 hover:text-club-700"
        >
          {post.author}
        </Link>
      </div>
      <time dateTime={post.date} className="mt-2 block text-xs text-slate-500">
        {new Date(post.date).toLocaleString("ko-KR")}
      </time>
    </div>
  );
}
