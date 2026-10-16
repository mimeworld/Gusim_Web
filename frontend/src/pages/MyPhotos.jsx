import { PageTitle } from "../components";
import { useApp } from "../context";
import PostCard from "../components/PostCard";
export default function MyPhotos() {
  const { posts, profile } = useApp();
  const mine = posts.filter((post) => post.authorId === profile.id);
  return (
    <>
      <PageTitle eyebrow="MY ARCHIVE" title="내가 올린 사진" />
      <main className="shell page-body">
        {mine.length ? (
          <div className="photo-grid">
            {mine.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        ) : (
          <div className="empty">아직 올린 사진이 없습니다.</div>
        )}
      </main>
    </>
  );
}
