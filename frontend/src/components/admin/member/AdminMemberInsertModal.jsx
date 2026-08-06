import React, { useState } from "react";
import "../../../css/admin/AdminMemberModal.css";
import axios from "axios";
import { API_SERVER_URL } from "../../../apis/commonApi";
import { useSelector } from "react-redux";
import jwtAxios from "../../../apis/util/jwtUtil";
const initData = {
  userName: "",
  userEmail: "",
  userPw: "",
  userAddress: "",
  userPhone: "",
  role: "MEMBER",
};

const AdminMemberInsertModal = ({ getMemberList, setIsBool }) => {
  //권한확인용 데이터 불러오기
  const { memberData } = useSelector((state) => state.loginSlice);
  //회원정보 저장용 상태값
  const [memberInsertData, setmemberInsertData] = useState(initData);

  const onChangeFn = (e) => {
    const { name, value } = e.target;
    if (name === "userEmail") setEmailCheck(false);
    setmemberInsertData({ ...memberInsertData, [name]: value });
  };

  //이메일 중복체크 여부 확인 변수
  const [emailCheck, setEmailCheck] = useState(false);

  //이메일 형식 체크를 위한 정규식 선언
  const emailRegex =
    /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;

  const onInsertFn = async () => {
    if (!emailCheck) {
      alert("이메일 중복을 체크해주세요.");
      return;
    }
    if (!memberInsertData.userPw) {
      alert("비밀번호를 입력해주세요");
      return;
    }
    if (!memberInsertData.userName) {
      alert("이름을 입력해주세요");
      return;
    }
    const formData = new FormData();
    // memberInsertData 객체의 모든 [key, value] 쌍을 반복문으로 추가
    Object.entries(memberInsertData).forEach(([key, value]) => {
      // null이나 undefined가 들어가는 것을 방지
      formData.append(key, value ?? "");
    });

    try {
      const res = await jwtAxios.post(
        `${API_SERVER_URL}/api/member/admin/insert`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        },
      );
      console.log(res);
      if (res.data === "ok") {
        alert("회원생성 성공!");
        getMemberList("", "", 0);
        setIsBool(false);
      }
    } catch (err) {
      console.error("회원생성 통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };
  const emailCheckFn = async () => {
    if (!memberInsertData.userEmail) {
      alert("이메일을 입력해주세요");
      return;
    }
    //이메일 형식에 맞지않는지 체크
    if (!emailRegex.test(memberInsertData.userEmail.trim())) {
      alert("이메일 형식이 올바르지 않습니다.");
      return;
    }
    const formData = new FormData();
    formData.append("userEmail", memberInsertData.userEmail);
    try {
      const res = await jwtAxios.post(
        `${API_SERVER_URL}/api/member/email`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        },
      );
      if (res.data === "ok") {
        alert("이메일 체크 완료");
        setEmailCheck(true);
      } else if (res.data === "no") {
        alert("이메일이 중복되었습니다.");
      }
    } catch (err) {
      console.error("통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };
  return (
    <div className="memberInsertModal">
      <div className="memberInsertModal-con">
        <ul>
          <li>
            <span>이름</span>
            <span>
              <input
                type="text"
                name="userName"
                id="userName"
                value={memberInsertData.userName}
                onChange={onChangeFn}
              />
            </span>
          </li>
          <li>
            <span>이메일</span>
            <span>
              <input
                type="email"
                name="userEmail"
                id="userEmail"
                value={memberInsertData.userEmail}
                onChange={onChangeFn}
              />
            </span>
            <button onClick={emailCheckFn}>중복확인</button>
          </li>
          <li>
            <span>비밀번호</span>
            <span>
              <input
                type="password"
                name="userPw"
                id="userPw"
                value={memberInsertData.userPw}
                onChange={onChangeFn}
              />
            </span>
          </li>
          <li>
            <span>주소</span>
            <span>
              <input
                type="text"
                name="userAddress"
                id="userAddress"
                value={memberInsertData.userAddress}
                onChange={onChangeFn}
              />
            </span>
          </li>
          <li>
            <span>전화번호</span>
            <span>
              <input
                type="text"
                name="userPhone"
                id="userPhone"
                value={memberInsertData.userPhone}
                onChange={onChangeFn}
              />
            </span>
          </li>
          {memberData?.result?.role === "ADMIN" && (
            <li>
              <span>권한</span>
              <span>
                <select
                  name="role"
                  id="role"
                  value={memberInsertData.role}
                  onChange={onChangeFn}
                >
                  <option value="ADMIN">관리자</option>
                  <option value="TRAINER">트레이너</option>
                  <option value="MANAGER">매니저</option>
                  <option value="MEMBER">일반회원</option>
                </select>
              </span>
            </li>
          )}
          <li>
            <button onClick={onInsertFn}>회원생성</button>
            <button onClick={() => setmemberInsertData(initData)}>
              초기화
            </button>
            <button onClick={() => setIsBool(false)}>취소</button>
          </li>
        </ul>
      </div>
    </div>
  );
};

export default AdminMemberInsertModal;
