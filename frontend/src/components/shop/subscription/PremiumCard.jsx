import React, { useEffect, useState } from "react";
import "../../../css/shop/subscription/premiumCard.css";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const PremiumCard = () => {
  const [premiumProduct, setPremiumProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  const navigate = useNavigate();

  useEffect(() => {
    axios
      .get("/api/product/premium")
      .then((res) => {
        setPremiumProduct(res.data);
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>상품 불러오는 중...</div>;
  }

  if (!premiumProduct) {
    return <div>상품 정보를 불러올 수 없습니다.</div>;
  }
  const benefits = [
    { title: "PT 상품 할인", desc: "모든 PT 강습권 최대 15% 추가 할인" },
    { title: "헬스장 이용권 할인", desc: "제휴 헬스장 멤버십 단독 특가" },
    { title: "굿즈 할인", desc: "FitMate 공식 굿즈 전 상품 할인" },
    { title: "무조건 무료배송", desc: "구매 금액 상관없이 전 상품 무료 배송" },
    { title: "PT 우선 예약 서비스", desc: "인기 트레이너 스케줄 우선 선점" },
  ];

  return (
    <div className="premium-card-container">
      <div className="premium-badge">BEST VALUE</div>
      <h3 className="premium-title">FitMate Plus+</h3>
      <p className="premium-subtitle">운동의 질을 높이는 가장 완벽한 선택</p>

      {/* 가격 표시 */}
      <div className="price-box">
        <span className="price-amount">19,900</span>
        <span className="price-unit">원 / 월</span>
      </div>

      {/* 혜택 리스트 */}
      <ul className="benefit-list">
        {benefits.map((benefit, index) => (
          <li key={index} className="benefit-item">
            <span className="check-icon">✔</span>
            <div className="benefit-text">
              <strong>{benefit.title}</strong>
              <small>{benefit.desc}</small>
            </div>
          </li>
        ))}
      </ul>

      {/* 결제하기 버튼 */}
      <button
        className="subscribe-cta-btn"
        onClick={() =>
          navigate("/order/membership", { state: { product: premiumProduct } })
        }
      >
        FitMate Plus+ 시작하기
      </button>
    </div>
  );
};

export default PremiumCard;
