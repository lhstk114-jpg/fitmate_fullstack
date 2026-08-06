import { useState } from "react";
import RoutineForm from "../../components/exercise/RoutineForm";
import HistoryList from "../../components/exercise/HistoryList";
import RoutineResult from "../../components/exercise/RoutineResult";
import "../../css/Community/RoutinePage.css";

/**
 * 운동 루틴 생성 페이지 (라우트 진입점)
 * - 왼쪽: 조건 선택 폼(RoutineForm) → 부위/장비 선택 후 루틴 생성
 * - 가운데: 생성/선택된 루틴 결과(RoutineResult)
 * - 오른쪽: 최근 생성 히스토리(HistoryList) → 클릭 시 해당 루틴을 결과 영역에 다시 표시
 * 세 컴포넌트 사이의 상태(현재 결과, 히스토리 갱신 트리거)를 이 페이지가 관리(상태 끌어올리기)
 */
export default function RoutinePage() {
  const [result, setResult] = useState(null); // 현재 화면에 표시 중인 루틴 결과 (새로 생성됐거나 히스토리에서 선택된 것)
  const [history, setHistory] = useState(0); // 값이 바뀔 때마다 HistoryList가 재조회하도록 하는 트리거 카운터

  // RoutineForm에서 루틴 생성이 완료되면 호출됨: 결과를 표시하고 히스토리 목록을 갱신
  function handleGenerated(newResult) {
    setResult(newResult);
    setHistory((h) => h + 1); // 카운터 증가 → HistoryList의 useEffect가 재실행되어 방금 생성한 루틴이 목록에 반영됨
  }

  return (
    <div className="routine-page">
      {/* 조건 선택 후 생성 버튼 클릭 시 handleGenerated 호출 */}
      <RoutineForm onGenerated={handleGenerated} />
      {/* 현재 result를 그대로 표시 (없으면 빈 상태 화면) */}
      <RoutineResult result={result} />
      {/* 히스토리 항목 클릭 시 곧바로 setResult로 결과 영역에 반영, selectedId로 현재 선택 항목 하이라이트 */}
      <HistoryList
        onSelect={setResult}
        refreshKey={history}
        selectedId={result?.id}
      />
    </div>
  );
}
