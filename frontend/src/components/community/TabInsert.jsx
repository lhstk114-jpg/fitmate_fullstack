import axios from "axios";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/TabInsert.css";
import jwtAxios from "../../apis/util/jwtUtil";

/**
 * 관리자용 탭 생성 페이지
 * - 여러 개의 탭을 한 번에 추가할 수 있고, 각 탭마다 여러 카테고리를 함께 등록 가능
 * - "전체 만들기" 버튼 클릭 시 화면에 구성된 tabList 배열 전체를 서버로 한 번에 전송
 */
const TabInsert = () => {
  const navigate = useNavigate();
  // 탭 목록 상태: 각 탭은 { tabName, categoryList: [{ categoryName }] } 형태
  // categoryList를 배열로 초기화
  const [tabList, setTabList] = useState([
    { tabName: "", categoryList: [{ categoryName: "" }] },
  ]);

  // 특정 탭의 이름(tabName) 입력값 변경
  const onChangeInput = (tabIndex, e) => {
    const { name, value } = e.target;
    const newList = [...tabList];
    newList[tabIndex][name] = value;
    setTabList(newList);
  };

  // 특정 탭의 카테고리 배열 중 하나의 이름 변경
  const onChangeCategory = (tabIndex, catIndex, newValue) => {
    const newList = [...tabList];
    newList[tabIndex].categoryList[catIndex].categoryName = newValue;
    setTabList(newList);
  };

  // 새 탭 추가 (기본 카테고리 1개 포함)
  const onAddInput = () =>
    setTabList([
      ...tabList,
      { tabName: "", categoryList: [{ categoryName: "" }] },
    ]);

  // 특정 탭에 카테고리 입력칸 추가
  const onAddCategory = (tabIndex) => {
    const newList = [...tabList];
    newList[tabIndex].categoryList.push({ categoryName: "" }); // 객체로 저장
    setTabList(newList);
  };

  // 특정 탭의 카테고리 삭제
  const onRemoveCategory = (tabIndex, categoryIndex) => {
    const newList = [...tabList];
    newList[tabIndex].categoryList.splice(categoryIndex, 1);
    setTabList(newList);
  };

  // 구성한 탭/카테고리 목록 전체를 서버에 등록 요청
  const onTabFn = async () => {
    try {
      const res = await jwtAxios.post(
        `${API_SERVER_URL}/api/admin/tabInsert`,
        tabList,
      );
      alert("탭 생성 성공");
      navigate("/community");
    } catch (error) {
      alert("탭 생성 중 오류 발생");
    }
  };

  return (
    <div className="tabInsert">
      <h1 className="tab-title">탭 생성 페이지</h1>
      {tabList.map((tab, tabIndex) => (
        <div key={tabIndex} className="tab-section">
          <div className="section-header">
            <span className="badge">탭 {tabIndex + 1}</span>
            {/* 탭이 2개 이상일 때만 해당 탭 삭제 버튼 노출 (최소 1개는 유지) */}
            {tabList.length > 1 && (
              <button
                className="btn btn-danger-outline"
                onClick={() =>
                  setTabList(tabList.filter((_, i) => i !== tabIndex))
                }
              >
                탭 삭제
              </button>
            )}
          </div>
          {/* 탭 이름 입력 */}
          <div className="form-group">
            <label className="form-label">탭 이름</label>
            <input
              className="input-box tab-name-input"
              name="tabName"
              value={tab.tabName}
              onChange={(e) => onChangeInput(tabIndex, e)}
              placeholder="탭 이름"
            />
          </div>
          {/* 해당 탭에 속한 카테고리 입력칸들 출력 */}
          <div className="category-container">
            <label className="form-label">하위 카테고리 목록</label>
            <div className="category-list">
              {tab.categoryList.map((cat, catIndex) => (
                <div key={catIndex} className="category-item">
                  <span className="category-index">{catIndex + 1}</span>
                  <input
                    className="input-box"
                    value={cat.categoryName || ""}
                    onChange={(e) =>
                      onChangeCategory(tabIndex, catIndex, e.target.value)
                    }
                    placeholder={`카테고리 명칭`}
                  />
                </div>
              ))}
            </div>
            <div className="category-btn-group">
              {/* 카테고리 추가 버튼 */}
              <button
                onClick={() => onAddCategory(tabIndex)}
                className="btn btn-outline"
              >
                + 카테고리 추가
              </button>
              {/* 카테고리가 2개 이상일 때만 삭제 버튼 노출 (최소 1개는 유지) */}
              {tab.categoryList.length > 1 && (
                <button
                  onClick={() => onRemoveCategory(tabIndex)}
                  className="btn btn-danger-outline"
                >
                  - 카테고리 삭제
                </button>
              )}
            </div>
            {/* 마지막 탭 아래에만 "탭 추가" 버튼 노출 */}
            {tabIndex === tabList.length - 1 && (
              <div className="add-tab-wrapper">
                <button onClick={onAddInput} className="btn btn-sub">
                  + 새 탭 추가하기
                </button>
              </div>
            )}
          </div>
        </div>
      ))}
      {/* 구성된 모든 탭/카테고리를 서버에 일괄 등록 */}
      <button onClick={onTabFn} className="btn btn-submit">
        전체 만들기
      </button>
    </div>
  );
};

export default TabInsert;
