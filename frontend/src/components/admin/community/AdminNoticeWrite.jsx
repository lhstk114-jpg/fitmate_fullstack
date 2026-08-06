import { useEffect, useMemo, useState } from "react";
import { API_SERVER_URL } from "../../../apis/commonApi";
import jwtAxios from "../../../apis/util/jwtUtil";
import TiptapEditor from "../../community/TiptapEditor";

/**
 * 관리자용 공지사항 작성 모달
 * - 부모(AdminCommunity)에서 넘겨준 tabId(공지사항 전용 탭)에 글을 작성
 * - 로그인한 관리자 정보를 자동으로 불러와 작성자로 세팅
 * - tabId: 작성 대상 탭 id (고정, adminOnly 탭)
 * - tabs: 전체 탭 목록 (탭 이름 표시용)
 * - categories: 전체 카테고리 목록 (해당 탭의 카테고리만 필터링해 사용)
 * - onClose: 모달 닫기 콜백
 * - onSuccess: 작성 성공 후 호출되는 콜백 (부모에서 모달 닫기 + 목록 갱신 처리)
 */
const AdminNoticeWrite = ({ tabId, tabs, categories, onClose, onSuccess }) => {
  // 작성 폼 데이터 (userName/userEmail은 마운트 시 자동으로 채워짐)
  const [formData, setFormData] = useState({
    tabId: String(tabId),
    categoryId: "",
    title: "",
    content: "",
    userName: "",
    userEmail: "",
  });

  // tabId에 해당하는 탭 이름을 조회 (모달 헤더에 "공지사항 작성 > 탭이름" 형태로 표시)
  const tabName = useMemo(
    () => tabs.find((t) => String(t.id) === String(tabId))?.tabName || "",
    [tabs, tabId],
  );

  // 전체 카테고리 중 현재 tabId에 속한 카테고리만 필터링 (카테고리 select box에 사용)
  const filteredCategories = useMemo(
    () => categories.filter((cat) => String(cat.tabId) === String(tabId)),
    [categories, tabId],
  );

  // 모달이 열릴 때(마운트 시) 현재 로그인한 관리자 정보를 조회해 작성자명/이메일 자동 세팅
  useEffect(() => {
    const loadUser = async () => {
      try {
        const res = await jwtAxios.get(`${API_SERVER_URL}/api/member/detail`);
        if (res.data?.result) {
          setFormData((prev) => ({
            ...prev,
            userName: res.data.result.userName,
            userEmail: res.data.result.userEmail,
          }));
        }
      } catch (err) {
        console.error("작성자 정보 로드 실패", err);
      }
    };
    loadUser();
  }, []);

  // 폼 제출: 카테고리/제목/내용 필수값 검증 후 게시글 등록 요청
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.categoryId) {
      alert("카테고리를 선택해주세요");
      return;
    }
    if (!formData.title) {
      alert("제목을 입력해주세요");
      return;
    }
    if (!formData.content) {
      alert("내용을 작성해주세요");
      return;
    }
    try {
      await jwtAxios.post(`${API_SERVER_URL}/api/community/insert`, formData);
      alert("작성 완료!");
      onSuccess(); // 부모에서 모달 닫기 및 목록 새로고침 처리
    } catch (err) {
      alert("작성 실패");
    }
  };

  return (
    // 오버레이 클릭 시 모달 닫힘 (배경 클릭으로 닫기)
    <div className="admin-modal-overlay" onClick={onClose}>
      {/* 모달 박스 내부 클릭은 오버레이의 onClose로 전파되지 않도록 이벤트 전파 차단 */}
      <div className="admin-modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="admin-modal-header">
          <h2>공지사항 작성{tabName && ` > ${tabName}`}</h2>
          <button type="button" className="admin-modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="insert-form">
          {/* 카테고리 선택 */}
          <div className="form-group">
            <label>카테고리 선택</label>
            <select
              name="categoryId"
              value={formData.categoryId}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, categoryId: e.target.value }))
              }
            >
              <option value="">카테고리를 선택하세요</option>
              {filteredCategories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.categoryName}
                </option>
              ))}
            </select>
          </div>

          {/* 제목 입력 */}
          <input
            name="title"
            placeholder="제목"
            value={formData.title}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, title: e.target.value }))
            }
          />

          {/* 리치 텍스트 에디터(Tiptap)로 본문 작성, 이미지 최대 4개 첨부 제한 */}
          <TiptapEditor
            value={formData.content}
            onChange={(html) =>
              setFormData((prev) => ({ ...prev, content: html }))
            }
            maxImageCount={4}
          />

          {/* 작성 완료 / 취소 버튼 */}
          <div className="admin-modal-actions">
            <button type="submit" className="submit-btn">
              글 작성하기
            </button>
            <button type="button" onClick={onClose}>
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdminNoticeWrite;
