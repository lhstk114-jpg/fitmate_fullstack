import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams } from "react-router-dom";
import CommunityLeft from "../../components/community/CommunityLeft.jsx";
import CommunityList from "../../components/community/CommunityList.jsx";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/CommunityLeft.css";
import "../../css/Community/CommunityList.css";

/**
 * 커뮤니티 게시판 페이지 (라우트 진입점)
 * - URL 파라미터(useParams)를 그대로 필터 조건으로 사용해 CommunityList에 전달
 * - 사이드바(CommunityLeft)는 NavLink로 URL을 직접 이동시키므로,
 *   이 페이지는 별도 상태 관리 없이 URL 변화에 따라 params/tab만 재계산하면 됨
 */
const CommunityListPage = () => {
  const { tabId, categoryId } = useParams();
  const [tabList, setTabList] = useState([]); // 현재 탭 이름/adminOnly 여부를 찾기 위한 전체 탭 목록
  const [categoryList, setCategoryList] = useState([]); //카테고리 이름을 찾기 위한 목록

  // 마운트 시 전체 탭 목록 조회 (탭 이름/관리자 전용 여부를 URL의 tabId와 매칭하기 위함)
  useEffect(() => {
    const fetchTabList = async () => {
      try {
        const res = await axios.get(`${API_SERVER_URL}/api/community/tabList`);
        setTabList(res.data.result || []);
      } catch (err) {
        console.error("탭 목록 로드 실패", err);
      }
    };
    fetchTabList();
  }, []);

  // 카테고리 목록 조회
  useEffect(() => {
    const fetchCategoryList = async () => {
      try {
        const res = await axios.get(`${API_SERVER_URL}/api/community/category`);
        setCategoryList(res.data.result || []);
      } catch (err) {
        console.error("카테고리 목록 로드 실패", err);
      }
    };
    fetchCategoryList();
  }, []);

  // URL의 tabId(string)로 실제 탭 정보를 찾는다
  const currentTab = tabId
    ? tabList.find((t) => String(t.id) === String(tabId))
    : null;

  // url 카테고리아이디로 실제 카테고리 정보 찾기
  const currentCategory = categoryId
    ? categoryList.find((c) => String(c.id) === String(categoryId))
    : null;

  // CommunityList 상단에 표시할 탭 이름/관리자 전용 여부 (URL에 tabId가 없으면 전체게시판으로 표시)
  const selectTab = {
    tabName: currentTab?.tabName || "전체게시판",
    categoryName: currentCategory?.categoryName || "",
    adminOnly: !!currentTab?.adminOnly,
  };

  // CommunityList 조회에 사용할 필터 파라미터 (URL 문자열 파라미터를 숫자로 변환)
  const params = {
    tabId: tabId ? Number(tabId) : null,
    categoryId: categoryId ? Number(categoryId) : null,
  };

  return (
    <div className="community-wrapper" style={{ display: "flex" }}>
      {/* 사이드바: 탭/카테고리 클릭 시 NavLink로 URL이 바뀌고, 그 결과 이 컴포넌트가 재렌더링되며 params가 갱신됨 */}
      <CommunityLeft />
      {/* URL에서 파생된 필터(params)와 탭 표시 정보(selectTab)를 목록 컴포넌트로 전달 */}
      <CommunityList params={params} tab={selectTab} />
    </div>
  );
};

export default CommunityListPage;
