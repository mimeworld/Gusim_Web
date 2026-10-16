import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../services/community";
import { useApp } from "../context";
import ComposePost from "./ComposePost";
export default function EditPost() {
  const { id } = useParams();
  const { profile } = useApp();
  const [post, setPost] = useState(null),
    [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    setPost(null);
    setError("");
    api(`/posts/${id}`)
      .then((data) => {
        if (active) setPost(data);
      })
      .catch((e) => {
        if (active) setError(e.message);
      });
    return () => {
      active = false;
    };
  }, [id]);
  if (error)
    return (
      <main className="shell page-body">
        <p className="alert" role="alert">
          {error}
        </p>
      </main>
    );
  if (!post) return <p className="shell page-body">불러오는 중…</p>;
  if (post.authorId !== profile.id)
    return (
      <main className="shell page-body">
        <p className="alert" role="alert">
          본인이 작성한 글만 수정할 수 있습니다.
        </p>
      </main>
    );
  return (
    <ComposePost key={post.id} category={post.category} initialPost={post} />
  );
}
