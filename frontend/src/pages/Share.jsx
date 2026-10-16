import { Link } from "react-router-dom";
import { PageTitle } from "../components";
import { useApp } from "../context";
import PostCard from "../components/PostCard";
export default function Share() {
  const { posts } = useApp();
  return (
    <>
      <PageTitle eyebrow="PHOTO COMMUNITY" title="사진 공유">
        <p>구심인들이 포착한 순간을 만나보세요.</p>
      </PageTitle>
      <main className="shell page-body">
        <div className="section-head">
          <h2>모든 사진 · {posts.length}개</h2>
          <Link className="button" to="/share/upload">
            사진 공유하기
          </Link>
        </div>
        {posts.length ? (
          <div className="photo-grid">
            {posts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        ) : (
          <div className="empty">아직 공유된 사진이 없습니다.</div>
        )}
      </main>
    </>
  );
}
