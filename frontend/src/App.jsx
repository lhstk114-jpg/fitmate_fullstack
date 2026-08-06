import { RouterProvider } from "react-router-dom";
import Main from "./components/Main";
import root from "./router/root";
import { useDispatch, useSelector } from "react-redux";
import { useEffect } from "react";
import { loadMemberInit, socialLoginSuccess } from "./store/slices/loginSlice";
import { getCookie, removeCookie } from "./apis/util/cookieUtil";

function App() {
  const dispatch = useDispatch();

  const { memberData } = useSelector((state) => state.loginSlice);

  //소셜로그인전용
  useEffect(() => {
    //백엔드의 소셜로그인 성공 핸들러가 구운 임시 쿠키 확인
    const socialMemberInfo = getCookie("memberInfo");
    if (socialMemberInfo) {
      try {
        //URL 디코딩 후 JSON객체로 파싱(만약 문자열 타입이면 파싱, 이미 객체면 그대로 사용)
        const memberDataFromSocial =
          typeof socialMemberInfo === "string"
            ? JSON.parse(decodeURIComponent(socialMemberInfo))
            : socialMemberInfo;

        //redux의 상태 업데이트 및 기존 member쿠키로 변환&보관하는 리듀서 호출
        dispatch(socialLoginSuccess(memberDataFromSocial));
        //사용이 끝난 임시 쿠키 삭제
        removeCookie("memberInfo");
      } catch (err) {
        console.error("소셜 로그인 데이터 파싱 중 에러 발생:", err);
      }
    }
  }, [dispatch]);
  //일반로그인전용
  useEffect(() => {
    const socialMemberInfo = getCookie("memberInfo");
    const memberCookie = getCookie("member");

    if (
      !socialMemberInfo &&
      memberCookie &&
      (!memberData || Object.keys(memberData).length === 0)
    ) {
      dispatch(loadMemberInit());
    }
  }, [dispatch, memberData]);
  console.log(memberData);
  return (
    <>
      <RouterProvider router={root}>
        <Main />
      </RouterProvider>
    </>
  );
}
export default App;
