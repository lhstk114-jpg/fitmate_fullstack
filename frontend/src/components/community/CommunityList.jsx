import axios from "axios";
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_SERVER_URL } from "../../apis/commonApi";
import { getCookie } from "../../apis/util/cookieUtil";

// FAQ 카테고리 여부 판단 (대소문자 무관, 다른 곳의 QNA 체크와 동일한 방식으로 통일)
const isFaqCategory = (categoryName) =>
  !!categoryName && categoryName.toUpperCase().includes("FAQ");

/**
 * 게시글 목록 컴포넌트
 * props:
 * - params: { tabId, categoryId } 조회 필터
 * - tab: 현재 선택된 탭 정보 { tabName, adminOnly, categoryName }
 */
const CommunityList = ({ params, tab }) => {
  const [list, setList] = useState([]); // 현재 페이지의 게시글 목록
  const [page, setPage] = useState(0); // 0부터 시작 (Spring Pageable 기본)
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0); // 전체 게시글 수 (번호 계산에 사용)
  const size = 10; // 페이지당 게시글 수
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [subject, setSubject] = useState("");
  const [keyword, setKeyword] = useState("");
  const member = getCookie("member");
  const isAdmin = member?.role && String(member.role).toUpperCase() === "ADMIN";

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setKeyword(search);
  };

  const [openFaqIds, setOpenFaqIds] = useState(new Set());

  // "게시글 작성" 버튼 노출 여부: 관리자 전용 탭이면 관리자만, 아니면 누구나 노출
  const canShowWriteButton = useMemo(() => {
    if (!tab) return true;
    if (tab.adminOnly) {
      return isAdmin;
    }
    return true;
  }, [tab, isAdmin]);

  // 필터(탭/카테고리)가 바뀌면 페이지를 0으로 리셋
  useEffect(() => {
    setPage(0);
    setSearch("");
    setSubject("");
    setKeyword("");
  }, [params]);

  // 서치/ 필터 또는 페이지가 바뀔 때마다 목록 재조회
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(`${API_SERVER_URL}/api/community/tclist`, {
          params: {
            tabId: params?.tabId,
            categoryId: params?.categoryId,
            page,
            size,
            subject: subject || undefined,
            keyword: keyword || "",
          },
        });
        const { content, totalPages: tp, totalElements: te } = res.data.result;
        setList(content || []);
        setTotalPages(tp || 0);
        setTotalElements(te || 0);
        setOpenFaqIds(new Set());
      } catch (error) {
        alert("게시글 목록을 불러오는 중 에러가 발생했습니다: " + error);
      }
    };
    fetchData();
  }, [params, page, subject, keyword]);

  // "게시글 작성" 버튼 클릭하면 로그인 여부 및 관리자 전용 탭 권한 확인 후 작성 페이지로 이동
  const handleWriteClick = () => {
    const member = getCookie("member");
    if (!member?.access) {
      alert("로그인이 필요합니다");
      navigate("/auth/login");
      return;
    }
    if (tab?.adminOnly && member.role !== "ADMIN") {
      alert("공지사항은 관리자만 작성할 수 있습니다.");
      return;
    }
    // 현재 선택된 탭/카테고리를 state로 넘겨 작성 페이지에서 자동으로 채워지도록 함
    navigate("/community/insert", {
      state: { tabId: params?.tabId, categoryId: params?.categoryId },
    });
  };

  // FAQ 제목 클릭 시 상세 이동 대신 펼침/접힘 토글
  const toggleFaq = (id) => {
    setOpenFaqIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  // FAQ면 토글, 아니면 기존처럼 상세 페이지로 이동
  const handleTitleClick = (item) => {
    if (isFaqCategory(item.categoryName)) {
      toggleFaq(item.id);
    } else {
      navigate(`/community/detail/${item.id}`);
    }
  };

  const renderableContent = (content) =>
    (content || "").replace(
      /src="\/upload\//g,
      `src="${API_SERVER_URL}/api/upload/`,
    );

  //시간 서식 함수
  const formatDateTime = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day} ${hours}:${minutes}`;
  };

  return (
    <>
      <div className="communityList">
        <div className="communityList-con">
          <h1>
            {tab?.tabName}
            {tab?.categoryName && ` > ${tab.categoryName}`}
          </h1>

          {/* 상단 검색 영역 (우측 정렬) */}
          <div className="community-top-bar">
            <form onSubmit={handleSearch} className="search-form">
              <select
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
              >
                <option value="">전체</option>
                <option value="title">제목</option>
                <option value="userName">작성자</option>
              </select>
              <input
                type="text"
                placeholder="검색어를 입력하세요"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <button type="submit">검색</button>
            </form>
          </div>

          <table>
            <thead>
              <tr>
                <th>번호</th>
                <th>카테고리</th>
                <th>제목</th>
                <th>작성자</th>
                <th>작성시간</th>
                <th>조회수</th>
              </tr>
            </thead>
            <tbody>
              {list.map((item, index) => {
                const displayId = totalElements - page * size - index;
                const isFaq = isFaqCategory(item.categoryName);
                const isOpen = openFaqIds.has(item.id);

                return (
                  <React.Fragment key={item.id || index}>
                    <tr>
                      <td>{displayId}</td>
                      <td>{item.categoryName}</td>
                      <td onClick={() => handleTitleClick(item)}>
                        <div className="board-title-wrapper">
                          {item.thumbnail ? (
                            <img
                              className="board-item-thumb"
                              src={item.thumbnail}
                              alt=""
                            />
                          ) : (
                            <div className="board-item-thumb board-item-thumb-empty" />
                          )}
                          <p>
                            {item.title}
                            {isFaq && <span> {isOpen ? "▲" : "▼"}</span>}
                          </p>
                        </div>
                      </td>
                      <td>{item.userName}</td>
                      <td>{formatDateTime(item.createTime)}</td>
                      <td>{item.hit}</td>
                    </tr>

                    {/* 답변 */}
                    {isFaq && isOpen && (
                      <tr className="faq-answer-row">
                        <td colSpan={5}>
                          <div
                            className="faq-answer-content"
                            dangerouslySetInnerHTML={{
                              __html: renderableContent(item.content),
                            }}
                          />
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </table>

          {/* 하단 영역 (게시글 작성 버튼을 우측에 여백을 주어 배치) */}
          <div className="community-bottom-bar">
            {canShowWriteButton && (
              <button
                type="button"
                className="write-btn"
                onClick={handleWriteClick}
              >
                게시글 작성
              </button>
            )}
          </div>

          {/* 페이지네이션: 이전/다음 버튼 방식 (페이지가 2개 이상일 때만 표시) */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                이전
              </button>
              <span>
                {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                다음
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default CommunityList;
