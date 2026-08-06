import React, { useEffect, useState } from "react";
import jwtAxios from "../../../apis/util/jwtUtil";
import "../../../css/shop/subscription/membership.css";

const Membership = () => {
  const [memberships, setMemberships] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL"); // ALL, GYM, PT

  useEffect(() => {
    getMembershipList();
  }, []);

  // 보유 이용권 목록 조회
  const getMembershipList = async () => {
    try {
      const res = await jwtAxios.get("/api/member-products/my");
      console.log("보유 이용권 데이터:", res.data);
      setMemberships(res.data || []);
    } catch (err) {
      console.error("이용권 조회 실패:", err);
    
      console.log("err 자체:", err);
      console.log("response:", err?.response);
      console.log("message:", err?.message);
    }finally {
      setLoading(false);
    }
  };

  // 날짜 포맷팅 (YYYY-MM-DD)
  const formatDate = (dateStr) => {
    if (!dateStr) return "-";
    return dateStr.split("T")[0];
  };

  // D-Day 계산 함수
  const getDDay = (endDateStr) => {
    if (!endDateStr) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const endDate = new Date(endDateStr);
    endDate.setHours(0, 0, 0, 0);

    const diffTime = endDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 0) return "EXPIRED";
    if (diffDays === 0) return "D-Day";
    return `D-${diffDays}`;
  };

  // 상태 뱃지 렌더링
  const renderStatusBadge = (status, endDate) => {
    const dDay = getDDay(endDate);

    if (status === "EXPIRED" || dDay === "EXPIRED") {
      return <span className="status-tag status-expired">만료됨</span>;
    }
    if (status === "PAUSED") {
      return <span className="status-tag status-paused">일시정지</span>;
    }
    return <span className="status-tag status-active">이용 중 ({dDay})</span>;
  };

  // 필터링된 이용권 목록
  const filteredMemberships = memberships.filter((item) => {
    if (filter === "ALL") return true;
    return item.productType === filter; // GYM or PT
  });

  if (loading) {
    return (
      <div className="membership-page">
        <div className="membership-loading">
          <div className="spinner"></div>
          <p>보유 중인 이용권을 불러오는 중입니다...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="membership-page">
      <div className="page-header">
        <h2>내 보유 이용권</h2>
        <p className="sub-title">현재 보유 및 이용 중인 헬스장 회원권과 PT 이용권입니다.</p>
      </div>

      {/* 필터 탭 (전체 / 헬스장 / PT) */}
      <div className="tab-group">
        <button
          className={`tab-btn ${filter === "ALL" ? "active" : ""}`}
          onClick={() => setFilter("ALL")}
        >
          전체 ({memberships.length})
        </button>
        <button
          className={`tab-btn ${filter === "GYM" ? "active" : ""}`}
          onClick={() => setFilter("GYM")}
        >
          🏋️‍♂️ 헬스장 ({memberships.filter((m) => m.productType === "GYM").length})
        </button>
        <button
          className={`tab-btn ${filter === "PT" ? "active" : ""}`}
          onClick={() => setFilter("PT")}
        >
          💪 PT 이용권 ({memberships.filter((m) => m.productType === "PT").length})
        </button>
      </div>

      {/* 이용권 리스트 */}
      {filteredMemberships.length === 0 ? (
        <div className="empty-membership">
          <p>보유 중인 이용권이 없습니다.</p>
        </div>
      ) : (
        <div className="membership-grid">
          {filteredMemberships.map((item) => {
            const isGym = item.productType === "GYM";
            return (
              <div key={item.id} className={`membership-card ${isGym ? "card-gym" : "card-pt"}`}>
                <div className="card-top">
                  <span className="type-badge">{isGym ? "헬스장 회원권" : "개인 PT"}</span>
                  {renderStatusBadge(item.status, item.endDate)}
                </div>

                <div className="card-main">
                  <h3 className="item-title">{item.title}</h3>
                  <p className="gym-name">📍 {item.gymName || "FitMate 피트니스 Center"}</p>
                </div>

                <div className="card-info-box">
                  {/* PT 수강권일 경우 잔여 횟수 표시 */}
                  {!isGym && (
                    <div className="info-row highlight-row">
                      <span className="label">잔여 횟수</span>
                      <span className="value count-text">
                        <strong>{item.remainingCount ?? 0}</strong> / {item.totalCount ?? 0} 회
                      </span>
                    </div>
                  )}

                  <div className="info-row">
                    <span className="label">이용 기간</span>
                    <span className="value">
                      {formatDate(item.startDate)} ~ {formatDate(item.endDate)}
                    </span>
                  </div>

                  {item.trainerName && (
                    <div className="info-row">
                      <span className="label">담당 트레이너</span>
                      <span className="value">{item.trainerName} 트레이너</span>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Membership;