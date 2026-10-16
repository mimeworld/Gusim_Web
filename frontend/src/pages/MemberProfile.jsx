import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../services/community";
import MemberProfileLayout from "../components/MemberProfileLayout";
export default function MemberProfile() {
  const { id } = useParams();
  const [member, setMember] = useState(null),
    [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    setMember(null);
    setError("");
    api(`/members/${id}`)
      .then((data) => {
        if (active) setMember(data);
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
  if (!member) return <p className="shell page-body">불러오는 중…</p>;
  return (
    <MemberProfileLayout
      member={member}
      photos={member.photos}
      title={`${member.name}님의 페이지`}
    />
  );
}
