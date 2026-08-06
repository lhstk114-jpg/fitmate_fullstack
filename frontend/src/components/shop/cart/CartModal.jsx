import React from "react";
import "../../../css/shop/cart/CartModal.css";

const CartModal = ({ onContinue, onCart }) => {
  return (
    <div className="modal-overlay">
      <div className="cart-modal">
        <h3>장바구니에 추가되었습니다.</h3>

        <div className="modal-buttons">
          <button className="continue-btn" onClick={onContinue}>
            계속 쇼핑하기
          </button>

          <button className="cart-move-btn" onClick={onCart}>
            장바구니 이동
          </button>
        </div>
      </div>
    </div>
  );
};

export default CartModal;
