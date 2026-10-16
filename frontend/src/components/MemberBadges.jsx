export default function MemberBadges({ tags = [], className = "" }) {
  if (!tags.length) return null;
  return (
    <div
      aria-label="회원 태그"
      className={`flex flex-wrap gap-1.5 ${className}`}
    >
      {tags.map((tag, index) => (
        <span
          key={`${tag}-${index}`}
          className="rounded-full bg-club-50 px-2.5 py-1 text-xs font-medium text-club-700"
        >
          {tag}
        </span>
      ))}
    </div>
  );
}
