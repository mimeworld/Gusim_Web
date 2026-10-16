import { useEffect, useRef, useState } from "react";
export default function MessageDialog() {
  const [message, setMessage] = useState("");
  const ref = useRef(null);
  useEffect(() => {
    const show = (event) => setMessage(event.detail);
    window.addEventListener("api-error", show);
    window.addEventListener("api-notice", show);
    return () => {
      window.removeEventListener("api-error", show);
      window.removeEventListener("api-notice", show);
    };
  }, []);
  useEffect(() => {
    if (message && !ref.current.open) ref.current.showModal();
  }, [message]);
  return (
    <dialog
      ref={ref}
      aria-labelledby="message-title"
      onClose={() => setMessage("")}
      className="m-auto w-[calc(100%_-_40px)] max-w-md rounded-xl border border-slate-200 bg-white p-7 shadow-xl backdrop:bg-black/35"
    >
      <h2 id="message-title">알림</h2>
      <p className="my-5 whitespace-pre-wrap leading-7">{message}</p>
      <button className="button" autoFocus onClick={() => ref.current.close()}>
        확인
      </button>
    </dialog>
  );
}
