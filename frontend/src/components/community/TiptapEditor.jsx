import { useEffect, useCallback, useRef } from "react";
import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Underline from "@tiptap/extension-underline";
import Image from "@tiptap/extension-image";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/Tiptap.css";

// 에디터 HTML에서 img 태그 개수 세기 (첨부 이미지 개수 제한 검사에 사용)
const countImages = (html) => (html.match(/<img/g) || []).length;

// 바이트 단위를 사람이 읽기 쉬운 KB/MB 문자열로 변환 (용량 초과 안내 메시지용)
const formatSize = (bytes) => {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)}KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
};

// ---- 에디터 상단 툴바 ----
// 본문/제목 스타일 선택, 굵게/밑줄 토글, 이미지 첨부 버튼을 제공
const EditorToolbar = ({ editor, onImageClick, imageCount, maxImageCount }) => {
  if (!editor) return null; // 에디터 인스턴스가 아직 준비되지 않았으면 렌더링하지 않음
  const isLimited = Number.isFinite(maxImageCount);
  const isFull = isLimited && imageCount >= maxImageCount; // 이미지 개수 제한에 도달했는지 여부

  return (
    <div className="editor-toolbar">
      {/* 문단/제목1~3 스타일 선택 */}
      <select
        onChange={(e) => {
          const level = Number(e.target.value);
          if (level === 0) editor.chain().focus().setParagraph().run();
          else editor.chain().focus().toggleHeading({ level }).run();
        }}
        defaultValue={0}
      >
        <option value={0}>본문</option>
        <option value={1}>제목 1</option>
        <option value={2}>제목 2</option>
        <option value={3}>제목 3</option>
      </select>
      {/* 굵게 토글 (현재 적용 중이면 is-active 클래스로 하이라이트) */}
      <button
        type="button"
        onClick={() => editor.chain().focus().toggleBold().run()}
        className={editor.isActive("bold") ? "is-active" : ""}
      >
        굵게
      </button>
      {/* 밑줄 토글 */}
      <button
        type="button"
        onClick={() => editor.chain().focus().toggleUnderline().run()}
        className={editor.isActive("underline") ? "is-active" : ""}
      >
        밑줄
      </button>
      {/* 이미지 첨부 버튼: 개수 제한에 도달하면 비활성화 + 안내 툴팁 표시 */}
      <button
        type="button"
        onClick={onImageClick}
        disabled={isFull}
        title={
          isFull ? `이미지는 최대 ${maxImageCount}장까지 첨부 가능합니다.` : ""
        }
      >
        {isLimited ? `이미지 (${imageCount}/${maxImageCount})` : "이미지"}
      </button>
    </div>
  );
};

/**
 * 재사용 가능한 Tiptap 리치 텍스트 에디터
 * 게시글 작성(Insert)/수정(Update) 화면에서 공통으로 사용
 *
 * props:
 * - value: string (에디터에 표시할 HTML, 수정 페이지에서 초기값 전달용)
 * - onChange: (html: string) => void  (내용이 바뀔 때마다 부모에 HTML 전달)
 * - onImageCountChange: (count: number) => void  (선택, 부모가 이미지 개수를 알아야 할 때)
 * - onUploadingChange: (uploading: boolean) => void  (선택, 업로드 중 제출 버튼 disable 등에 사용)
 * - maxImageCount: number (기본 4)
 * - maxImageSize: number (bytes, 기본 5MB)
 * - allowedImageTypes: string[] (기본 jpg/png/gif/webp)
 */
