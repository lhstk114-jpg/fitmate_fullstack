import CommonCalendar from "../../common/calendar/CommonCalendar";
import { kakaoPay, normalPayment } from "../../../apis/shop/paymentApi";
import { createMembershipOrder } from "../../../apis/shop/orderApi";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PaymentMethod from "../order/PaymentMethod";

const OrderMembership = ({ product }) => {
  const productTypeMap = {
    PT: "PT 이용권",
    GYM: "헬스장 이용권",
    PREMIUM: "FitMate Plus+",
  };

  const navigate = useNavigate();

  const [startDate, setStartDate] = useState(null);
  const [agree, setAgree] = useState(false);
  const [payment, setPayment] = useState("kakao");
  const [loading, setLoading] = useState(false);

  if (!product) {
    return <div>상품 정보가 없습니다.</div>;
  }

  // 날짜 포맷
  const formatDate = (date) => {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
      2,
      "0",
    )}-${String(date.getDate()).padStart(2, "0")}`;
  };

  // 오늘 날짜 (한국 기준)
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // GYM 종료일 계산
  const getEndDate = () => {
    if (!startDate || !product.duration) return "-";

    const date = new Date(startDate);
    date.setDate(date.getDate() + product.duration);

    return formatDate(date);
  };

  // 시작일 선택
  const handleDateClick = (info) => {
    setStartDate(info.dateStr);
  };

  // 결제
  const handlePayment = async () => {
    const isPremium = product.productType === "PREMIUM";

    if (!isPremium && !startDate) {
      alert("이용 시작일을 선택해주세요.");
      return;
    }

    if (isPremium && !agree) {
      alert("자동결제 및 이용약관에 동의해주세요.");
      return;
    }

    try {
      setLoading(true);

      const orderId = await createMembershipOrder({
        productId: product.id,
        startDate: isPremium
          ? new Date().toLocaleDateString("sv-SE")
          : startDate,
      });

      if (!orderId) {
        alert("주문 생성 실패");
        return;
      }

      if (payment === "kakao") {
        const res = await kakaoPay(orderId);
        window.location.href = res.approvalUrl;
        return;
      }

      if (payment === "card") {
        const result = await normalPayment(orderId);

        alert("결제가 완료되었습니다.");

        navigate("/payment/success", {
          state: result,
        });
      }
    } catch (error) {
      console.log(error);
      alert("결제 처리 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const isPremium = product.productType === "PREMIUM";
  
  return (
    <div className="order-membership">
      <div className="order-membership-con">
        <h2>결제하기</h2>

        {!isPremium && (
          <>
            <div className="calendar-box">
              <CommonCalendar
                events={[]}
                onDateClick={handleDateClick}
                validRange={{
                  start: today,
                }}
              />
            </div>

            <div className="selected-date">
              이용 시작일 : {startDate || "선택해주세요."}
              {product.productType === "GYM" && (
                <p>이용 종료일 : {getEndDate()}</p>
              )}
            </div>
          </>
        )}

        {isPremium && (
          <div className="selected-date">
            이용 시작일 : {new Date().toLocaleDateString("sv-SE")}
          </div>
        )}

        <div className="order-product">
          <h3>{product.productName}</h3>

          <p>상품 종류 : {productTypeMap[product.productType]}</p>
          {product.duration > 0 && <p>이용기간 : {product.duration}일</p>}
          {product.sessionCount > 0 && (
            <p>PT 횟수 : {product.sessionCount}회</p>
          )}
          <p>결제금액 : {product.price.toLocaleString()}원</p>
        </div>

        <PaymentMethod payment={payment} setPayment={setPayment} />

        {isPremium && (
          <>
            <hr />
            <label>
              <input
                type="checkbox"
                checked={agree}
                onChange={(e) => setAgree(e.target.checked)}
              />
              FitMate Plus+ 자동결제 및 이용약관에 동의합니다.
            </label>

            <p className="payment-info">
              ※ 매월 같은 날짜에 자동 결제되며, 마이페이지에서 구독 해지가
              가능합니다.
            </p>
          </>
        )}

        <div className="order-payment">
          <button disabled={loading} onClick={handlePayment}>
            {loading ? "결제 진행중..." : "결제하기"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default OrderMembership;
