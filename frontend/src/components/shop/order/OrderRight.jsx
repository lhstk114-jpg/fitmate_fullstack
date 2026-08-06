import React from "react";
import { kakaoPay, normalPayment } from "../../../apis/shop/paymentApi";
import { cartOrder, directOrder } from "../../../apis/shop/orderApi";
import { useNavigate } from "react-router-dom";

const OrderRight = ({
  cartIds,
  orderData,
  totalPrice,
  payment,
  isPremium,
  hasDeliveryProduct,
}) => {
  const navigate = useNavigate();
  // 배송정보 체크
  const validateDeliveryInfo = () => {
    if (!hasDeliveryProduct) {
      return true; // 구독상품이면 체크 안함
    }

    if (!orderData.receiverName?.trim()) {
      alert("받는 분을 입력해주세요.");
      return false;
    }

    if (!orderData.receiverPhone?.trim()) {
      alert("연락처를 입력해주세요.");
      return false;
    }

    if (!orderData.receiverAddress?.trim()) {
      alert("주소를 입력해주세요.");
      return false;
    }

    return true;
  };
  // 주문생성
  const createOrder = async () => {
    let orderId;
    //바로 구매
    if (orderData.orderItemDtos?.length > 0) {
      orderId = await directOrder(orderData);
    } else {
      // 장바구니 구매
      orderId = await cartOrder({
        cartIds,
        order: orderData,
      });
    }
    return orderId;
  };
  // 결제
  const handlePayment = async () => {
    try {
      // 배송상품이면 배송정보 확인
      if (!validateDeliveryInfo()) {
        return;
      }
      const orderId = await createOrder();

      // 카카오페이
      if (payment === "kakao") {
        const res = await kakaoPay(orderId);
        window.location.href = res.approvalUrl;
        return;
      }
      // 일반결제
      if (payment === "card") {
        const paymentResult = {
          orderId,
          paymentStatus: "SUCCESS",
          paymentMethod: payment.toUpperCase(),
          amount: totalPrice + shippingFee,
        };
        alert("결제가 완료되었습니다.");
        const result = await normalPayment(orderId);
        navigate("/payment/success", {
          state: result,
        });
      }
    } catch (e) {
      console.error("결제 실패:", e);
    }
  };

  // 배송비는 항상 3000원
  const shippingFee = hasDeliveryProduct ? 3000 : 0;
  // FitMate Plus 할인 (상품금액 5%)
  const plusProductDiscount = isPremium ? Math.floor(totalPrice * 0.05) : 0;
  // FitMate Plus 무료배송 할인
  const freeShippingDiscount = isPremium && hasDeliveryProduct ? 3000 : 0;
  // 총 할인 금액
  const totalDiscount = plusProductDiscount + freeShippingDiscount;
  // 최종 결제금액
  const finalPrice = totalPrice + shippingFee - totalDiscount;
  return (
    <div className="orderRight">
      <h2>결제 정보</h2>

      <div className="priceBox">
        <div className="priceRow">
          <span>상품 금액</span>
          <span>{totalPrice.toLocaleString()}원</span>
        </div>

        <div className="priceRow">
          <span>배송비</span>
          <span>3,000원</span>
        </div>
        <hr />
        {/* 할인 영역 */}
        {totalDiscount > 0 && (
          <div className="priceRow">
            <span>총 할인받은 금액</span>
            <span>
              -{totalDiscount.toLocaleString()}원
            </span>
          </div>
        )}
        {/* 프리미엄 할인 */}
        {isPremium && plusProductDiscount > 0 && (
          <div className="priceRow">
            <span>FitMate Plus+ 상품 할인</span>
            <span>
              -{plusProductDiscount.toLocaleString()}원
            </span>
          </div>
        )}
        {/* 무료배송 할인 */}
        {isPremium && hasDeliveryProduct && (
          <div className="priceRow">
            <span>FitMate Plus+ 무료배송</span>
            <span>
              -3,000원
            </span>
          </div>
        )}
        <hr />
        <div className="totalPrice">
          <span>결제 예정 금액</span>

          <div>
            {/* 할인 있을 때만 할인 전 금액 표시 */}
            {totalDiscount > 0 && (
              <span className="beforePrice">
                {(totalPrice + shippingFee).toLocaleString()}원
              </span>
            )}
            <strong>
              {finalPrice.toLocaleString()}원
            </strong>
          </div>
        </div>
      </div>

      <button className="paymentButton" onClick={handlePayment}>
        결제하기
      </button>
    </div>
  );
};

export default OrderRight;
