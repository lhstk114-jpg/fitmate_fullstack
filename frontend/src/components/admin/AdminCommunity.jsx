import React, { useEffect, useMemo, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil.jsx";
import { API_SERVER_URL } from "../../apis/commonApi";
import { getCookie } from "../../apis/util/cookieUtil";
import TabList from "../community/TabList.jsx";
import PageGenerate from "../common/Page/PageGenerate.jsx";
import AdminNoticeWrite from "./community/AdminNoticeWrite.jsx";
import AdminCommunityDetail from "./community/AdminCommunityDetail.jsx";

// 탭별 그룹 모드(탭 미선택 시)에서 각 탭 섹션에 한 번에 보여줄 게시글 개수
const GROUP_SIZE = 5; // 탭별 섹션에서 한 번에 보여줄 개수 (필요시 조정)

/**
 * 관리자 커뮤니티(게시글) 관리 페이지
 * - 특정 탭을 선택하면 "단일 탭 모드"로 동작 (한 탭의 게시글만 페이징하여 표시)
 * - 탭을 선택하지 않으면 "탭별 그룹 모드"로 동작 (모든 탭을 섹션별로 나눠 각각 미리보기 표시)
 */
const AdminCommunity = () => {
  // 공지사항 작성 모달 열림 여부
  const [openModal, setOpenModal] = useState(false);

  // 전체 탭 목록, 전체 카테고리 목록 (서버에서 로드)
  const [tabs, setTabs] = useState([]);
  const [categories, setCategories] = useState([]);

  // 검색/필터 조건 (탭, 카테고리, 키워드)
  const [filters, setFilters] = useState({
    tabId: "",
    categoryId: "",
    keyword: "",
  });

  // ---- 단일 탭 선택 모드용 상태 (기존 방식 그대로 유지) ----
  const [list, setList] = useState([]); // 현재 탭의 게시글 목록
  const [selectedIds, setSelectedIds] = useState([]); // 체크박스로 선택된 게시글 id 목록
  const [page, setPage] = useState(0); // 현재 페이지 번호
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수
  const [totalElements, setTotalElements] = useState(0); // 전체 게시글 수
  const [isLoading, setIsLoading] = useState(true); // 목록 로딩 상태
  const size = 20; // 단일 탭 모드에서 페이지당 게시글 수

  // ---- 탭별 그룹 모드용 상태 (탭 미선택 시 기본 화면) ----
  // 구조: { [tabId]: { list, page, totalPages, totalElements, isLoading, selectedIds } }
  const [groupedByTab, setGroupedByTab] = useState({});

  // 모달에서 사용할 상태값
  const [writeTabId, setWriteTabId] = useState(null); // 공지사항 작성 대상 탭 id (모달 오픈 트리거)
  const [detailId, setDetailId] = useState(null); // 상세보기 모달에서 조회할 게시글 id

  // 단일 탭 모드 페이지네이션 그룹 계산 (한 번에 보여줄 페이지 버튼 묶음: 1~10, 11~20 ...)
  const pageGroupSize = 10;
  const currentGroup = Math.floor(page / pageGroupSize);
  const startPage = currentGroup * pageGroupSize + 1;
  const endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

  // 탭이 하나라도 선택돼 있으면 "단일 탭 모드", 아니면 "탭별 그룹 모드"
  const isSingleTabMode = !!filters.tabId;

  // 최초 마운트 시 탭 목록과 카테고리 목록을 동시에 조회
  useEffect(() => {
    const fetchTcList = async () => {
      try {
        const [tabRes, catRes] = await Promise.all([
          jwtAxios.get(`${API_SERVER_URL}/api/community/tabList`),
          jwtAxios.get(`${API_SERVER_URL}/api/community/category`),
        ]);
        setTabs(tabRes.data.result || []);
        setCategories(catRes.data.result || []);
      } catch (err) {
        console.error("탭/카테고리 로드 실패", err);
      }
    };
    fetchTcList();
  }, []);

  // 현재 선택된 탭(filters.tabId)에 속한 카테고리만 필터링 (카테고리 select box에 사용)
  const filteredCategories = useMemo(
    () =>
      categories.filter((cat) => String(cat.tabId) === String(filters.tabId)),
    [filters.tabId, categories],
  );

  // ---- 단일 탭 모드: 선택된 탭/카테고리/키워드 조건으로 게시글 목록 조회 (기존 로직 그대로) ----
  const fetchList = async () => {
    try {
      setIsLoading(true);
      const res = await jwtAxios.get(`${API_SERVER_URL}/api/community/tclist`, {
        params: {
          tabId: filters.tabId || undefined,
          categoryId: filters.categoryId || undefined,
          keyword: filters.keyword || undefined,
          page,
          size,
        },
      });
      const { content, totalPages: tp, totalElements: te } = res.data.result;
      setList(content || []);
      setTotalPages(tp || 0);
      setTotalElements(te || 0);
      setSelectedIds([]); // 목록이 갱신되면 선택 상태 초기화
    } catch (error) {
      alert("목록을 불러오지 못했습니다");
    } finally {
      setIsLoading(false);
    }
  };

  // ---- 탭별 그룹 모드: 각 탭마다 별도로 tclist API를 호출해 미리보기 목록을 채움 ----
  // tabIdOverride: 특정 탭 하나만 다시 불러올 때 지정 (예: 해당 탭의 페이지네이션 클릭 시)
  // pageOverride: 위 탭에 대해 조회할 페이지 번호를 지정
  const fetchGroupedList = async (tabIdOverride, pageOverride) => {
    if (tabs.length === 0) return;

    // tabIdOverride가 없으면 전체 탭을 대상으로, 있으면 해당 탭만 대상으로 조회
    const targets = tabIdOverride ? [tabIdOverride] : tabs.map((t) => t.id);

    // 대상 탭들의 로딩 상태를 먼저 true로 표시 (로딩 중 UI 표시용)
    setGroupedByTab((prev) => {
      const next = { ...prev };
      targets.forEach((tabId) => {
        next[tabId] = {
          ...(next[tabId] || {
            list: [],
            page: 0,
            totalPages: 0,
            totalElements: 0,
            selectedIds: [],
          }),
          isLoading: true,
        };
      });
      return next;
    });

    // 대상 탭들에 대해 병렬로 목록 조회
    await Promise.all(
      targets.map(async (tabId) => {
        // 페이지 오버라이드가 해당 탭에 대한 것이면 그 값을, 아니면 기존 저장된 페이지를 사용
        const currentPage =
          pageOverride !== undefined && tabIdOverride === tabId
            ? pageOverride
            : groupedByTab[tabId]?.page || 0;

        try {
          const res = await jwtAxios.get(
            `${API_SERVER_URL}/api/community/tclist`,
            {
              params: {
                tabId,
                keyword: filters.keyword || undefined,
                page: currentPage,
                size: GROUP_SIZE,
              },
            },
          );
          const {
            content,
            totalPages: tp,
            totalElements: te,
          } = res.data.result;

          // 해당 탭의 결과만 갱신 (다른 탭 데이터는 유지)
          setGroupedByTab((prev) => ({
            ...prev,
            [tabId]: {
              list: content || [],
              page: currentPage,
              totalPages: tp || 0,
              totalElements: te || 0,
              selectedIds: [],
              isLoading: false,
            },
          }));
        } catch (error) {
          // 실패 시 해당 탭만 빈 목록 처리하고 로딩 해제
          setGroupedByTab((prev) => ({
            ...prev,
            [tabId]: {
              ...(prev[tabId] || {}),
              list: [],
              isLoading: false,
            },
          }));
        }
      }),
    );
  };

  // 탭 목록이 로드되고, 단일 탭 모드가 아닐 때(=탭 미선택 상태) 그룹 목록을 초기 로드
  useEffect(() => {
    if (!isSingleTabMode && tabs.length > 0) {
      fetchGroupedList();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabs, isSingleTabMode]);

  // 단일 탭 모드에서 페이지가 바뀌거나 모드가 전환될 때 목록 재조회
  useEffect(() => {
    if (isSingleTabMode) {
      fetchList();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, isSingleTabMode]);

  // 검색 버튼 클릭(또는 엔터) 시 실행: 모드에 따라 적절한 조회 함수 호출
  const handleSearch = () => {
    if (isSingleTabMode) {
      if (page === 0) {
        fetchList(); // 이미 1페이지면 바로 재조회
      } else {
        setPage(0); // 페이지를 0으로 리셋하면 useEffect가 자동으로 fetchList 호출
      }
    } else {
      fetchGroupedList();
    }
  };

  // 필터(탭/카테고리/키워드) 변경 핸들러
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    if (name === "tabId") {
      // 탭이 바뀌면 카테고리 선택은 초기화하고 페이지도 0으로 리셋
      setFilters((prev) => ({ ...prev, tabId: value, categoryId: "" }));
      setPage(0);
    } else {
      setFilters((prev) => ({ ...prev, [name]: value }));
    }
  };

  // 키워드 입력창에서 엔터키 입력 시 검색 실행
  const handleKeyDown = (e) => {
    if (e.key === "Enter") handleSearch();
  };

  // 단일 탭 모드 페이지네이션 클릭 핸들러
  const handlePageChange = (search, subject, newPage) => {
    setPage(newPage);
  };

  // 탭별 그룹 섹션 내 페이지네이션 클릭 핸들러 (해당 탭만 재조회)
  const handleGroupPageChange = (tabId, newPage) => {
    fetchGroupedList(tabId, newPage);
  };

  // "공지사항 작성" 버튼 클릭: adminOnly(관리자 전용) 속성을 가진 탭을 찾아 작성 모달을 오픈
  const noticeWrite = () => {
    const noticeTab = tabs.find((tab) => tab.adminOnly);
    if (!noticeTab) {
      alert("공지사항 탭이 존재하지 않습니다. 탭 관리에서 먼저 생성해주세요.");
      return;
    }
    setWriteTabId(noticeTab.id);
  };

  // ---- 단일 탭 모드: 개별/전체 선택 토글 ----
  const toggleSelect = (id) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id],
    );
  };
  const toggleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedIds(list.map((item) => item.id));
    } else {
      setSelectedIds([]);
    }
  };

  // ---- 탭별 그룹 모드: 개별/전체 선택 토글 (탭별로 독립적으로 관리) ----
  const toggleGroupSelect = (tabId, id) => {
    setGroupedByTab((prev) => {
      const group = prev[tabId];
      if (!group) return prev;
      const nextSelected = group.selectedIds.includes(id)
        ? group.selectedIds.filter((v) => v !== id)
        : [...group.selectedIds, id];
      return {
        ...prev,
        [tabId]: { ...group, selectedIds: nextSelected },
      };
    });
  };

  const toggleGroupSelectAll = (tabId, e) => {
    setGroupedByTab((prev) => {
      const group = prev[tabId];
      if (!group) return prev;
      const nextSelected = e.target.checked
        ? group.list.map((item) => item.id)
        : [];
      return {
        ...prev,
        [tabId]: { ...group, selectedIds: nextSelected },
      };
    });
  };

  // 탭별 그룹 모드에서 선택된 게시글들을 일괄 삭제
  const handleGroupDeleteSelected = async (tabId) => {
    const group = groupedByTab[tabId];
    if (!group || group.selectedIds.length === 0) {
      alert("삭제할 게시글을 선택해주세요");
      return;
    }
    if (
      !window.confirm(
        `선택한 ${group.selectedIds.length}개 게시글을 삭제하시겠습니까?`,
      )
    )
      return;

    try {
      // 선택된 각 게시글에 대해 삭제 요청을 병렬로 실행 (일부 실패해도 나머지는 계속 처리)
      const results = await Promise.allSettled(
        group.selectedIds.map((id) =>
          jwtAxios.delete(`${API_SERVER_URL}/api/community/adminDelete/${id}`),
        ),
      );

      const failed = results.filter((r) => r.status === "rejected");
      if (failed.length > 0) {
        alert(
          `${failed.length}건 삭제 실패, ${results.length - failed.length}건 삭제 완료`,
        );
      } else {
        alert("삭제되었습니다");
      }
      // 삭제 후 해당 탭 목록을 같은 페이지로 다시 조회하여 갱신
      fetchGroupedList(tabId, group.page);
    } catch (error) {
      alert("일괄 삭제 중 오류가 발생했습니다");
    }
  };

  // 삭제/작성 완료 후 현재 모드에 맞게 목록을 새로고침
  const refreshAfterDelete = () => {
    if (isSingleTabMode) {
      fetchList();
    } else {
      fetchGroupedList();
    }
  };

  // 게시글 1건 삭제 (테이블 행의 "삭제" 버튼, 단일/그룹 모드 공용)
  const handleDeleteOne = async (id) => {
    if (!window.confirm("이 게시글을 삭제하시겠습니까?")) return;
    try {
      await jwtAxios.delete(
        `${API_SERVER_URL}/api/community/adminDelete/${id}`,
      );
      alert("삭제되었습니다");
      refreshAfterDelete();
    } catch (error) {
      alert("삭제 실패");
    }
  };

  // 단일 탭 모드: 선택된 게시글들을 일괄 삭제
  const handleDeleteSelected = async () => {
    if (selectedIds.length === 0) {
      alert("삭제할 게시글을 선택해주세요");
      return;
    }
    if (
      !window.confirm(
        `선택한 ${selectedIds.length}개 게시글을 삭제하시겠습니까?`,
      )
    )
      return;

    try {
      const results = await Promise.allSettled(
        selectedIds.map((id) =>
          jwtAxios.delete(`${API_SERVER_URL}/api/community/adminDelete/${id}`),
        ),
      );

      const failed = results.filter((r) => r.status === "rejected");
      if (failed.length > 0) {
        alert(
          `${failed.length}건 삭제 실패, ${results.length - failed.length}건 삭제 완료`,
        );
      } else {
        alert("삭제되었습니다");
      }
      fetchList();
    } catch (error) {
      alert("일괄 삭제 중 오류가 발생했습니다");
    }
  };

  // ---- 공통 테이블 렌더링 함수 (단일 탭 모드 / 그룹 모드 양쪽에서 재사용) ----
  // rows: 표시할 게시글 배열
  // isLoading: 로딩 여부 (로딩 중이면 텍스트만 표시)
  // selectedIds: 현재 선택된 id 배열
  // onToggleSelect / onToggleSelectAll: 체크박스 토글 콜백
  // showTabColumn: 탭 이름 컬럼을 표시할지 여부 (현재는 두 모드 모두 false로 사용)
  const renderTable = ({
    rows,
    isLoading: loading,
    selectedIds: rowSelectedIds,
    onToggleSelect,
    onToggleSelectAll,
    showTabColumn,
    totalElements: rowTotalElements = 0,
    page: rowPage = 0,
    size: rowSize = 20,
  }) => {
    if (loading) {
      return <p>목록을 불러오는 중입니다</p>;
    }
    return (
      <table>
        <thead>
          <tr>
            <th>
              {/* 전체 선택 체크박스: 현재 표시된 행이 모두 선택된 경우에만 체크 표시 */}
              <input
                type="checkbox"
                checked={
                  rows.length > 0 && rowSelectedIds.length === rows.length
                }
                onChange={onToggleSelectAll}
              />
            </th>
            <th>번호</th>
            {showTabColumn && <th>탭</th>}
            <th>카테고리</th>
            <th>제목</th>
            <th>작성자</th>
            <th>조회수</th>
            <th>작성일</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={showTabColumn ? 9 : 8}>게시글이 없습니다</td>
            </tr>
          ) : (
            rows.map((item, idx) => {
              // 최신 글이 가장 큰 번호를 갖도록 계산 (전체 개수 - 이전 페이지들 - 현재 행 순서)
              const displayNo = rowTotalElements - rowPage * rowSize - idx;
              return (
                <tr key={item.id}>
                  <td>
                    <input
                      type="checkbox"
                      checked={rowSelectedIds.includes(item.id)}
                      onChange={() => onToggleSelect(item.id)}
                    />
                  </td>
                  <td>{displayNo}</td>
                  {showTabColumn && <td>{item.tabName}</td>}
                  <td>{item.categoryName}</td>
                  <td
                    className="admin-title-cell"
                    onClick={() => setDetailId(item.id)} // 제목 클릭 시 상세보기 모달 오픈
                  >
                    {/* {item.thumbnail ? (
                      <img
                        className="board-item-thumb"
                        src={item.thumbnail}
                        alt=""
                      />
                    ) : (
                      <div className="board-item-thumb board-item-thumb-empty" />
                    )} */}
                    {item.title}
                  </td>
                  <td>{item.userName}</td>
                  <td>{item.hit}</td>
                  <td>{item.createTime?.split("T")[0] || ""}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => handleDeleteOne(item.id)}
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    );
  };

  return (
    <div className="comMain">
      <div className="comMain-wrap">
        <h1>게시글 관리</h1>
        <div className="write">
          {/* 공지사항 작성 버튼: adminOnly 탭이 있어야 작성 모달이 열림 */}
          <button type="button" onClick={noticeWrite}>
            공지사항 작성
          </button>
        </div>

        {/* 필터 영역: 탭 선택, 카테고리 선택, 키워드 검색 */}
        <div className="admin-filter-row">
          <select
            name="tabId"
            value={filters.tabId}
            onChange={handleFilterChange}
          >
            <option value="">탭별로 보기 (전체)</option>
            {tabs.map((tab) => (
              <option key={tab.id} value={tab.id}>
                {tab.tabName}
              </option>
            ))}
          </select>

          <select
            name="categoryId"
            value={filters.categoryId}
            onChange={handleFilterChange}
            disabled={!filters.tabId} // 탭을 선택해야 카테고리 필터 활성화
          >
            <option value="">전체 카테고리</option>
            {filteredCategories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.categoryName}
              </option>
            ))}
          </select>

          <input
            type="text"
            name="keyword"
            placeholder="제목 또는 작성자 검색"
            value={filters.keyword}
            onChange={handleFilterChange}
            onKeyDown={handleKeyDown}
          />

          <button type="button" onClick={handleSearch}>
            검색
          </button>
        </div>

        {isSingleTabMode ? (
          <>
            {/* ---------- 단일 탭 모드: 특정 탭을 선택했을 때의 화면 ---------- */}
            <div className="admin-action-row">
              <span>
                총 {totalElements}건 중 {selectedIds.length}건 선택
              </span>
              <button
                type="button"
                onClick={handleDeleteSelected}
                disabled={selectedIds.length === 0}
              >
                선택 삭제
              </button>
            </div>

            {renderTable({
              rows: list,
              isLoading,
              selectedIds,
              onToggleSelect: toggleSelect,
              onToggleSelectAll: toggleSelectAll,
              showTabColumn: false,
              totalElements,
              page,
              size,
            })}

            {/* 페이지가 2개 이상일 때만 페이지네이션 컴포넌트 표시 */}
            {totalPages > 1 && (
              <PageGenerate
                currentPage={page}
                startPage={startPage}
                endPage={endPage}
                totalPage={totalPages}
                onPageChange={handlePageChange}
                search={filters.keyword}
                subject={filters.tabId}
              />
            )}
          </>
        ) : (
          <>
            {/* ---------- 탭별 그룹 모드: 탭 미선택 시 기본 화면, 탭마다 섹션을 나눠 표시 ---------- */}
            {tabs.map((tab) => {
              // 아직 데이터가 없는 탭은 기본값(빈 목록, 로딩 중)으로 처리
              const group = groupedByTab[tab.id] || {
                list: [],
                page: 0,
                totalPages: 0,
                totalElements: 0,
                isLoading: true,
              };

              // 해당 탭 섹션의 페이지네이션 그룹(버튼 묶음) 계산
              const groupCurrentGroup = Math.floor(group.page / pageGroupSize);
              const groupStartPage = groupCurrentGroup * pageGroupSize + 1;
              const groupEndPage = Math.min(
                groupStartPage + pageGroupSize - 1,
                group.totalPages,
              );

              return (
                <section key={tab.id} className="admin-tab-section">
                  <div className="admin-tab-section-header">
                    <h2>
                      {tab.tabName}{" "}
                      <span className="admin-tab-count">
                        ({group.totalElements}건)
                      </span>
                    </h2>
                    <div className="admin-action-row">
                      <span>{group.selectedIds?.length || 0}건 선택</span>
                      <button
                        type="button"
                        onClick={() => handleGroupDeleteSelected(tab.id)}
                        disabled={!group.selectedIds?.length}
                      >
                        선택 삭제
                      </button>
                    </div>
                  </div>

                  {renderTable({
                    rows: group.list,
                    isLoading: group.isLoading,
                    selectedIds: group.selectedIds || [],
                    onToggleSelect: (id) => toggleGroupSelect(tab.id, id),
                    onToggleSelectAll: (e) => toggleGroupSelectAll(tab.id, e),
                    showTabColumn: false,
                    totalElements: group.totalElements,
                    page: group.page,
                    size: GROUP_SIZE,
                  })}

                  {/* 이 탭의 게시글이 GROUP_SIZE를 넘으면 탭별 페이지네이션 표시 */}
                  {group.totalPages > 1 && (
                    <PageGenerate
                      currentPage={group.page}
                      startPage={groupStartPage}
                      endPage={groupEndPage}
                      totalPage={group.totalPages}
                      onPageChange={(search, subject, newPage) =>
                        handleGroupPageChange(tab.id, newPage)
                      }
                      search={filters.keyword}
                      subject={tab.id}
                    />
                  )}
                </section>
              );
            })}
          </>
        )}

        <div className="tabList">
          {/* 탭 관리(생성/수정/삭제 등)를 위한 하위 컴포넌트 */}
          <TabList />
        </div>
      </div>

      {/* 공지사항 작성 모달: writeTabId가 세팅되면 오픈 */}
      {writeTabId && (
        <AdminNoticeWrite
          tabId={writeTabId}
          tabs={tabs}
          categories={categories}
          onClose={() => setWriteTabId(null)}
          onSuccess={() => {
            setWriteTabId(null);
            refreshAfterDelete(); // 작성 성공 시 목록 갱신
          }}
        />
      )}

      {/* 게시글 상세보기 모달: detailId가 세팅되면 오픈 */}
      {detailId && (
        <AdminCommunityDetail
          id={detailId}
          onClose={() => setDetailId(null)}
          onDeleted={() => {
            setDetailId(null);
            refreshAfterDelete(); // 상세보기에서 삭제 시 목록 갱신
          }}
        />
      )}
    </div>
  );
};

export default AdminCommunity;
