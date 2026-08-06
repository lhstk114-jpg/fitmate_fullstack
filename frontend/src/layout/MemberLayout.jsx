import React, { useEffect } from "react";
import Header from "../components/common/Header";
import Footer from "../components/common/Footer";
import { Outlet, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";

const MemberLayout = () => {
  const navigate = useNavigate();
  //authSlice에 저장된 멤버데이터를 가져옴
  const { memberData } = useSelector((state) => state.loginSlice);

  // userEmail 존재 여부로 로그인 상태 확인
  const isLogin = !!memberData?.result?.userEmail;

  useEffect(() => {
    // 비로그인 상태로 이 레이아웃에 직접 접근했을 때만 마운트 시점에 체크
    if (!isLogin) {
      alert("접근권한이 없습니다.");
      navigate("/", { replace: true });
    }
  }, [navigate]);

  // 로그인 상태가 아니면 화면을 그리지 않음
  if (!isLogin) return null;
  return (
    isLogin && (
      <>
        <Header />
        <Outlet />
        <Footer />
      </>
    )
  );
};

export default MemberLayout;
