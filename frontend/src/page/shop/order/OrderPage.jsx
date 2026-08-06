import React, { useEffect, useState } from "react";
import jwtAxios from "../../../apis/util/jwtUtil";

import ProductList from "../../../components/shop/order/ProductList";
import BuyerInfo from "../../../components/shop/order/BuyerInfo";
import PaymentMethod from "../../../components/shop/order/PaymentMethod";
import OrderRight from "../../../components/shop/order/OrderRight";
import { useLocation } from "react-router-dom";
import "../../../css/shop/order/OrderPage.css";

const OrderPage = () => {
  const location = useLocation();
  const [orderInfo, setOrderInfo] = useState({
    receiverName: "",
    receiverPhone: "",
    receiverAddress: "",
    receiverDetailAddress: "",
    deliveryMemo: "",
  });
  const [payment, setPayment] = useState("kakao");
  const [memberInfo, setMemberInfo] = useState(null);
  const [isPremium, setIsPremium] = useState(false);

  useEffect(() => {
    jwtAxios
      .get("/api/member/detail")
      .then((res) => {
        setMemberInfo(res.data.result);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);
  //프리미엄 여부
  useEffect(() => {
    jwtAxios
      .get("/api/member-products/subscribe")
      .then((res) => {
        setIsPremium(res.data);
      })
      .catch((err) => {
        console.error(err);
      });
  }, []);
  // 회원 정보를 기본 배송 정보로 세팅
  useEffect(() => {
    if (memberInfo) {
      setOrderInfo({
        receiverName: memberInfo.userName || "",
        receiverPhone: memberInfo.userPhone || "",
        receiverAddress: memberInfo.userAddress || "",
        receiverDetailAddress: memberInfo.userDetailAddress || "",
        deliveryMemo: "",
      });
    }
  }, [memberInfo]);

  // 장바구니 주문 데이터
  const cartItems = location.state?.cartItems || [];
  const cartIds = location.state?.cartIds || [];
  const totalPrice = location.state?.totalPrice || 0;

  // 바로구매 데이터
  const directItem = location.state?.directItem;

  // 배송상품 여부
  const hasDeliveryProduct = directItem
    ? directItem.productType === "GOODS"
    : cartItems.some((item) => item.productType === "GOODS");

  const orderData = {
    ...orderInfo,

    orderItemDtos: directItem
      ? [
          {
            productId: directItem.productId,
            quantity: directItem.quantity,
            productType: directItem.productType,
          },
        ]
      : [],
  };
  console.log("cartItems", cartItems);
  console.log(
    "상품타입",
    cartItems.map((item) => item.productType),
  );
  console.log("hasDeliveryProduct", hasDeliveryProduct);
  console.log("isPremium", isPremium);
  return (
    <div className="orderPage">
      <div className="orderPage-con">
        <h1>주문하기</h1>
        <div className="orderContainer">
          <div className="left">
            <ProductList cartItems={directItem ? [directItem] : cartItems} />
            <BuyerInfo orderInfo={orderInfo} setOrderInfo={setOrderInfo} />
            <PaymentMethod payment={payment} setPayment={setPayment} />
          </div>

          <div className="right">
            <OrderRight
              cartIds={cartIds}
              orderData={orderData}
              totalPrice={
                directItem ? directItem.price * directItem.quantity : totalPrice
              }
              payment={payment}
              hasDeliveryProduct={hasDeliveryProduct}
              isPremium={isPremium}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderPage;
