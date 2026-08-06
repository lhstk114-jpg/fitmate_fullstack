import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/TabList.css";

/**
 * 관리자용 탭 목록 페이지
 * - 전체 탭과 각 탭에 속한 카테고리들을 표 형태로 보여줌
 * - 탭 이름 클릭 시 해당 탭 상세(TabDetail) 페이지로 이동
 * - "탭 추가" 버튼으로 탭 생성(TabInsert) 페이지로 이동
 */
const TabList = () => {
  const navigate = useNavigate();
  const [tabList, setTabList] = useState([]); // 탭 목록 (각 탭에 categoryList 포함)
  const [isLoading, setIsLoading] = useState(true);
  const { categoryId, tabId } = useParams(); // URL에 특정 탭/카테고리가 있으면 필터링용으로 사용

  // 탭 목록 조회 (URL 파라미터의 tabId/categoryId가 있으면 서버에 필터 조건으로 전달)
  const fetchTabData = async () => {
    setIsLoading(true);
    const url = `${API_SERVER_URL}/api/community/tabList`;
    try {
      const res = await axios.get(url, {
        params: {
          tabId: tabId,
          categoryId: categoryId,
        },
      });
      setTabList(res.data.result || []);
    } catch (error) {
      alert(error);
    } finally {
      setIsLoading(false);
    }
  };

  // categoryId/tabId(라우트 파라미터)가 바뀔 때마다 목록 재조회
  useEffect(() => {
    fetchTabData();
  }, [categoryId, tabId]);
  return (
    <>
      <div className="tabList">
        <div className="tabList-con">
          <h1>탭 목록</h1>
          <button onClick={() => navigate("/admin/tabInsert")}>탭 추가</button>
          {isLoading ? (
            <p>로딩중...</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>탭 이름</th>
                  <th>카테고리</th>
                </tr>
              </thead>
              <tbody>
                {tabList.map((tab, index) => (
                  <tr key={tab.id || index}>
                    {/* 탭 이름 클릭 시 해당 탭 상세/수정 페이지로 이동 */}
                    <td onClick={() => navigate(`/admin/tabDetail/${tab.id}`)}>
                      {tab.tabName}
                    </td>
                    {/* 해당 탭에 속한 카테고리 이름들을 콤마로 구분해 한 줄에 표시 */}
                    <td>
                      {tab.categoryList
                        .map((cat) => cat.categoryName)
                        .join(", ")}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
};

export default TabList;
