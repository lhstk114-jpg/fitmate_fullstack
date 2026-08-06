import jwtAxios from "../util/jwtUtil";
import { API_SERVER_URL } from "../commonApi";


//장바구니 주문 생성
export const cartOrder = async (orderData) => {
  const res = await jwtAxios.post(`${API_SERVER_URL}/api/order/cart`, orderData);
  console.log(res);
  return res.data;   // orderId 반환
};


// 바로 구매 주문 생성
export const directOrder = async (orderData) => {
  const res = await jwtAxios.post(
    `${API_SERVER_URL}/api/order/direct`, orderData);

  return res.data;
};

// 내 주문 목록
export const getOrderList = async () => {
  const res = await jwtAxios.get(
    `${API_SERVER_URL}/api/order/list`);
  return res.data;
};


// 주문 상세
export const getOrderDetail = async(orderId)=>{
  const res = await jwtAxios.get(
    `${API_SERVER_URL}/api/order/detail/${orderId}`);
  return res.data;
}

// PT / GYM / PREMIUM 주문 생성
export const createMembershipOrder = async (data) => {

  const res = await jwtAxios.post(
    `${API_SERVER_URL}/api/order/subscription`,
    data
  );
  return res.data;
};