import axios from "axios";
import { API_SERVER_URL } from "../commonApi";

//jwtUtil로 자동으로 처리
export const getRefreshToken = async () => {
  //액세스 토큰을 localStorage에서 가져옴
  const access = localStorage.getItem("accessToken");
  //Refresh토큰을 localStorage에서 가져옴
  const refresh = localStorage.getItem("refreshToken");

  //쿼리 파라미터로 Refresh토큰 전달 및 Authorization 헤더에 액세스 토큰 추가
  const rs = await axios.get(API_SERVER_URL + "/reissue", {
    params: { refreshToken: refresh }, //Refresh토큰을 쿼리 파라미터에 전달
    headers: {
      //Authorization 헤더에 액세스 토큰을 Bearer형식으로 추가
      Authorization: "Bearer" + access,
    },
  });

  return rs.data; //서버 응답 데이터 반환
};
