import axios from "axios";
import { API_SERVER_URL } from "../commonApi";
import { getCookie } from "../util/cookieUtil";
import jwtAxios from "../util/jwtUtil";

export const addCart = (data) => {

  const member = getCookie("member");

  return axios.post(
    `${API_SERVER_URL}/api/cart`,
    data,
    {
      headers: {
        access: member.access
      },
      withCredentials: true
    }
  );
};

export const getCartList = () => {
  return jwtAxios.get("/api/cart");
};

export const updateCartQuantity = (cartItemId, quantity) => {
  return jwtAxios.put(
    `/api/cart/${cartItemId}`,
    {
      quantity
    }
  );
};

export const deleteCartItem = (cartItemId) => {
  return jwtAxios.delete(
    `/api/cart/${cartItemId}`
  );
};

export const clearCart = () => {
  return jwtAxios.delete("/api/cart/clear");
};