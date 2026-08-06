import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../../store/slices/loginSlice";

const AdminHeader = ({ onToggleSidebar }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  //로그인 여부 판단
  const { memberData } = useSelector((state) => state.loginSlice); //user 정보
  const isLogin = !!memberData?.result?.userEmail;

  //로그아웃
  const logoutFn = async () => {
    //기존 그냥 로그아웃함수만 불러오던것 -> 비동기청크로 실제 customLogoutFilter를 거칠수있게 설정
    try {
      //로그아웃이 될때까지 기다림
      await dispatch(logoutAsync()).unwrap();
      alert("로그아웃 되었습니다.");
      navigate("/");
    } catch (error) {
      //로그아웃api가 실패하거나 서버가 다운되어있으면 로그를 남기고, 멤버쿠키만 제거하는 기존 로그아웃으로 진행
      console.error("로그아웃 처리 중 에러 발생:", error);
      dispatch(logout());
      navigate("/");
    }
  };

  return (
    <>
      <div className="admin-header">
        <div className="admin-header-con">
          {/* 모바일 900px 이하에서만 노출되는 햄버거 버튼 */}
          <button
            className="mobile-toggle-btn"
            onClick={onToggleSidebar}
            aria-label="메뉴 열기"
          >
            ☰
          </button>
          <div className="admin-nav-wrap">
            <div className="admin-gnb-right">
              <ul>
                <li>
                  <Link to={`/shop`}>스토어</Link>
                </li>
                <li>
                  <Link to={`/community`}>게시판</Link>
                </li>
                <li>
                  <Link to="/mypage">{memberData?.result?.userName}님</Link>
                </li>
                <li>
                  <button onClick={logoutFn}>로그아웃</button>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AdminHeader;
