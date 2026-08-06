import { useState } from "react";
import { API_SERVER_URL } from "../../apis/commonApi";

/**
 * 생성된 운동 루틴 결과를 보여주는 패널
 * - 부모(RoutinePage 등)에서 생성/선택된 루틴 결과(result)를 받아 운동 목록을 렌더링
 * - 각 운동 카드마다 "사진 보기" 토글로 동작 GIF 이미지를 지연 표시(lazy)할 수 있음
 * props:
 * - result: 루틴 데이터 { nameKo, exerciseDetails: [...] } (없으면 빈 상태 화면 표시)
 */
export default function RoutineResult({ result }) {
  // 각 운동 카드의 사진 보기 토글 상태를 관리 (운동 인덱스 `i`를 키로 사용)
  const [showImageIndices, setShowImageIndices] = useState(new Set());

  // 아직 생성/선택된 루틴이 없는 경우 안내 문구만 표시
  if (!result) {
    return (
      <div className="panel result-panel empty-state">
        <p className="eyebrow">02 — 결과</p>
        <h2 className="panel-title">아직 비어있음</h2>
        <p className="muted">왼쪽에서 부위를 고르고 루틴을 생성.</p>
      </div>
    );
  }

  // 특정 운동의 사진 보기 토글 핸들러 (Set에 인덱스가 있으면 제거, 없으면 추가)
  const toggleImage = (index) => {
    setShowImageIndices((prev) => {
      const next = new Set(prev);
      if (next.has(index)) {
        next.delete(index);
      } else {
        next.add(index);
      }
      return next;
    });
  };

  return (
    <div className="panel result-panel">
      <p className="eyebrow">02 — 결과</p>
      <h2 className="panel-title">{result.nameKo} 루틴</h2>

      <ol className="exercise-list">
        {result.exerciseDetails?.map((ex, i) => {
          const isImageVisible = showImageIndices.has(i);

          return (
            <li key={i} className="exercise-card">
              <div className="exercise-info" style={{ width: "100%" }}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  {/* 세트 번호 (1부터 시작, 2자리로 패딩: 01, 02 ...) */}
                  <span className="exercise-index">
                    SET {String(i + 1).padStart(2, "0")}
                  </span>

                  {/* 사진 보기 / 닫기 토글 버튼: 운동 id가 있는 경우에만 노출 (이미지가 있는 운동만) */}
                  {ex.id && (
                    <button
                      type="button"
                      className="toggle-btn"
                      onClick={() => toggleImage(i)}
                      style={{
                        cursor: "pointer",
                        padding: "4px 8px",
                        fontSize: "12px",
                      }}
                    >
                      {isImageVisible ? "사진 닫기 🔼" : "사진 보기 🔽"}
                    </button>
                  )}
                </div>

                <h3>{ex.name}</h3>
                <p className="muted">
                  {ex.target} · {ex.equipment}
                </p>
                {/* 반복 횟수 / 세트 수 / 휴식 시간 배지 */}
                <div className="badge-row">
                  <span className="badge">{ex.reps}회, </span>
                  <span className="badge">{ex.sets}세트, </span>
                  <span className="badge">{ex.restSeconds}초 휴식</span>
                </div>

                {/* '사진 보기'를 눌렀을 때만 나타나는 GIF 이미지 영역 (loading="lazy"로 필요 시점에 로드) */}
                {isImageVisible && ex.id && (
                  <div style={{ marginTop: "12px", textAlign: "center" }}>
                    <img
                      className="exercise-gif"
                      src={`${API_SERVER_URL}/upload/exercise/${String(
                        ex.id,
                      ).padStart(4, "0")}_360.gif`}
                      alt={ex.name}
                      loading="lazy"
                      style={{
                        maxWidth: "100%",
                        borderRadius: "8px",
                      }}
                      onError={(e) => {
                        console.error("이미지 로드 실패:", e.currentTarget.src);
                      }}
                    />
                  </div>
                )}
              </div>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
