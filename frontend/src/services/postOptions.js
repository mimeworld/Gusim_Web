export const BOARD_CATEGORIES = {
  CHAT: "잡담",
  QUESTION: "질문",
  INFO: "정보",
};
export function attachmentError(category, files, required = true) {
  if (required && category !== "BOARD" && !files.length)
    return "사진을 첨부해주세요.";
  if (category === "PHOTO" && files.length > 1)
    return "사진 공유에는 사진 한 장을 첨부해주세요.";
  if (category === "CRITIQUE" && files.length > 2)
    return "크리틱에는 사진을 최대 2장까지 첨부할 수 있습니다.";
  if (files.some((file) => file.size > 50 * 1024 * 1024))
    return "사진 한 장은 50MB 이하여야 합니다.";
  if (files.reduce((total, file) => total + file.size, 0) > 100 * 1024 * 1024)
    return "첨부 사진의 합계는 100MB 이하여야 합니다.";
  return "";
}
