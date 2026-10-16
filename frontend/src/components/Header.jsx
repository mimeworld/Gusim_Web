import { LogIn, Menu, X, UserRound } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import { useState } from "react";
import { useApp } from "../context";
import logo from "../assets/logo.png";
export default function Header() {
  const { profile } = useApp();
  const [open, setOpen] = useState(false);
  const links = [
    ["/about", "동아리 소개"],
    ["/officers", "동아리 임원진"],
    ["/share", "사진들"],
    ...(profile
      ? [
          ["/critique", "크리틱"],
          ["/ot", "OT"],
          ["/board", "게시판"],
          ["/members", "부원 목록"],
        ]
      : []),
    ...(profile?.role === "ADMIN" ? [["/admin", "관리자"]] : []),
  ];
  const menuLink = ({ isActive }) =>
    `border-b-2 px-1 py-3 text-sm font-medium transition hover:border-club-600 hover:text-club-700 focus-visible:border-club-600 ${isActive ? "border-club-600 text-club-700" : "border-transparent text-slate-700"}`;
  return (
    <header className="relative z-20 border-b border-slate-200 bg-white">
      <nav
        className="shell flex min-h-20 items-center gap-5"
        aria-label="주 메뉴"
      >
        <Link
          to="/"
          aria-label="구도와 심도 홈"
          className="shrink-0"
          onClick={() => setOpen(false)}
        >
          <img
            src={logo}
            alt="구도와 심도"
            className="h-9 w-20 object-contain"
          />
        </Link>
        <div className="hidden items-center gap-3 xl:gap-4 lg:flex">
          {links.map(([path, label]) => (
            <NavLink key={path} to={path} className={menuLink}>
              {label}
            </NavLink>
          ))}
        </div>
        <div className="ml-auto flex shrink-0 items-center gap-3">
          {profile ? (
            <Link
              to="/mypage"
              aria-label="마이페이지"
              className="grid size-10 place-items-center overflow-hidden rounded-full bg-club-50 text-club-700"
              onClick={() => setOpen(false)}
            >
              {profile.avatar ? (
                <img
                  src={profile.avatar}
                  alt="내 프로필"
                  className="size-full object-cover"
                />
              ) : (
                <UserRound size={22} />
              )}
            </Link>
          ) : (
            <Link to="/login" className="button" onClick={() => setOpen(false)}>
              <LogIn size={16} />
              로그인
            </Link>
          )}
          <button
            className="rounded p-2 lg:hidden"
            aria-expanded={open}
            aria-controls="mobile-menu"
            aria-label="메뉴 열기/닫기"
            onClick={() => setOpen(!open)}
          >
            {open ? <X /> : <Menu />}
          </button>
        </div>
      </nav>
      {open && (
        <nav
          id="mobile-menu"
          aria-label="모바일 메뉴"
          className="shell flex flex-col border-t border-slate-100 pb-4 lg:hidden"
        >
          {links.map(([path, label]) => (
            <NavLink
              key={path}
              to={path}
              className={menuLink}
              onClick={() => setOpen(false)}
            >
              {label}
            </NavLink>
          ))}
        </nav>
      )}
    </header>
  );
}
