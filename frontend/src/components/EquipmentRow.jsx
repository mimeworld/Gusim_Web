export default function EquipmentRow({ equipment, children }) {
  return (
    <article className="grid grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] items-start gap-4 border-b border-slate-200 py-4">
      <div className="min-w-0">
        <span className="text-xs text-slate-500">{equipment.type}</span>
        <h3 className="mt-1 break-words font-medium">
          {equipment.maker} {equipment.model}
        </h3>
      </div>
      <p className="min-w-0 whitespace-pre-wrap break-words text-sm leading-7 text-slate-500">
        {equipment.description || "설명 없음"}
      </p>
      {children && <div className="text-sm text-red-700">{children}</div>}
    </article>
  );
}
