import React, { useEffect, useState } from "react";

import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { logout, logoutAsync } from "../../store/slices/loginSlice";

import "../../css/member/MemberDetail.css";
import MemberDetailUpdateView from "./detailComponents/MemberDetailUpdateView";
import MemberDetailView from "./detailComponents/MemberDetailView";

const MemberDetail = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  //authSlice에 저장된 멤버데이터를 가져옴
  const { memberData } = useSelector((state) => state.loginSlice);

  //처음 멤버데이터를 집어넣고, 마이페이지에 보여줄 데이터
  const [member, setMember] = useState(null);

  //멤버데이터 수정 여부
  const [isUpdate, setIsUpdate] = useState(false);
  //멤버데이터를 수정할 때 따로 수정데이터를 조작할 수 있게 설정
  const [updateData, setUpdateData] = useState(null);

  //처음 시작 시 멤버데이터를 불러오기 위한 비동기 함수
  const getMemberDetail = async () => {
    //jwtAxios => jwtUtil의 axios함수(jwt토큰 중 access토큰과 refresh토큰을 비교하여 데이터를 불러옴)
    const res = await jwtAxios.get(`${API_SERVER_URL}/api/member/detail`, {
      headers: {
        "Cache-Control": "no-cache",
      },
    });
    // console.log(res);
    return res.data;
  };

  //멤버 수정버튼 클릭시 상태값이 서로 반전되고, 수정함수를 실행하는 함수
  const updateFn = () => {
    if (!isUpdate) {
      setIsUpdate((prev) => !prev);
      setUpdateData({ ...member, userPw: "" });
    }
  };

  const logoutFn = async () => {
    //기존 그냥 로그아웃함수만 불러오던것 -> 비동기청크로 실제 customLogoutFilter를 거칠수있게 설정
    try {
      //로그아웃이 될때까지 기다림
      await dispatch(logoutAsync()).unwrap();
      navigate("/");
    } catch (error) {
      //로그아웃api가 실패하거나 서버가 다운되어있으면 로그를 남기고, 멤버쿠키만 제거하는 기존 로그아웃으로 진행
      console.error("로그아웃 처리 중 에러 발생:", error);
      dispatch(logout());
      navigate("/");
    }
  };

  //멤버 삭제 비동기 함수
  const memberDelete = async () => {
    if (!confirm("회원탈퇴를 하시겠습니까?")) return;
    try {
      const res = await jwtAxios.delete(`${API_SERVER_URL}/api/member/quit`);
      if (res.data === "ok") {
        dispatch(logout());
        alert("회원탈퇴 성공");
        window.location.href = "/";
      } else {
        alert("회원탈퇴에 실패하였습니다.");
      }
    } catch (err) {
      console.log(err);
      alert("회원탈퇴중 오류가 발생했습니다.");
    }
  };

  //처음 마이페이지 접속 시 authSlice에 저장된 member의 유저이메일의 유무로 데이터 가져오기
  useEffect(() => {
    getMemberDetail()
      .then((data) => {
        setMember(data.result);
        // console.log(data);
      })
      .catch((err) => console.error(err));
  }, []);
  return (
    <>
      <div className="memberDetail">
        <div className="memberDetail-con">
          <div className="memberInfo">
            {member === null ? (
              <>회원님의 정보를 불러오는 중입니다...</>
            ) : !isUpdate ? (
              <>
                <MemberDetailView
                  member={member}
                  updateFn={updateFn}
                  memberDelete={memberDelete}
                  navigate={navigate}
                />
              </>
            ) : (
              <MemberDetailUpdateView
                member={member}
                updateData={updateData}
                logoutFn={logoutFn}
                setMember={setMember}
                setUpdateData={setUpdateData}
                memberDelete={memberDelete}
                navigate={navigate}
                getMemberDetail={getMemberDetail}
                setIsUpdate={setIsUpdate}
              />
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default MemberDetail;
