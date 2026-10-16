import { UserRound } from "lucide-react";
export default function Avatar({ src, name, className = "size-10" }) {
  return (
    <span
      className={`inline-grid shrink-0 place-items-center overflow-hidden rounded-full bg-club-50 text-club-700 ${className}`}
    >
      {src ? (
        <img
          src={src}
          alt={`${name} 프로필`}
          className="size-full object-cover"
        />
      ) : (
        <UserRound aria-label={`${name} 기본 프로필`} className="size-1/2" />
      )}
    </span>
  );
}
