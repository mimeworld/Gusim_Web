import { UserRound } from "lucide-react";
export default function OfficerCard({ officer }) {
  return (
    <article className="flex items-start gap-5 rounded-xl border border-slate-200 p-5">
      <div className="grid h-32 w-24 shrink-0 place-items-center overflow-hidden rounded-lg bg-club-50 text-club-600">
        {officer.photo ? (
          <img
            className="size-full object-cover"
            src={officer.photo}
            alt={`${officer.name} 소개 사진`}
          />
        ) : (
          <UserRound size={36} />
        )}
      </div>
      <div className="min-w-0">
        <p className="mb-2 text-sm font-medium text-club-700">
          {officer.position}
        </p>
        <h2>{officer.name}</h2>
        <p className="mt-3 whitespace-pre-wrap break-words text-sm leading-7 text-slate-500">
          {officer.bio}
        </p>
      </div>
    </article>
  );
}
