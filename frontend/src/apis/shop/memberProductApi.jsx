import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../commonApi";

export const getActivePtProducts = async () => {
  const res = await jwtAxios.get(
    `${API_SERVER_URL}/api/member-products/active-pt`
  );
  return res.data;
};

export const checkSubscribe = async () => {
  const res = await jwtAxios.get(
    `${API_SERVER_URL}/api/member-products/subscribe`
  );
  return res.data;
};
// 내 이용권 조회
export const getMyMembership = async () => {
  const res = await jwtAxios.get(
    `${API_SERVER_URL}/api/member-products/my`
  );
  return res.data;
};