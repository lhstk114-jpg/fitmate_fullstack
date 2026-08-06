import axios from "axios";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import { getCookie } from "../../apis/util/cookieUtil";
import TiptapEditor from "./TiptapEditor";

/**
 * 게시글 수정 페이지
 * - URL 파라미터의 id로 기존 게시글 데이터를 불러와 폼에 채워넣고 수정 요청을 보냄
 * - CommunityInsert와 유사하지만, 신규 작성이 아닌 기존 데이터 로딩 후 수정하는 흐름
 */
const CommunityUpdate = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [tabs, setTabs] = useState([]); // 전체 탭 목록
  const [categories, setCategories] = useState([]); // 전체 카테고리 목록
  const [community, setCommunity] = useState({
    title: "",
    content: "",
    tabId: "",
    categoryId: "",
    attachFile: "",
  });
  const [isLoading, setIsLoading] = useState(true);

  // 게시글 데이터 + 탭/카테고리 목록을 병렬로 조회
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [tabRes, catRes, detailRes] = await Promise.all([
          axios.get(`${API_SERVER_URL}/api/community/tabList`),
          axios.get(`${API_SERVER_URL}/api/community/category`),
          jwtAxios.get(`${API_SERVER_URL}/api/community/detail/${id}`),
        ]);
        setTabs(tabRes.data.result);
        setCategories(catRes.data.result);
        // tabId/categoryId는 select value 비교를 위해 문자열로 통일
        setCommunity({
          ...detailRes.data.community,
          tabId: String(detailRes.data.community.tabId),
          categoryId: String(detailRes.data.community.categoryId),
        });
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [id]);

  // 현재 선택된 탭(community.tabId)에 속한 카테고리만 필터링
  const filteredCategories = useMemo(
    () =>
      categories.filter((cat) => String(cat.tabId) === String(community.tabId)),
    [categories, community.tabId],
  );

  // 탭/카테고리 select 변경 핸들러 (권한 검증 포함, CommunityInsert와 동일한 로직)
  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === "tabId") {
      const targetTab = tabs.find((t) => String(t.id) === String(value));
      const isAdmin = getCookie("member")?.role === "ADMIN";

      // 관리자 전용 탭(공지사항)은 일반 사용자가 선택할 수 없도록 차단
      if (targetTab?.adminOnly && !isAdmin) {
        alert("공지사항은 관리자만 작성할 수 있습니다.");
        return;
      }
      // 탭이 바뀌면 카테고리는 초기화 (이전 탭의 카테고리가 남아있지 않도록)
      setCommunity((prev) => ({ ...prev, tabId: value, categoryId: "" }));
    } else if (name === "categoryId") {
      const targetCat = categories.find((c) => String(c.id) === String(value));
      const isAdmin = getCookie("member")?.role === "ADMIN";

      // FAQ 카테고리는 관리자만 선택 가능
      if (targetCat?.categoryName === "FAQ" && !isAdmin) {
        alert("FAQ는 관리자만 작성할 수 있습니다.");
        return;
      }
      setCommunity((prev) => ({ ...prev, categoryId: value }));
    } else {
      setCommunity((prev) => ({ ...prev, [name]: value }));
    }
  };

  // 수정 요청: 필수값 검증 후 PUT 요청으로 게시글 갱신
  const getCommunityUpdate = async (e) => {
    e.preventDefault();
    if (!community.tabId) {
      alert("탭을 선택해주세요");
      return;
    }
    if (!community.categoryId) {
      alert("카테고리를 선택해주세요");
      return;
    }
    if (!community.title) {
      alert("제목을 입력해주세요");
      return;
    }
    if (!community.content) {
      alert("내용을 작성해주세요");
      return;
    }

    try {
      await jwtAxios.put(
        `${API_SERVER_URL}/api/community/update/${id}`,
        community,
      );
      alert("수정되었습니다.");
      // 수정 완료 후 해당 게시글 상세 페이지로 이동
      navigate(`/community/detail/${id}`);
    } catch (error) {
      alert("수정 실패");
    }
  };

  // 초기 데이터 로딩 중에는 폼 대신 로딩 문구만 표시
  if (isLoading) return <div>로딩중...</div>;

  return (
    <div className="communityUpdate">
      <h1>게시글 수정</h1>
      <form onSubmit={getCommunityUpdate} className="insert-form">
        {/* 작성자명은 수정 불가 (읽기 전용) */}
        <div className="form-group">
          <label>작성자</label>
          <input name="userName" value={community.userName} readOnly />
        </div>

        {/* 탭 선택 */}
        <div className="select-group">
          <label>탭 선택</label>
          <select name="tabId" value={community.tabId} onChange={handleChange}>
            <option value="">탭을 선택하세요</option>
            {tabs.map((tab) => (
              <option key={tab.id} value={tab.id}>
                {tab.tabName} {tab.adminOnly ? "(관리자 전용)" : ""}
              </option>
            ))}
          </select>

          {/* 카테고리 선택 (선택된 탭에 속한 카테고리만 노출) */}
          <label>카테고리 선택</label>
          <select
            name="categoryId"
            value={community.categoryId}
            onChange={handleChange}
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
        <div className="form-group">
          <label>제목</label>
          <input
            name="title"
            placeholder="제목을 입력하세요"
            value={community.title}
            onChange={handleChange}
          />
        </div>

        {/* 본문 수정용 에디터: 기존 content를 초기값으로 전달 */}
        <TiptapEditor
          value={community.content}
          onChange={(html) =>
            setCommunity((prev) => ({ ...prev, content: html }))
          }
        />

        <button type="submit" className="submit-btn">
          수정
        </button>
        <button type="button" onClick={() => navigate(-1)}>
          취소
        </button>
      </form>
    </div>
  );
};

export default CommunityUpdate;
