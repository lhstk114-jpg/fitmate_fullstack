//쿠키 관리용 유틸
import { Cookies } from "react-cookie";

const cookies = new Cookies();

//쿠키 생성
export const setCookie = (name, Value, days) => {
  const expires = new Date();
  expires.setUTCDate(expires.getUTCDate() + days); //쿠키 만료기한
  return cookies.set(name, Value, { path: "/", expires: expires });
};
//쿠키 가져오기
export const getCookie = (name) => {
  return cookies.get(name);
};
//쿠키 제거
export const removeCookie = (name, path = "/") => {
  cookies.remove(name, { path });
};
