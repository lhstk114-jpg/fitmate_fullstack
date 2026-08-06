import { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";

/**
 * 운동 루틴 생성 조건 선택 폼
 * - 타겟 부위 선택 → 해당 부위에서 사용 가능한 장비 목록을 다시 조회 → 부위+장비 조건으로 루틴 생성 요청
 * - 생성 성공 시 결과를 onGenerated 콜백으로 부모에 전달 (부모가 RoutineResult에 표시)
 * props:
 * - onGenerated: (result) => void, 루틴 생성 성공 시 호출되는 콜백
 */
export default function RoutineForm({ onGenerated }) {
  const [targetMap, setTargetMap] = useState({}); // 타겟 부위 옵션 { 영문키: 한글명 }
  const [equipMap, setEquipMap] = useState({}); // 선택된 부위에 대한 장비 옵션 { 영문키: 한글명 }
  const [target, setTarget] = useState(""); // 현재 선택된 타겟 부위
  const [equip, setEquip] = useState(""); // 현재 선택된 장비 (빈 값이면 전체)
  const [loading, setLoading] = useState(false); // 루틴 생성 요청 진행 상태
  const [error, setError] = useState(null);

  // 마운트 시 타겟 부위 옵션 목록을 조회하고, 첫 번째 항목을 기본 선택값으로 세팅
  useEffect(() => {
    jwtAxios
      .get(`${API_SERVER_URL}/api/exercise/options`)
      .then((res) => {
        const targets = res.data.targets || {};
        setTargetMap(targets);

        const targetKeys = Object.keys(targets);
        if (targetKeys.length) {
          setTarget(targetKeys[0]);
        }
      })
      .catch((e) => setError(e.message));
  }, []);

  // 선택된 타겟 부위(target)가 바뀔 때마다 해당 부위에서 사용 가능한 장비 목록을 재조회
  useEffect(() => {
    if (!target) {
      setEquipMap({});
      setEquip("");
      return;
    }

    jwtAxios
      .get(`${API_SERVER_URL}/api/exercise/options/equipments`, {
        params: { target },
      })
      .then((res) => {
        setEquipMap(res.data || {});
        setEquip(""); // 부위가 바뀌면 이전에 선택했던 장비는 초기화(전체로 리셋)
      })
      .catch((e) => {
        console.error("장비 목록 로드 실패:", e);
        setEquipMap({});
      });
  }, [target]);

  // 부위/장비 조건으로 서버에 루틴 생성을 요청하는 API 호출 함수
  const generateRoutine = async ({ target, equip }) => {
    const res = await jwtAxios.post(
      `${API_SERVER_URL}/api/exercise/recommend`,
      { target, equip },
    );
    return res.data;
  };

  // 폼 제출 핸들러: 루틴 생성 요청 후 성공 시 결과를 부모에 전달, 실패 시 에러 메시지 파싱해 표시
  async function handleSubmit(e) {
    e.preventDefault();
    if (!target) return;
    setLoading(true);
    setError(null);
    try {
      const result = await generateRoutine({ target, equip });
      onGenerated(result);
    } catch (e) {
      console.error("에러 객체 확인:", e);

      let errorMessage = "알 수 없는 오류가 발생했습니다.";

      // 서버 응답 형태(문자열/객체)에 따라 적절한 에러 메시지를 추출
      if (e.response?.data) {
        if (typeof e.response.data === "string") {
          errorMessage = e.response.data;
        } else if (typeof e.response.data === "object") {
          errorMessage =
            e.response.data.message ||
            e.response.data.error ||
            JSON.stringify(e.response.data);
        }
      } else if (e.message) {
        errorMessage = e.message;
      }

      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="panel form-panel" onSubmit={handleSubmit}>
      <p className="eyebrow">01 — 조건 선택</p>
      <h2 className="panel-title">오늘 뭐 할까</h2>

      {/* 운동 부위 선택 */}
      <label className="field">
        <span>타겟 부위</span>
        <select value={target} onChange={(e) => setTarget(e.target.value)}>
          {Object.keys(targetMap).map((engKey) => (
            <option key={engKey} value={engKey}>
              {targetMap[engKey]}
            </option>
          ))}
        </select>
      </label>

      {/* 부위에 해당하는 장비만 선택 옵션으로 노출 (선택 안 하면 전체 장비 포함) */}
      <label className="field">
        <span>장비 (선택)</span>
        <select value={equip} onChange={(e) => setEquip(e.target.value)}>
          <option value="">전체 (모든 장비 포함)</option>
          {Object.keys(equipMap).map((engKey) => (
            <option key={engKey} value={engKey}>
              {equipMap[engKey]}
            </option>
          ))}
        </select>
      </label>

      {error && <p className="error-text">{error}</p>}

      {/* 생성 중이거나 부위가 선택되지 않았으면 버튼 비활성화 */}
      <button
        type="submit"
        className="btn-primary"
        disabled={loading || !target}
      >
        {loading ? "조합하는 중…" : "루틴 생성"}
      </button>
    </form>
  );
}
