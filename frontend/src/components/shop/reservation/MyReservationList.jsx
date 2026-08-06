import React, { useEffect, useState } from "react";
import jwtAxios from "../../../apis/util/jwtUtil";
import "../../../css/shop/reservation/MyReservationList.css";
import ReservationModal from "./ReservationModal";

const MyReservationList = () => {
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 내 예약 목록 불러오기
  const fetchMyReservations = async () => {
    try {
      setLoading(true);
      const res = await jwtAxios.get("/api/reservations/my");
      setReservations(res.data || []);
      console.log(res.data);
    } catch (err) {
      console.error("내 예약 목록 조회 실패:", err);
      console.error(err);
      console.error("status:", err.response?.status);
      console.error("data:", err.response?.data);
      console.error("message:", err.message);
      setError("예약 내역을 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyReservations();
  }, []);

  // 예약 취소 핸들러
  const handleCancel = async (reservationId) => {
    if (!window.confirm("정말로 이 예약을 취소하시겠습니까?")) return;

    try {
      await jwtAxios.put(`/api/reservations/${reservationId}/member-cancel`);
      alert("예약이 성공적으로 취소되었습니다.");
      // 목록 다시 로드 (또는 상태 업데이트)
      fetchMyReservations();
    } catch (err) {
      console.error("예약 취소 실패:", err);
      alert(err.response?.data?.message || "예약 취소 중 오류가 발생했습니다.");
    }
  };

  if (loading)
    return (
      <div className="reservation-loading">예약 내역을 불러오는 중...</div>
    );
  if (error) return <div className="reservation-error">{error}</div>;

  return (
    <>
      {isBool && (
        <ReservationModal setIsBool={setIsBool} modalMemberId={modalMemberId} />
      )}
      <div className="my-reservation-container">
        <h3 className="section-title">나의 PT 예약 내역</h3>

        {reservations.length === 0 ? (
          <div className="empty-reservation">
            <p>예약된 PT 일정이 없습니다.</p>
            <p className="sub-text">
              'PT 예약 신청' 탭에서 원하는 시간대를 선택해 보세요!
            </p>
          </div>
        ) : (
          <div className="reservation-card-list">
            {reservations.map((item) => (
              <div
                key={item.id}
                className={`reservation-card status-${item.reservationStatus?.toLowerCase()}`}
                onClick={() => memberModalFn(item.trainerId)}
              >
                <div className="card-header">
                  <span className="reservation-date">
                    📅 {item.reservationDate} ({item.reservationTime.slice(0, 5)})
                  </span>
                  <span className={`status-badge ${item.reservationStatus}`}>
                    {item.reservationStatus === "RESERVED" && "예약 완료"}
                    {item.reservationStatus === "CANCELLED" && "취소됨"}
                    {item.reservationStatus === "COMPLETED" && "수업 완료"}
                  </span>
                </div>

                <div className="card-body">
                  <div className="trainer-info">
                    <span className="info-label">담당 트레이너</span>
                    <span className="info-value">
                      {item.trainerName || "김트레이너"} 코치
                    </span>
                  </div>
                  <div className="product-info">
                    <span className="info-label">사용 차수</span>
                    <span className="info-value">
                      {item.lessonNumber}회차 / {item.totalCount}회
                    </span>
                  </div>
                </div>

                {/* 예약 상태가 RESERVED(예약완료) 일 때만 취소 버튼 활성화 */}
                {item.reservationStatus === "RESERVED" && (
                  <div className="card-footer">
                    <button
                      className="btn-cancel"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCancel(item.id);
                      }}
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

export default MyReservationList;
