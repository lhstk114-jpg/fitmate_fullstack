import { useEffect, useState } from "react";
import jwtAxios from "../../../apis/util/jwtUtil";
import "../../../css/shop/subscription/MySubscription.css";
import { useNavigate } from "react-router-dom";

const MySubscription = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getSubscriptionList();
  }, []);

  const getSubscriptionList = async () => {
    try {
      const res = await jwtAxios.get("/api/subscription/my");
      console.log(res.data);
      setSubscriptions(res.data || []);
    } catch (err) {
      console.error("구독 목록 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // ACTIVE 상태인 최신 구독 정보 찾기
  const validSubscriptions = subscriptions.filter(
    (sub) =>
      sub.productType === "PREMIUM" &&
      (sub.subscriptionStatus === "ACTIVE")
  );

  const premiumSubscription =
    validSubscriptions.length > 0
      ? validSubscriptions.reduce((latest, current) => (current.id > latest.id ? current : latest))
      : null;


  // 구독 해지
  const cancelSubscription = async () => {
    if (!premiumSubscription?.id) {
      alert("해지할 구독 정보를 찾을 수 없습니다.");
      return;
    }

    if (!window.confirm(`[${premiumSubscription.productName}] 구독을 해지하시겠습니까?\n해지 시 프리미엄 혜택이 바로 종료됩니다.`)) {
      return;
    }

    try {
      await jwtAxios.put(`/api/subscription/${premiumSubscription.id}/cancel`);
      alert("구독이 성공적으로 해지되었습니다.");
      getSubscriptionList();
    } catch (err) {
      console.error("구독 해지 에러:", err);
      alert(err.response?.data?.message || "구독 해지 실패");
    }
  };

  if (loading) {
    return (
      <div className="my-subscription-page">
        <div className="subscription-loading">
          <div className="spinner"></div>
          <p>구독 정보를 불러오는 중입니다...</p>
        </div>
      </div>
    );
  }

  const isPaused = premiumSubscription?.subscriptionStatus === "PAUSED";

  return (
    <div className="my-subscription-page">
      <div className="subscription-header">
        <h2>FitMate Plus+</h2>
        <p className="sub-title">프리미엄 멤버십 구독 현황 및 혜택 관리</p>
      </div>

      {premiumSubscription ? (
        <div className="subscription-content">
          {/* 프리미엄 멤버십 카드 */}
          <div className={`membership-card ${isPaused ? "paused-card" : ""}`}>
            <div className="card-top">
              <span className="brand-logo">FitMate Plus+</span>
              <span className={`status-badge ${isPaused ? "badge-paused" : "badge-active"}`}>
                {isPaused ? "⏸️ 일시정지 중" : "✨ 이용 중"}
              </span>
            </div>

            <div className="card-middle">
              <h3 className="plan-name">{premiumSubscription.productName}</h3>
            </div>

            <div className="card-bottom">
              <div className="date-info">
                <span>구독 시작일</span>
                <strong>{premiumSubscription.startDate?.split("T")[0] || "-"}</strong>
              </div>
              <div className="date-info">
                <span>{isPaused ? "다음 결제 예정" : "다음 결제일"}</span>
                <strong>{premiumSubscription.nextPaymentDate?.split("T")[0] || "-"}</strong>
              </div>
            </div>
          </div>

          {/* 혜택 안내 박스 */}
          <div className="benefit-section">
            <h3>받고 있는 혜택 모아보기</h3>
            <div className="benefit-grid">
              <div className="benefit-item">
                <span className="icon">🏋️‍♂️</span>
                <div className="text">
                  <strong>PT 상품 10% 할인</strong>
                  <p>모든 PT 수업 할인가 적용</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">💳</span>
                <div className="text">
                  <strong>헬스장 이용권 할인</strong>
                  <p>제휴 피트니스 센터 할인</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">🛍️</span>
                <div className="text">
                  <strong>굿즈 & 용품 할인</strong>
                  <p>스토어 전 상품 추가 할인</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">🚚</span>
                <div className="text">
                  <strong>무조건 무료 배송</strong>
                  <p>구매 금액 상관없이 무료배송</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">⚡</span>
                <div className="text">
                  <strong>PT 우선 예약</strong>
                  <p>인기 트레이너 우선 예약권</p>
                </div>
              </div>
            </div>
          </div>

          {/* 액션 버튼 영역 */}
          <div className="action-button-group">
            <button className="btn btn-secondary" onClick={() => setShowPaymentModal(true)}>
              결제 내역 확인
            </button>
            <button className="btn btn-danger-outline" onClick={cancelSubscription}>
              구독 해지
            </button>
          </div>
        </div>
      ) : (
        /* 구독을 하지 않은 상태 */
        <div className="no-subscription-card">
          <div className="promo-badge">NEW BENEFIT</div>
          <h3>아직 FitMate Plus+ 회원이 아니신가요?</h3>
          <p className="promo-desc">
            지금 구독하고 PT 할인부터 무료 배송까지 다양한 혜택을 누려보세요!
          </p>

          <div className="benefit-section promo-benefits">
            <div className="benefit-grid">
              <div className="benefit-item">
                <span className="icon">🏋️‍♂️</span>
                <div className="text">
                  <strong>PT 상품 10% 할인</strong>
                  <p>모든 PT 수업 할인가 적용</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">💳</span>
                <div className="text">
                  <strong>헬스장 이용권 할인</strong>
                  <p>제휴 피트니스 센터 할인</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">🛍️</span>
                <div className="text">
                  <strong>굿즈 & 용품 할인</strong>
                  <p>스토어 전 상품 추가 할인</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">🚚</span>
                <div className="text">
                  <strong>무조건 무료 배송</strong>
                  <p>구매 금액 상관없이 무료배송</p>
                </div>
              </div>
              <div className="benefit-item">
                <span className="icon">⚡</span>
                <div className="text">
                  <strong>PT 우선 예약</strong>
                  <p>인기 트레이너 우선 예약권</p>
                </div>
              </div>
            </div>
          </div>

          <button className="btn btn-primary btn-large" onClick={() => navigate("/subscription/premium")}>
            FitMate Plus+ 시작하기
          </button>
        </div>
      )}

      {/* 결제내역 모달 */}
      {showPaymentModal && (
        <div className="modal-overlay" onClick={() => setShowPaymentModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>구독 결제 상세 정보</h3>
              <button className="close-x" onClick={() => setShowPaymentModal(false)}>
                &times;
              </button>
            </div>
            <div className="modal-body">
              <div className="modal-info-row">
                <span>구독 상품</span>
                <strong>{premiumSubscription?.productName}</strong>
              </div>
              <div className="modal-info-row">
                <span>결제 일자</span>
                <strong>
                  {premiumSubscription?.createTime
                    ? premiumSubscription.createTime.split("T")[0]
                    : "-"}
                </strong>
              </div>
              <div className="modal-info-row">
                <span>결제 수단</span>
                <strong>
                  {premiumSubscription?.paymentMethod || "-"}
                </strong>
              </div>
              <div className="modal-info-row">
                <span>결제 상태</span>
                <strong className="text-success">결제 완료</strong>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowPaymentModal(false)}>
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MySubscription;