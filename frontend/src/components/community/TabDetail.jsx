import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/TabDetail.css";
import jwtAxios from "../../apis/util/jwtUtil";

/**
 * 관리자용 탭 상세/수정 페이지
 * - 특정 탭의 이름, 관리자 전용 여부, 하위 카테고리 목록을 조회하고 수정 가능
 * - 카테고리 추가/삭제는 화면에서 로컬 상태만 변경하고, 실제 저장(수정 버튼 클릭)
 *   시점에 백엔드로 전체 목록을 보내 반영됨
 */
const TabDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [tab, setTab] = useState(null); // 탭 상세 데이터 (categoryList 포함)
  const [isLoading, setIsLoading] = useState(true);

  //상세정보 보기
  const getTabDetail = async () => {
    try {
      setIsLoading(true);
      const res = await jwtAxios.get(
        `${API_SERVER_URL}/api/admin/tabDetail/${id}`,
      );
      if (res.data?.tab) {
        setTab(res.data.tab);
      }
    } catch (error) {
      alert("탭이 존재하지 않습니다");
      navigate("/admin/tabList");
    } finally {
      setIsLoading(false);
    }
  };
  // id가 있을 때만(라우트 진입 시) 탭 상세 조회
  useEffect(() => {
    if (id) {
      getTabDetail();
    }
  }, [id]);

  // ---- 카테고리 이름 수정: 배열에서 해당 인덱스의 categoryName만 교체 ----
  const handleCategoryNameChange = (index, value) => {
    const newList = [...(tab.categoryList || [])];
    newList[index] = { ...newList[index], categoryName: value };
    setTab({ ...tab, categoryList: newList });
  };

  // ---- 카테고리 삭제 (목록에서 제거만 하면, 제출 시 백엔드가 자동으로 삭제 처리) ----
  const handleCategoryDelete = (index) => {
    if (!window.confirm("이 카테고리를 삭제하시겠습니까?")) return;
    const newList = (tab.categoryList || []).filter((_, i) => i !== index);
    setTab({ ...tab, categoryList: newList });
  };

  // ---- 카테고리 추가 (id 없이 추가하면 백엔드가 신규 생성으로 처리) ----
  const handleCategoryAdd = () => {
    const newList = [
      ...(tab.categoryList || []),
      { id: null, categoryName: "" },
    ];
    setTab({ ...tab, categoryList: newList });
  };

  //탭 수정
  const getTabUpdate = async () => {
    // 빈 이름으로 저장되는 것 방지 (카테고리 이름이 하나라도 비어있으면 저장 차단)
    const hasEmptyName = (tab.categoryList || []).some(
      (cat) => !cat.categoryName?.trim(),
    );
    if (hasEmptyName) {
      alert("카테고리 이름을 모두 입력해주세요.");
      return;
    }

    try {
      setIsLoading(true);
      const res = await jwtAxios.put(
        `${API_SERVER_URL}/api/admin/tabUpdate/${id}`,
        tab,
      );
      alert("수정되었습니다.");
      navigate("/community/tabList");
    } catch (error) {
      console.error(error);
      alert("수정 실패");
    } finally {
      setIsLoading(false);
    }
  };

  //탭 삭제
  const getTabDelete = async () => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      setIsLoading(true);
      const res = await jwtAxios.delete(
        `${API_SERVER_URL}/api/admin/tabDelete/${id}`,
      );
      if (res.data?.result) {
        setTab(res.data.result);
        navigate("/community/tabList");
      }
    } catch (error) {
      alert("삭제 시도 중 오류가 발생했습니다");
      navigate("/community/tabList");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <div className="tabDetail">
        <div className="tabDetail-con">
          <h1>탭 상세 페이지</h1>
          {isLoading ? (
            <p>데이터를 불러오는 중입니다</p>
          ) : tab ? (
            <div className="detailbody">
              <ul>
                {/* 관리자 전용(공지사항용) 탭 여부 체크박스 */}
                <li>
                  <label>
                    <input
                      type="checkbox"
                      checked={tab.adminOnly || false}
                      onChange={(e) =>
                        setTab({ ...tab, adminOnly: e.target.checked })
                      }
                    />
                    관리자만 작성/삭제 가능 (공지사항용)
                  </label>
                </li>
                {/* 탭 이름 수정 */}
                <li>
                  <label htmlFor="tabName">이름</label>
                  <input
                    type="text"
                    name="tabName"
                    value={tab.tabName || ""}
                    onChange={(e) =>
                      setTab({ ...tab, tabName: e.target.value })
                    }
                  />
                </li>
                {/* 카테고리 목록: 이름 수정 / 개별 삭제 / 추가 */}
                <li>
                  <label>카테고리</label>
                  <div className="category-edit-group">
                    {tab.categoryList?.map((cat, index) => (
                      <div
                        key={cat.id ?? `new-${index}`}
                        className="category-edit-row"
                      >
                        <input
                          type="text"
                          value={cat.categoryName}
                          placeholder="카테고리 이름"
                          onChange={(e) =>
                            handleCategoryNameChange(index, e.target.value)
                          }
                        />
                        <button
                          type="button"
                          onClick={() => handleCategoryDelete(index)}
                        >
                          삭제
                        </button>
                      </div>
                    ))}
                    <button type="button" onClick={handleCategoryAdd}>
                      + 카테고리 추가
                    </button>
                  </div>
                </li>
                {/* 생성일/수정일 표시: 수정일이 있으면 수정일 우선 */}
                <li>
                  <label>생성일</label>
                  <div className="view-box">
                    {tab?.updateTime
                      ? `수정일: ${tab.updateTime?.split("T")[0]}`
                      : `생성일: ${tab?.createTime?.split("T")[0] || ""}`}
                  </div>
                </li>
                <li>
                  <button onClick={() => getTabUpdate()}>수정</button>
                </li>
              </ul>
              <div className="button">
                <button onClick={() => navigate("/community/tabList")}>
                  목록으로 돌아가기
                </button>
                <button onClick={() => getTabDelete()}>삭제</button>
              </div>
            </div>
          ) : (
            <p>게시글 정보가 없습니다</p>
          )}
        </div>
      </div>
    </>
  );
};

export default TabDetail;
