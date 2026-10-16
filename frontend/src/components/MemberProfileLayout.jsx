import { PageTitle } from "./index";
import Avatar from "./Avatar";
import MemberBadges from "./MemberBadges";
import EquipmentRow from "./EquipmentRow";
import PostCard from "./PostCard";
export default function MemberProfileLayout({
  member,
  photos,
  title = "회원 페이지",
  actions,
  privateDetails,
  equipmentAction,
  error,
  photosLoading = false,
}) {
  return (
    <>
      <PageTitle eyebrow="MEMBER PROFILE" title={title} />
      <main className="shell grid gap-8 py-12 md:grid-cols-[280px_1fr]">
        <aside className="panel h-fit space-y-5 text-center">
          <Avatar src={member.avatar} name={member.name} className="size-24" />
          <h2>{member.name}</h2>
          <MemberBadges tags={member.tags} className="justify-center" />
          {privateDetails}
          <p className="whitespace-pre-wrap break-words text-sm leading-7 text-slate-500">
            {member.bio || "아직 소개가 없습니다."}
          </p>
          {actions}
          {error && (
            <p className="alert" role="alert">
              {error}
            </p>
          )}
        </aside>
        <div className="min-w-0 space-y-8">
          <section className="panel">
            <div className="section-head">
              <h2>카메라 장비</h2>
              {equipmentAction}
            </div>
            {member.equipment?.length ? (
              member.equipment.map((gear) => (
                <EquipmentRow key={gear.id} equipment={gear} />
              ))
            ) : (
              <p className="text-slate-500">등록된 장비가 없습니다.</p>
            )}
          </section>
          <section className="panel">
            <div className="section-head">
              <h2>공유한 사진</h2>
              <span className="text-sm text-slate-500">
                최근 {photos.length}개
              </span>
            </div>
            {photosLoading ? (
              <p>사진을 불러오는 중…</p>
            ) : photos.length ? (
              <div className="grid gap-4 sm:grid-cols-2">
                {photos.map((post) => (
                  <PostCard key={post.id} post={post} />
                ))}
              </div>
            ) : (
              <p className="text-slate-500">아직 공유한 사진이 없습니다.</p>
            )}
          </section>
        </div>
      </main>
    </>
  );
}
