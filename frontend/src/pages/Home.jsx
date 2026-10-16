import { ChevronRight } from "lucide-react";
import { Link } from "react-router-dom";
import { logo } from "../components";
export default function Home() {
  return (
    <>
      <section className="border-b border-slate-200 px-5 py-24 text-center sm:py-32">
        <p className="eyebrow">PHOTOGRAPHY CLUB · GACHON UNIVERSITY</p>
        <img src={logo} alt="GU.SIM" className="mx-auto my-8 w-full max-w-sm" />
        <p className="mb-8 text-lg text-slate-600">
          우리가 바라본 순간, 함께 깊어지는 시선
        </p>
        <Link className="button" to="/about">
          구도와 심도 알아보기
          <ChevronRight size={18} />
        </Link>
      </section>
      <section className="shell py-20">
        <p className="eyebrow">CAPTURE THE MOMENT</p>
        <h1 className="text-4xl font-bold leading-tight tracking-tight">
          찰나를 담고,
          <br />
          시선을 나눕니다.
        </h1>
        <p className="my-6 max-w-xl leading-8 text-slate-500">
          카메라와 사진을 좋아하는 사람들이 모여, 매달 새로운 풍경과 이야기를
          기록하는 사진 동아리입니다.
        </p>
        <div className="mt-12 grid gap-8 border-t border-slate-200 pt-8 sm:grid-cols-3">
          {[
            ["정기 출사", "함께 새로운 장소와 빛을 찾아 떠납니다."],
            [
              "사진 크리틱",
              "서로의 사진을 보고 듣고, 더 좋은 시선을 키웁니다.",
            ],
            ["연합 출사", "전국 사진 동아리들과 더 넓은 세계를 만납니다."],
          ].map(([title, text], i) => (
            <article key={title}>
              <span className="text-xs text-club-600">0{i + 1}</span>
              <h2 className="mb-3 mt-7">{title}</h2>
              <p className="text-sm leading-7 text-slate-500">{text}</p>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}
