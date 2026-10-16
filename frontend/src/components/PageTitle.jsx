export default function PageTitle({ eyebrow, title, children }) {
  return (
    <section className="border-b border-slate-200 py-12 sm:py-16">
      <div className="shell">
        <p className="eyebrow">{eyebrow}</p>
        <h1 className="text-3xl font-bold tracking-tight sm:text-5xl">
          {title}
        </h1>
        <div className="mt-5 max-w-2xl leading-7 text-slate-500">
          {children}
        </div>
      </div>
    </section>
  );
}
