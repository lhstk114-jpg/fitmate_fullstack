import { useEffect, useState } from "react";

import CartItem from "../../../components/shop/cart/CartItem";
import CartSummary from "../../../components/shop/cart/CartSummary";
import "../../../css/shop/cart/Cart.css";

import {
  getCartList,
  updateCartQuantity,
  deleteCartItem,
} from "../../../apis/shop/cartApi";
import { useNavigate } from "react-router-dom";

const CartPage = () => {
  const [cartItems, setCartItems] = useState([]);
  const [selectedItems, setSelectedItems] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      const res = await getCartList();
      setCartItems(res.data);

      // 기본이 전체 선택
      setSelectedItems(res.data.map((item) => item.id));
    } catch (e) {
      console.error(e);
      if (e.response) {
        console.log(e.response.data);
      } else {
        console.log(e.message);
      }
    }
  };

  // 전체선택
  const handleSelectAll = (checked) => {
    if (checked) {
      setSelectedItems(cartItems.map((item) => item.id));
    } else {
      setSelectedItems([]);
    }
  };

  // 개별선택
  const handleSelectItem = (cartId, checked) => {
    if (checked) {
      setSelectedItems((prev) => [...prev, cartId]);
    } else {
      setSelectedItems((prev) => prev.filter((id) => id !== cartId));
    }
  };

  // 수량 변경
  const changeQuantity = async (cartItemId, quantity) => {
    if (quantity < 1) return;
    await updateCartQuantity(cartItemId, quantity);
    loadCart();
  };

  // 삭제
  const removeItem = async (cartItemId) => {
    await deleteCartItem(cartItemId);
    loadCart();
  };

  return (
    <div className="cart-page">
      <h2>장바구니</h2>
      <div className="cart-list">
        {cartItems.length === 0 ? (
          <div className="empty-cart">
            <h3> 장바구니가 비어있습니다.</h3>
            <button
              className="shop-btn"
              onClick={() => navigate("/shop/index")}
            >
              상품 주문하러 가기 →
            </button>
          </div>
        ) : (
          <>
            {cartItems.map((item) => (
              <CartItem
                key={item.id}
                item={item}
                checked={selectedItems.includes(item.id)}
                onSelect={handleSelectItem}
                changeQuantity={changeQuantity}
                removeItem={removeItem}
              />
            ))}

            <div className="cart-select-all">
              <input
                type="checkbox"
                checked={
                  cartItems.length > 0 &&
                  selectedItems.length === cartItems.length
                }
                onChange={(e) => handleSelectAll(e.target.checked)}
              />

              <span>전체 선택</span>
            </div>
          </>
        )}
      </div>
      {cartItems.length > 0 && (
        <CartSummary cartItems={cartItems} selectedItems={selectedItems} />
      )}
    </div>
  );
};

export default CartPage;
