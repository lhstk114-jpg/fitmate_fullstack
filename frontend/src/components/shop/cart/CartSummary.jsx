import React from "react";
import { useNavigate } from "react-router-dom";

const CartSummary = ({ cartItems, selectedItems }) => {
  const navigate = useNavigate();

  // 선택된 상품만 계산
  const selectedCartItems = cartItems.filter((item) =>
    selectedItems.includes(item.id),
  );

  // 총금액
  const totalPrice = selectedCartItems.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );
  // 총 개수
  const totalQuantity = selectedCartItems.reduce(
    (sum, item) => sum + item.quantity,
    0,
  );
  const handleBuy = () => {
    if (cartItems.length === 0) {
      alert("장바구니가 비어있습니다.");
      return;
    }
    if (selectedCartItems.length === 0) {
      alert("주문할 상품을 선택해주세요.");
      return;
    }
    navigate("/order", {
      state: {
        cartItems: selectedCartItems,
        cartIds: selectedItems,
        totalPrice,
      },
    });
  };

  return (
    <div className="cart-summary">
      <h3> 총 {totalQuantity}개 결제금액 </h3>
      <p>{totalPrice.toLocaleString()}원</p>
      <button onClick={handleBuy}>바로 구매</button>
    </div>
  );
};

export default CartSummary;
