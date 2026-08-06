import React, { useState } from "react";
import Reservation from "../../../components/shop/reservation/Reservation";
import MyReservationList from "../../../components/shop/reservation/MyReservationList";

const ReservationPage = () => {
  const [activeTab, setActiveTab] = useState("apply"); // 'apply' | 'my'
  const handleReservationSuccess = () => {
    setActiveTab("my");

    window.scrollTo(0, 0);
  };
  return (
    <div className="reservation-container">
      {/* 탭 네비게이션 */}
      <div className="tab-menu">
        <button
          className={activeTab === "apply" ? "active" : ""}
          onClick={() => setActiveTab("apply")}
        >
          PT 예약 신청
        </button>
        <button
          className={activeTab === "my" ? "active" : ""}
          onClick={() => setActiveTab("my")}
        >
          내 예약 내역
        </button>
      </div>

      {/* 탭 콘텐츠 영역 */}
      <div className="tab-content">
        {activeTab === "apply" ? (
          <Reservation onSuccess={handleReservationSuccess} />
        ) : (
          /* 예약 성공 시 onSuccess 콜백을 통해 'my' 탭으로 자동 이동 */
          <MyReservationList />
        )}
      </div>
    </div>
  );
};

export default ReservationPage;
