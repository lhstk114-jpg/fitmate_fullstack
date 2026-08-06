import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { getCookie, setCookie, removeCookie } from "../../apis/util/cookieUtil";
import { loginFn, loginOAuth2Fn } from "../../apis/auth/login";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import axios from "axios";
import store from "../store";
//멤버 초기화값
const initState = {
  memberData: null,
};
//일반 로그인용 비동기청크
export const loginPostAsync = createAsyncThunk(
  "loginPostAsync",
  ({ userEmail, userPw }) => {
    return loginFn(userEmail, userPw);
  },
);
//로그아웃용 비동기 청크
export const logoutAsync = createAsyncThunk(
  "auth/logoutAsync",
  async (_, { rejectWithValue }) => {
    try {
      // 백엔드의 CustomLogoutFilter가 동작하도록 POST 요청을 보냄
      const res = await axios.post(
        `${API_SERVER_URL}/logout`,
        {},
        { withCredentials: true },
      );
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

const loadMemberCookie = () => {
  const memberInfo = getCookie("member");

  if (memberInfo === null) return null;
  try {
    //쿠키가 문자열 상태일때만 객체로 파싱
    const parsedMember =
      typeof memberInfo === "string" ? JSON.parse(memberInfo) : memberInfo;

    //이메일 데이터가 있고 인코딩 되어있을땐 디코딩처리
    if (parsedMember && parsedMember.userEmail) {
      parsedMember.userEmail = decodeURIComponent(parsedMember.userEmail);
    }
    //파싱된데이터 or memberInfo데이터 반환
    return parsedMember;
  } catch (err) {
    console.error("멤버쿠키 파싱 실패", err);
    return null;
  }
};

//멤버의 email을 이용해 정보를 불러오는 비동기청크
export const loadMemberInit = createAsyncThunk(
  "auth/loadMemberInit",
  async (_, { rejectWithValue }) => {
    try {
      const memberInfo = getCookie("member");
      if (memberInfo === null) return null;
      //member쿠키를 파싱
      const parsedMember =
        typeof memberInfo === "string" ? JSON.parse(memberInfo) : memberInfo;

      if (parsedMember && parsedMember.userEmail) {
        const res = await jwtAxios.get(
          `${API_SERVER_URL}/api/member/init/${parsedMember.userEmail}`,
        );
        return res.data;
      }
      return null;
    } catch (err) {
      console.log("에러발생 : ", err);
      removeCookie("member");
      store.dispatch(logout());
      alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
      window.location.href = "/";
      return rejectWithValue(err);
    }
  },
);

const getInitialState = () => {
  const savedMember = loadMemberCookie();
  if (savedMember) {
    // 쿠키가 있으면 그 정보 전체가 현재 로그인 정보가 됨
    return savedMember;
  }
  return { ...initState }; // { memberData: null }
};

//로그인 관련 슬라이스 설정
const loginSlice = createSlice({
  name: "loginSlice",
  initialState: getInitialState(), //쿠키의 유무에따라 초깃값사용
  reducers: {
    //로그인
    login: (state, action) => {
      const payload = action.payload;
      setCookie("member", JSON.stringify(payload), 1);
      return payload;
    },
    //로그아웃
    logout: (state, action) => {
      removeCookie("member"); //쿠키삭제
      return { ...initState }; //초기상태로
    },
    socialLoginSuccess: (state, action) => {
      const payload = action.payload;
      const cookiePayload = { ...payload };
      if (cookiePayload.userEmail) {
        cookiePayload.userEmail = encodeURIComponent(cookiePayload.userEmail);
      }
      //기존 로그인 방식과 동일하게 쿠키 설정
      setCookie("member", JSON.stringify(cookiePayload), 1);

      return payload;
    },
  },
  extraReducers: (builder) => {
    builder
      //로그아웃 성공유무에따라 에러로그 생성
      .addCase(logoutAsync.fulfilled, (state, action) => {
        removeCookie("member"); // 일반 member 쿠키 제거
        return { ...initState }; // 상태 초기화
      })
      .addCase(logoutAsync.rejected, (state, action) => {
        console.error("백엔드 로그아웃 실패: ", action.payload);
        // 에러가 나더라도 클라이언트 쿠키는 지워주는 것이 안전합니다.
        removeCookie("member");
        return { ...initState };
      })
      .addCase(loginPostAsync.fulfilled, (state, action) => {
        const payload = action.payload;
        //정상적인 로그인 확인
        if (payload && !payload.error) {
          const cookiePayload = { ...payload };
          if (cookiePayload.userEmail) {
            //이메일의 한글 처리
            cookiePayload.userEmail = encodeURIComponent(
              cookiePayload.userEmail,
            );
          }
          //쿠키 저장
          setCookie("member", JSON.stringify(cookiePayload), 1);

          return payload;
        }
      })
      .addCase(loadMemberInit.fulfilled, (state, action) => {
        if (action.payload) {
          state.memberData = action.payload;
        }
      })
      .addCase(loginPostAsync.pending, (state, action) => {
        console.log("pending");
      })
      .addCase(loginPostAsync.rejected, (state, action) => {
        console.log("rejected");
      });
  },
});

export const { login, logout, socialLoginSuccess } = loginSlice.actions;

export default loginSlice;
