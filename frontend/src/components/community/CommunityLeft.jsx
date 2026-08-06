import axios from "axios";
import React, { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "../../css/Community/CommunityLeft.css";
import { API_SERVER_URL } from "../../apis/commonApi";

/**
 * 커뮤니티 왼쪽 사이드바 (탭/카테고리 네비게이션)
 * - 탭을 클릭하면 하위 카테고리 목록이 아코디언처럼 펼쳐짐
 * - NavLink를 사용해 현재 경로에 맞는 탭/카테고리로 라우팅 (URL 이동이 곧 필터 변경)
 * - 탭/카테고리 목록은 이 컴포넌트가 직접 조회함 (CommunityListPage.jsx가 <CommunityLeft />를
 *   props 없이 렌더링하는 구조이므로, props로 받는 버전을 쓰면 tabs/categories가 항상 빈 배열이 되어
 *   "전체게시판"만 보이고 나머지 탭이 안 뜨는 문제가 생김 — 반드시 이 자체 조회 버전을 사용할 것)
 */
const CommunityLeft = () => {
  const [tabList, setTabList] = useState([]);
  const [categoryList, setCategoryList] = useState([]);
  // 어떤 탭이 열려있는지 관리하는 상태 (null이면 모두 닫힘, id값이 들어가면 해당 탭 열림)
  const [openTabId, setOpenTabId] = useState(null);
  const navigate = useNavigate();

  // 마운트 시 탭 목록과 카테고리 목록을 조회
  useEffect(() => {
    const fetchData = async () => {
      try {
        const tabRes = await axios.get(
          `${API_SERVER_URL}/api/community/tabList`,
        );
        const catRes = await axios.get(
          `${API_SERVER_URL}/api/community/category`,
        );
        setTabList(tabRes.data.result);
        setCategoryList(catRes.data.result);
      } catch (err) {
        console.error(err);
      }
    };
    fetchData();
  }, []);

  // 탭 클릭 시 열림/닫힘 토글 함수
  const handleToggleTab = (tabId, e) => {
    // 링크 자체의 기본 이동을 막고 토글만 제어하고 싶다면 e.preventDefault() 활용 가능
    // 만약 라우터 이동과 토글을 동시에 하고 싶다면 아래와 같이 작성
    setOpenTabId(openTabId === tabId ? null : tabId);
  };

  return (
    <div className="community-left">
      <ul>
        {/* 전체게시판 링크 (필터 없이 전체 목록으로 이동) 전체게시판 필요 시 주석 해제 */}
        {/* <li>
          <NavLink to="/community/communityList" end>
            전체게시판
          </NavLink>
        </li> */}
        {tabList.map((tabItem) => (
          <li key={tabItem.id}>
            <div
              className="tab-header"
              onClick={(e) => handleToggleTab(tabItem.id, e)}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              {/* 탭 이름 클릭 시 해당 탭 전체 게시글 목록으로 이동 */}
              <NavLink to={`/community/tab/${tabItem.id}`}>
                {tabItem.tabName}
              </NavLink>
              {/* 토글 화살표 표시 (열림/닫힘 상태 시각화, 선택사항) */}
              <span>{openTabId === tabItem.id ? "▲" : "▼"}</span>
            </div>

            {/* openTabId가 일치할 때만 open 클래스가 붙어 하위 카테고리 목록이 펼쳐짐 */}
            <ul className={`overTab ${openTabId === tabItem.id ? "open" : ""}`}>
              {categoryList
                .filter((cat) => cat.tabId === tabItem.id) // 현재 탭에 속한 카테고리만 표시
                .map((cat) => (
                  <li key={cat.id}>
                    <NavLink
                      to={`/community/tab/${tabItem.id}/category/${cat.id}`}
                    >
                      {cat.categoryName}
                    </NavLink>
                  </li>
                ))}
            </ul>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CommunityLeft;
