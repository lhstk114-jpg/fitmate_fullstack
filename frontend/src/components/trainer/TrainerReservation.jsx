import React, { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/trainer/trainerReservation.css";
import TrainerMemberModal from "./TrainerMemberModal";

const TrainerReservation = () => {
  //김우송 추가 -> 카드 선택 시 상세정보 모달 띄울수있게
  //boolean값 추가
  const [isBool, setIsBool] = useState(false);
  //Modal창에 넣을 id값 저장
  const [modalMemberId, setModalMemberId] = useState(false);
  //modal창 함수(memberId를 받음)
  const memberModalFn = (memberId) => {
    setIsBool(true);
    setModalMemberId(memberId);
  };

  const [reservations, setReservations] = useState([]);
  const [filter, setFilter] = useState("ALL"); // ALL, RESERVED, COMPLETE, CANCEL

  useEffect(() => {
    getReservationList();
  }, []);

  // 예약 목록 조회
  const getReservationList = async () => {
    try {
      const res = await jwtAxios.get("/api/reservations/trainer");
      setReservations(res.data || []);
    } catch (error) {
      console.error(error);
    }
  };

  // 예약 상태 변경
  const changeStatus = async (id, status) => {
    const actionText = status === "COMPLETE" ? "수업을 완료" : "예약을 취소";
    if (!window.confirm(`정말 해당 예약을 ${actionText}하시겠습니까?`)) return;

    try {
      await jwtAxios.put(`/api/reservations/${id}`, {
        reservationStatus: status,
      });

      alert(
        `예약이 ${status === "COMPLETE" ? "완료" : "취소"} 처리되었습니다.`,
      );
      getReservationList();
    } catch (error) {
      console.error(error);
      alert("상태 변경 중 오류가 발생했습니다.");
    }
  };

  // 뱃지 텍스트 및 클래스 매핑
  const getStatusBadge = (status) => {
    switch (status) {
      case "RESERVED":
        return <span className="status-badge status-reserved">예약 완료</span>;
      case "COMPLETE":
        return <span className="status-badge status-complete">수업 완료</span>;
      case "CANCEL":
        return <span className="status-badge status-cancel">예약 취소</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  // 날짜/시간 포맷팅 함수
  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return "-";
    const date = new Date(dateTimeStr);
    if (isNaN(date.getTime())) return dateTimeStr;

    const days = ["일", "월", "화", "수", "목", "금", "토"];
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const dayOfWeek = days[date.getDay()];
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");

    return `${year}년 ${month}월 ${day}일 (${dayOfWeek}) ${hours}:${minutes}`;
  };

  // 통계 계산
  const stats = {
    total: reservations.length,
    reserved: reservations.filter((r) => r.reservationStatus === "RESERVED")
      .length,
    complete: reservations.filter((r) => r.reservationStatus === "COMPLETE")
      .length,
    cancel: reservations.filter((r) => r.reservationStatus === "CANCEL").length,
  };

  // 필터링된 리스트
  const filteredReservations = reservations.filter((r) => {
    if (filter === "ALL") return true;
    return r.reservationStatus === filter;
  });

  return (
    <>
      {isBool && (
        <TrainerMemberModal
          setIsBool={setIsBool}
          modalMemberId={modalMemberId}
        />
      )}
      <div className="trainer-reservation-container">
        {/* 타이틀 영역 */}
        <div className="page-header">
          <h2>PT 수업 예약 관리</h2>
          <p className="subtitle">
            회원님들이 신청한 PT 예약 및 수업 일정을 관리합니다.
          </p>
        </div>

        {/* 대시보드 요약 카드 */}
        <div className="stats-grid">
          <div
            className={`stat-card ${filter === "ALL" ? "active" : ""}`}
            onClick={() => setFilter("ALL")}
          >
            <span className="stat-label">전체 예약</span>
            <span className="stat-value">{stats.total}</span>
          </div>
          <div
            className={`stat-card ${filter === "RESERVED" ? "active" : ""}`}
            onClick={() => setFilter("RESERVED")}
          >
            <span className="stat-label reserved">대기/예약중</span>
            <span className="stat-value text-blue">{stats.reserved}</span>
          </div>
          <div
            className={`stat-card ${filter === "COMPLETE" ? "active" : ""}`}
            onClick={() => setFilter("COMPLETE")}
          >
            <span className="stat-label complete">수업 완료</span>
            <span className="stat-value text-green">{stats.complete}</span>
          </div>
          <div
            className={`stat-card ${filter === "CANCEL" ? "active" : ""}`}
            onClick={() => setFilter("CANCEL")}
          >
            <span className="stat-label cancel">예약 취소</span>
            <span className="stat-value text-gray">{stats.cancel}</span>
          </div>
        </div>

        {/* 예약 카드 목록 */}
        {filteredReservations.length === 0 ? (
          <div className="empty-box">
            <p>해당 조건의 PT 예약 내역이 없습니다.</p>
          </div>
        ) : (
          <div className="reservation-grid">
            {filteredReservations.map((reservation) => (
              <div
                key={reservation.id}
                className="reservation-card"
                onClick={() => memberModalFn(reservation.memberId)}
              >
                <div className="card-header">
                  <div className="member-info">
                    <span className="member-icon">👤</span>
                    <h3 className="member-name">
                      {reservation.memberName} 회원님
                    </h3>
                  </div>
                  {getStatusBadge(reservation.reservationStatus)}
                </div>

                <div className="card-body">
                  <div className="info-row">
                    <span className="info-label">🗓️ 수업 일시</span>
                    <span className="info-value highlight-time">
                      {formatDateTime(reservation.reservationTime)}
                    </span>
                  </div>

                  <div className="info-row memo-row">
                    <span className="info-label">💬 요청사항</span>
                    <p className="info-memo">
                      {reservation.memo && reservation.memo.trim() !== ""
                        ? reservation.memo
                        : "특이사항 및 요청사항이 없습니다."}
                    </p>
                  </div>
                </div>

                {/* 예약 상태가 RESERVED일 때만 액션 버튼 표시 */}
                {reservation.reservationStatus === "RESERVED" && (
                  <div className="card-footer">
                    <button
                      className="btn btn-complete"
                      onClick={() => changeStatus(reservation.id, "COMPLETE")}
                    >
                      수업 완료
                    </button>
                    <button
                      className="btn btn-cancel"
                      onClick={() => changeStatus(reservation.id, "CANCEL")}
                    >
                      예약 취소
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
};

export default TrainerReservation;
