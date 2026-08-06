import axios from "axios";
import { getCookie, removeCookie, setCookie } from "./cookieUtil";
import { API_SERVER_URL } from "../commonApi";
import store from "../../store/store";
import { logout, logoutAsync } from "../../store/slices/loginSlice";

//jwtUtil을 이용하기위해 axios통신 객체 생성
const jwtAxios = axios.create({
  withCredentials: true,
});
const host = API_SERVER_URL; //백엔드 서버주소
//액세스토큰 재 발급 함수
const refreshJWT = async () => {
  const res = await axios.post(
    `${host}/api/reissue`,
    {},
    { withCredentials: true },
  );

  //Reissue컨트롤러가 응답 헤더로 보내주는 access토큰 추출
  const newAccessToken = res.headers["access"];
  return newAccessToken;
};

const beforeReq = (config) => {
  const memberInfo = getCookie("member");

  if (!memberInfo) {
    return Promise.reject({ response: { data: { error: "REQUIRE_LOGIN" } } });
  }

  //access 헤더에 토큰 세팅
  config.headers.access = memberInfo.access;
  return config;
};

const requestFail = (err) => {
  return Promise.reject(err);
};
const beforeRes = async (res) => {
  return res;
};

const responseFail = async (err) => {
  const status = err.response?.status;
  const data = err.response?.data;

  //JwtFilter에서 걸러지는 error코드 감지시
  if (
    err.response &&
    (err.response.status === 401 ||
      (data && data.error === "ERROR_ACCESS_TOKEN"))
  ) {
    try {
      const memberCookieValue = getCookie("member");
      //쿠키값을 자바스크립트 객체로 변환
      const parsedMember =
        typeof memberCookieValue === "string"
          ? JSON.parse(memberCookieValue)
          : memberCookieValue;
      //정상적으로 memberCookie가 객체로 변환되었는지 확인
      if (!parsedMember) {
        throw new Error("MEMBER_COOKIE_NOT_FOUND");
      }
      //새 액세스 토큰 발급
      const newAccessToken = await refreshJWT();
      if (!newAccessToken) {
        throw new Error("REFRESH_FAILED"); // 토큰이 없으면 강제로 에러 발생
      }
      //'member'쿠키 최신화
      parsedMember.access = newAccessToken;
      setCookie("member", JSON.stringify(memberCookieValue), 1);
      //실패했던 요청정보를 가져와서 새 토큰으로 교체
      const originalRequest = err.config;
      originalRequest.headers.access = newAccessToken;
      //새로운 토큰으로 교체 후 백그라운드 요청 재시도
      return await jwtAxios(originalRequest);
    } catch (refreshError) {
      //리프레시 토큰까지 만료 시 만료 응답 처리
      // 로컬 청소 전개
      removeCookie("member");
      store.dispatch(logout());

      if (refreshError.response?.status === 500) {
        alert("서버 장애로 인증에 실패했습니다. 다시 로그인해주세요.");
      } else {
        alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
      }

      window.location.href = "/";
      return Promise.reject(refreshError);
    }
  }
  // console.log(status);
  // console.warn(`인증 외 에러 발생 (${status}): 로컬 세션을 클리어합니다.`);
  // removeCookie("member");
  // store.dispatch(logout());

  // // 상황에 따른 알림 창 분기
  // if (status === 500) {
  //   alert("서버 장애가 발생했습니다. 잠시 후 다시 로그인해주세요.");
  // } else {
  //   alert("올바르지 않은 세션 정보입니다. 다시 로그인해주세요.");
  // }

  // window.location.href = "/";
  // return Promise.reject(err);
};

jwtAxios.interceptors.request.use(beforeReq, requestFail);
jwtAxios.interceptors.response.use(beforeRes, responseFail);

export default jwtAxios;