const TiptapEditor = ({
  value = "",
  onChange,
  onImageCountChange,
  onUploadingChange,
  maxImageCount = 4,
  maxImageSize = 5 * 1024 * 1024,
  allowedImageTypes = ["image/jpeg", "image/png", "image/gif", "image/webp"],
}) => {
  // 에디터 내부 입력으로 인한 value 변경인지 구분하는 플래그
  // (외부에서 value prop이 바뀌었을 때만 강제로 setContent 하기 위한 무한 루프 방지용)
  const isInternalUpdate = useRef(false);

  // Tiptap 에디터 인스턴스 생성: 기본 확장(StarterKit) + 밑줄 + 이미지 확장 사용
  const editor = useEditor({
    extensions: [StarterKit, Underline, Image],
    content: value,
    onUpdate: ({ editor }) => {
      isInternalUpdate.current = true; // 이번 변경은 에디터 내부에서 발생한 것임을 표시
      const html = editor.getHTML();
      onChange?.(html);
      onImageCountChange?.(countImages(html));
    },
  });

  // 수정 페이지처럼 부모가 뒤늦게 value(초기 게시글 내용)를 채워주는 경우 에디터 내용과 동기화
  useEffect(() => {
    if (!editor) return;
    if (isInternalUpdate.current) {
      // 방금 전 변경이 에디터 내부 타이핑으로 인한 것이면 다시 setContent 하지 않고 플래그만 리셋
      isInternalUpdate.current = false;
      return;
    }
    const current = editor.getHTML();
    if (value !== current) {
      // 외부에서 value가 바뀐 경우(예: 수정 페이지 데이터 로딩 완료)에만 에디터 내용을 갱신
      editor.commands.setContent(value || "", false);
      onImageCountChange?.(countImages(value || ""));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, editor]);

  // 이미지 첨부 버튼 클릭 시 실행되는 업로드 플로우
  const handleImageUpload = useCallback(() => {
    if (!editor) return;

    // 1. 개수 제한 체크 (파일 선택창을 열기 전에 먼저 차단)
    const currentCount = countImages(editor.getHTML());
    if (currentCount >= maxImageCount) {
      alert(`이미지는 게시글당 최대 ${maxImageCount}장까지 첨부 가능합니다.`);
      return;
    }

    // 숨겨진 파일 input을 동적으로 생성해 파일 선택창을 오픈
    const input = document.createElement("input");
    input.setAttribute("type", "file");
    input.setAttribute("accept", "image/*");
    input.click();
    input.onchange = async () => {
      const file = input.files[0];
      if (!file) return;

      // 2. 파일 선택 직후 개수 재확인 (연속 클릭 등으로 제한을 우회하는 것 방지)
      const countBeforeUpload = countImages(editor.getHTML());
      if (countBeforeUpload >= maxImageCount) {
        alert(`이미지는 게시글당 최대 ${maxImageCount}장까지 첨부 가능합니다.`);
        return;
      }

      // 3. 파일 형식 체크 (허용된 이미지 타입만 업로드 가능)
      if (!allowedImageTypes.includes(file.type)) {
        alert("jpg, png, gif, webp 형식만 업로드 가능합니다.");
        return;
      }

      // 4. 파일 용량 체크
      if (file.size > maxImageSize) {
        alert(
          `이미지 용량은 ${formatSize(maxImageSize)} 이하만 첨부 가능합니다. (선택한 파일: ${formatSize(file.size)})`,
        );
        return;
      }

      // 검증을 통과하면 서버로 이미지 업로드 요청
      const uploadData = new FormData();
      uploadData.append("file", file);

      try {
        onUploadingChange?.(true); // 업로드 시작을 부모에 알림 (제출 버튼 비활성화 등에 사용)
        const res = await jwtAxios.post(
          `${API_SERVER_URL}/api/upload/image`,
          uploadData,
        );
        // 업로드된 이미지 URL을 현재 커서 위치에 삽입
        editor
          .chain()
          .focus()
          .setImage({ src: `${API_SERVER_URL}${res.data.url}` })
          .run();

        // onUpdate 콜백에서도 갱신되지만, 즉시 반영을 보장하기 위해 한 번 더 호출
        const html = editor.getHTML();
        onChange?.(html);
        onImageCountChange?.(countImages(html));
      } catch (err) {
        if (err?.response?.status === 413) {
          alert("이미지 용량이 서버 제한을 초과했습니다.");
        } else {
          alert("이미지 업로드 실패");
        }
      } finally {
        onUploadingChange?.(false); // 업로드 종료를 부모에 알림
      }
    };
  }, [
    editor,
    maxImageCount,
    maxImageSize,
    allowedImageTypes,
    onChange,
    onImageCountChange,
    onUploadingChange,
  ]);

  // 현재 에디터 본문에 포함된 이미지 개수 (툴바에 "이미지 (n/max)" 형태로 표시)
  const imageCount = editor ? countImages(editor.getHTML()) : 0;

  return (
    <div className="tiptap-wrapper">
      <EditorToolbar
        editor={editor}
        onImageClick={handleImageUpload}
        imageCount={imageCount}
        maxImageCount={maxImageCount}
      />
      <EditorContent editor={editor} className="tiptap-content" />
      {/* 이미지 첨부 제한 안내 문구 */}
      <p className="image-limit-hint">
        이미지 최대 {maxImageCount}장, 장당 {formatSize(maxImageSize)} 이하
        (jpg, png, gif, webp)
      </p>
    </div>
  );
};

export default TiptapEditor;
