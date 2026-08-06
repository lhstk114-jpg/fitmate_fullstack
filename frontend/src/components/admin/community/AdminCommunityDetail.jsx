import { useEffect, useState } from "react";
import { API_SERVER_URL } from "../../../apis/commonApi";
import jwtAxios from "../../../apis/util/jwtUtil";

/**
 * 관리자용 게시글 상세보기 모달
 * - props로 받은 id의 게시글 상세 정보를 조회해 읽기 전용으로 보여줌
 * - 삭제 버튼 클릭 시 게시글 삭제 후 onDeleted 콜백 호출 (부모에서 목록 갱신 처리)
 * - id: 조회할 게시글 번호
 * - onClose: 모달 닫기 콜백
 * - onDeleted: 삭제 완료 후 호출되는 콜백
 */
const AdminCommunityDetail = ({ id, onClose, onDeleted }) => {
  const [community, setCommunity] = useState(null); // 게시글 상세 데이터
  const [isLoading, setIsLoading] = useState(true); // 상세 조회 로딩 상태

  // id가 바뀔 때마다(모달이 열릴 때마다) 상세 정보를 새로 조회
  useEffect(() => {
    const fetchDetail = async () => {
      try {
        setIsLoading(true);
        // count=true : 상세 조회 시 조회수(hit)를 증가시키기 위한 파라미터
        const res = await jwtAxios.get(
          `${API_SERVER_URL}/api/community/detail/${id}?count=true`,
        );
        if (res.data?.community) setCommunity(res.data.community);
      } catch (err) {
        // 존재하지 않는 게시글이거나 조회 실패 시 알림 후 모달 닫기
        alert("존재하지 않는 게시글입니다");
        onClose();
      } finally {
        setIsLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  //게시글 삭제
  const handleDelete = async () => {
    if (!window.confirm("이 게시글을 삭제하시겠습니까?")) return;
    try {
      await jwtAxios.delete(
        `${API_SERVER_URL}/api/community/adminDelete/${id}`,
      );
      alert("삭제되었습니다");
      onDeleted();
    } catch (err) {
      alert("삭제 실패");
    }
  };

  //게시글 이미지 불러오기
  // 본문(content) 안에 상대 경로("/upload/...")로 저장된 이미지 src를
  // 실제 접근 가능한 절대 경로(API 서버 주소 포함)로 치환해서 이미지가 정상 표시되도록 함
  const renderableContent = (community?.content || "").replace(
    /src="\/upload\//g,
    `src="${API_SERVER_URL}/upload/`,
  );

  return (
    // 오버레이 클릭 시 모달 닫힘 (배경 클릭으로 닫기)
    <div className="admin-modal-overlay" onClick={onClose}>
      {/* 모달 박스 내부 클릭은 오버레이의 onClose로 전파되지 않도록 이벤트 전파 차단 */}
      <div className="admin-modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="admin-modal-header">
          <h2>게시글 상세보기</h2>
          <button type="button" className="admin-modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        {isLoading ? (
          <p>데이터를 불러오는 중입니다</p>
        ) : community ? (
          <div className="detailbody">
            {/* 제목 (읽기 전용) */}
            <div className="form-row">
              <label>제목</label>
              <input type="text" value={community.title || ""} readOnly />
            </div>

            {/* 탭 이름 / 카테고리 이름 (읽기 전용) */}
            <div className="form-group-row">
              <div className="flex-item">
                <label>탭 이름</label>
                <input type="text" value={community.tabName || ""} readOnly />
              </div>
              <div className="flex-item">
                <label>카테고리 이름</label>
                <input
                  type="text"
                  value={community.categoryName || ""}
                  readOnly
                />
              </div>
            </div>

            {/* 작성자 / 조회수 (읽기 전용) */}
            <div className="form-group-row">
              <div className="flex-item">
                <label>작성자</label>
                <input type="text" value={community.userName || ""} readOnly />
              </div>
              <div className="flex-item">
                <label>조회수</label>
                <input type="text" value={community.hit || ""} readOnly />
              </div>
            </div>

            {/* 본문 내용: HTML 형식으로 저장되어 있어 dangerouslySetInnerHTML로 렌더링 */}
            <div className="form-row">
              <label>내용</label>
              <div
                className="content-view"
                dangerouslySetInnerHTML={{ __html: renderableContent }}
              />
            </div>

            {/* 날짜 표시: 수정일이 있으면 수정일 우선, 없으면 작성일, 둘 다 없으면 안내 문구 */}
            <div className="form-row">
              <label>날짜</label>
              <div className="view-box">
                {community.updateTime
                  ? `수정일: ${community.updateTime?.split("T")[0] || ""}`
                  : community.createTime
                    ? `작성일: ${community.createTime?.split("T")[0] || ""}`
                    : "날짜 정보 없음"}
              </div>
            </div>

            {/* 삭제 / 닫기 버튼 */}
            <div className="admin-modal-actions">
              <button type="button" onClick={handleDelete}>
                삭제
              </button>
              <button type="button" onClick={onClose}>
                닫기
              </button>
            </div>
          </div>
        ) : (
          <p>게시글 정보가 없습니다</p>
        )}
      </div>
    </div>
  );
};

export default AdminCommunityDetail;
