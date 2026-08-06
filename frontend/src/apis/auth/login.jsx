import axios from "axios";
import { API_SERVER_URL } from "../commonApi";
import { setCookie } from "../util/cookieUtil";

export const loginFn = async (userEmail, userPw) => {
  const form = new FormData();
  form.append("username", userEmail);
  form.append("password", userPw);
  const header = {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  try {
    const rs = await axios.post(`${API_SERVER_URL}/login`, form, {
      ...header,
      withCredentials: true,
    });

    if (rs.status === 200) {
      //LoginFilter에서 가공한 유저 데이터 'member'를 쿠키에 저장
      setCookie("member", JSON.stringify(rs.data), 1);
      return rs.data;
    }
  } catch (err) {
    if (err.response && err.response.status === 401) {
      alert("로그인에 실패하였습니다. 아이디 또는 비밀번호를 확인해주세요.");
    } else {
      console.log(err);
      alert("서버 연결에 실패하였습니다.");
    }
    throw err;
  }
};

//소셜 로그인 함수
export const loginOAuth2Fn = async (code) => {
  try {
    const rs = await axios.post(
      `${API_SERVER_URL}/api/auth/oauth2-token`,
      { code: code },
      { withCredentials: true },
    );
    if (rs.status === 200) {
      setCookie("member", JSON.stringify(rs.data), 1);
      return rs.data;
    }
  } catch (err) {
    if (err.response && err.response.status === 401) {
      alert("로그인에 실패하였습니다. 아이디 또는 비밀번호를 확인해주세요.");
    } else {
      console.log(err);
      alert("서버 연결에 실패하였습니다.");
    }
    throw err;
  }
};
