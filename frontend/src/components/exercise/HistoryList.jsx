import { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";

/**
 * 최근 5개까지만 저장되도록 백엔드에서 제한하므로 페이지네이션은 없애다.
 * - refreshKey: 부모(RoutinePage)가 새 루틴 생성 시 값을 올려서 즉시 재조회를 트리거.
 * - selectedId: 부모가 현재 RoutineResult에 표시 중인 항목의 id를 내려줘서
 *   하이라이트 상태를 별도 내부 state 없이 그대로 반영.
 * props:
 * - onSelect: 히스토리 항목 클릭 시 호출되는 콜백 (해당 루틴을 RoutineResult에 표시하기 위함)
 * - refreshKey: 값이 바뀔 때마다 목록을 재조회하는 트리거
 * - selectedId: 현재 선택(표시 중)된 루틴의 id, 목록에서 하이라이트 표시에 사용
 */
export default function HistoryList({ onSelect, refreshKey, selectedId }) {
  const [items, setItems] = useState([]); // 최근 루틴 히스토리 목록 (최대 5개)
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  // refreshKey가 바뀔 때마다(새 루틴 생성 시 등) 최근 5개 히스토리를 재조회
  useEffect(() => {
    setLoading(true);
    jwtAxios
      .get(`${API_SERVER_URL}/api/exercise/history`, {
        params: { page: 0, size: 5 },
      })
      .then((res) => setItems(res.data || []))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [refreshKey]);

  // 날짜 문자열을 "YYYY.MM.DD" 형태로 변환
  function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(
      d.getDate(),
    ).padStart(2, "0")}`;
  }

  return (
    <div className="panel history-panel">
      <p className="eyebrow">03 — 히스토리 (최근 5개)</p>
      <h2 className="panel-title">지난 루틴</h2>

      {error && <p className="error-text">{error}</p>}
      {loading && <p className="muted">불러오는 중…</p>}
      {!loading && items.length === 0 && (
        <p className="muted">아직 생성한 루틴이 없습니다.</p>
      )}

      <ul className="history-list">
        {items.map((item) => {
          // 해당 루틴에 포함된 운동 이름들을 콤마로 이어 붙여 미리보기 텍스트로 사용
          const exerciseNames =
            item.exerciseDetails?.map((d) => d.name).join(", ") ||
            "운동 정보 없음";
          // 부모가 내려준 selectedId와 비교해 현재 선택된 항목인지 판단 (내부 state 없이 반영)
          const isSelected = item.id === selectedId;

          return (
            <li key={item.id}>
              <button
                type="button"
                className={`history-card${isSelected ? " selected" : ""}`}
                onClick={() => onSelect?.(item)}
                style={{
                  width: "100%",
                  textAlign: "left",
                  background: "none",
                  border: "1px solid #eef1f5",
                }}
              >
                <div
                  className="history-item-head"
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "6px",
                  }}
                >
                  <strong className="history-title" style={{ margin: 0 }}>
                    {item.nameKo || item.name}
                  </strong>
                  <time className="history-date muted">
                    {formatDate(item.createTime)}
                  </time>
                </div>
                <div className="history-content">{exerciseNames}</div>
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
