import React, { lazy, Suspense } from "react";
import { Navigate } from "react-router-dom";
const Loading = <div className="loading">...Loading</div>;

// 각 페이지 컴포넌트는 lazy()로 지연 로딩 (실제 진입 전까지 번들을 불러오지 않아 초기 로딩 속도 개선)
const CommunityInsertPage = lazy(
  () => import("../page/community/CommunityInsertPage"),
);
const CommunityListPage = lazy(
  () => import("../page/community/CommunityListPage"),
);
const CommunityDetailPage = lazy(
  () => import("../page/community/CommunityDetailPage"),
);
const CommunityUpdatePage = lazy(
  () => import("../page/community/CommunityUpdatePage"),
);
const TabListPage = lazy(() => import("../page/community/TabListPage"));
const RoutinePage = lazy(() => import("../page/community/RoutinePage"));
const CommunityMainPage = lazy(
  () => import("../page/community/CommunityMainPage"),
);

/**
 * 커뮤니티 관련 하위 라우트 정의
 * - 상위 라우터(App 등)에서 "/community/*" 경로 아래에 이 배열을 연결해 사용하는 형태로 추정
 * - 모든 페이지는 Suspense로 감싸 lazy 로딩 중 Loading 문구를 표시
 */
const toCommunityRouter = () => {
  return [
    {
      // "/community" 루트 진입 시 기본적으로 "/community/index"(메인 페이지)로 리다이렉트
      path: "",
      element: <Navigate replace to={"index"} />,
    },
    {
      // 커뮤니티 메인(홈) 페이지: 날씨/추천운동/탭별 게시글 미리보기 등
      path: "index",
      element: (
        <Suspense fallback={Loading}>
          <CommunityMainPage />
        </Suspense>
      ),
    },
    {
      // 게시글 작성 페이지
      path: "insert",
      element: (
        <Suspense fallback={Loading}>
          <CommunityInsertPage />
        </Suspense>
      ),
    },
    {
      // 게시글 상세 페이지 (id로 특정 게시글 조회)
      path: "detail/:id",
      element: (
        <Suspense fallback={Loading}>
          <CommunityDetailPage />
        </Suspense>
      ),
    },
    {
      // 게시글 수정 페이지 (id로 기존 게시글 불러와 수정)
      path: "update/:id",
      element: (
        <Suspense fallback={Loading}>
          <CommunityUpdatePage />
        </Suspense>
      ),
    },

    {
      // 전체 게시글 목록 페이지 (탭/카테고리 필터 없음 → CommunityListPage에서 params.tabId/categoryId가 null로 처리됨)
      path: "communityList",
      element: (
        <Suspense fallback={Loading}>
          <CommunityListPage />
        </Suspense>
      ),
    },
    {
      // 관리자용 탭 목록 페이지 - 특정 탭 id로 진입 (탭 상세로 필터링해서 보고 싶을 때 사용하는 것으로 추정)
      path: "tabList/:tabId",
      element: (
        <Suspense fallback={Loading}>
          <TabListPage />
        </Suspense>
      ),
    },
    {
      // 관리자용 탭 목록 페이지 - 전체 탭 조회
      path: "tabList",
      element: (
        <Suspense fallback={Loading}>
          <TabListPage />
        </Suspense>
      ),
    },

    {
      // 운동 루틴 생성 페이지 (RoutineForm + RoutineResult + HistoryList)
      path: "routine",
      element: (
        <Suspense fallback={Loading}>
          <RoutinePage />
        </Suspense>
      ),
    },
    {
      // 특정 탭의 게시글 목록 (CommunityListPage가 useParams로 tabId를 읽어 필터링)
      path: "tab/:tabId",
      element: (
        <Suspense fallback={Loading}>
          <CommunityListPage />
        </Suspense>
      ),
    },
    {
      // 특정 탭 + 특정 카테고리의 게시글 목록 (CommunityListPage가 tabId/categoryId 둘 다 읽어 필터링)
      path: "tab/:tabId/category/:categoryId",
      element: (
        <Suspense fallback={Loading}>
          <CommunityListPage />
        </Suspense>
      ),
    },
  ];
};

export default toCommunityRouter;
