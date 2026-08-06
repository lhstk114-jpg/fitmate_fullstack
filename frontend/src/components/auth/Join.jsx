import { useEffect, useState } from "react";
import axios from "axios";
import { API_SERVER_URL } from "../../apis/commonApi";
import { Link, useNavigate } from "react-router-dom";
import "../../css/auth/join.css";
import { useSelector } from "react-redux";
import AddressModal from "../common/map/AddressModal";
const initUserData = {
  userEmail: "",
  userPw: "",
  userName: "",
  gender: "MALE",
  userAddress: "",
  userPhone: "",
  interest: "",
};
const Join = () => {
  const navigate = useNavigate();

  //비밀번호 확인용 상태값
  const [checkPw, setCheckPw] = useState("");

  const { memberData } = useSelector((state) => state.loginSlice);
  //이메일의 존재유무에 따라 true, false
  const isLogin = !!memberData?.result?.userEmail;

  // 회원가입정보를 담게될 변수
  const [joinData, setJoinData] = useState(initUserData);
  const onChangeFn = (e) => {
    const { name, value } = e.target;
    if (name === "userEmail") setEmailCheck(false);
    setJoinData({ ...joinData, [name]: value });
  };

  //이메일 중복체크 여부 확인 변수
  const [emailCheck, setEmailCheck] = useState(false);

  //이메일 형식 체크를 위한 정규식 선언
  const emailRegex =
    /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;

  const onJoinFn = async () => {
    if (!emailCheck) {
      alert("이메일 중복을 체크해주세요.");
      return;
    }
    if (!joinData.userPw) {
      alert("비밀번호를 입력해주세요");
      return;
    }
    if (!joinData.userName) {
      alert("이름을 입력해주세요");
      return;
    }
    const formData = new FormData();
    // joinData 객체의 모든 [key, value] 쌍을 반복문으로 추가
    Object.entries(joinData).forEach(([key, value]) => {
      // null이나 undefined가 들어가는 것을 방지
      formData.append(key, value ?? "");
    });
    try {
      const res = await axios.post(
        `${API_SERVER_URL}/api/member/join`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        },
      );
      if (res.data === "ok") {
        alert("회원가입 성공!");
        navigate("/auth/login");
      }
    } catch (err) {
      console.error("회원가입 통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };
  const emailCheckFn = async () => {
    if (!joinData.userEmail) {
      alert("이메일을 입력해주세요");
      return;
    }
    //이메일 형식에 맞지않는지 체크
    if (!emailRegex.test(joinData.userEmail.trim())) {
      alert("이메일 형식이 올바르지 않습니다.");
      return;
    }
    const formData = new FormData();
    formData.append("userEmail", joinData.userEmail);
    try {
      const res = await axios.post(
        `${API_SERVER_URL}/api/member/email`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        },
      );
      if (res.data === "ok") {
        setEmailCheck(true);
      } else if (res.data === "no") {
        alert("이메일이 중복되었습니다.");
        setEmailCheck(false);
      }
    } catch (err) {
      console.error("통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };

  // 주소찾기 모달 열기 여부
  const [open, setOpen] = useState(false);

  // AddressModal에서 선택한 주소 반환
  // 원하는 CRUD 폼에 맞게 자유롭게 저장하여 사용
  const handleSelect = ({ address }) => {
    setJoinData((prev) => ({
      ...prev,
      userAddress: address, // 기본 주소
    }));
  };

  useEffect(() => {
    if (isLogin) {
      alert("이미 로그인된 상태입니다.");
      navigate("/"); // 메인 페이지로 리다이렉트
    }
  }, [isLogin]);
  if (isLogin) {
    return null;
  }
  //비밀번호 확인유무
  const isMatch =
    joinData.userPw !== "" && checkPw !== "" && joinData.userPw === checkPw;
  return (
    <>
      <AddressModal
        open={open}
        onClose={() => setOpen(false)}
        onSelect={handleSelect}
        mapWidth="100%"
        mapHeight="400px"
        mapLevel={3}
      />

      <div className="join">
        <div className="join-header">
          <Link to="/">
            <h1>
              <img src="/images/main/라이트버전.png" alt="logo" />
            </h1>
          </Link>
        </div>
        <div className="join-con">
          <ul>
            <li>
              <input
                type="email"
                name="userEmail"
                id="userEmail"
                placeholder="이메일을 입력해주세요"
                value={joinData.userEmail}
                onChange={onChangeFn}
              />
              <button onClick={emailCheckFn}>중복확인</button>
            </li>
            {emailCheck ? (
              <li>
                <span>이메일 중복확인이 완료되었습니다.</span>
              </li>
            ) : (
              <li>
                <span>이메일 중복체크를 해주세요.</span>
              </li>
            )}
            <li>
              <input
                type="password"
                name="userPw"
                id="userPw"
                placeholder="비밀번호를 입력해주세요"
                value={joinData.userPw}
                onChange={onChangeFn}
              />
            </li>
            {/* 입력 중일 때 실시간 결과 표시 */}
            {checkPw.length > 0 && (
              <p style={{ color: isMatch ? "green" : "red" }}>
                {isMatch
                  ? "비밀번호가 일치합니다."
                  : "비밀번호가 일치하지 않습니다."}
              </p>
            )}
            <li>
              <input
                type="password"
                placeholder="비밀번호 확인"
                value={checkPw}
                onChange={(e) => setCheckPw(e.target.value)}
              />
            </li>

            <li>
              <input
                type="text"
                name="userName"
                id="userName"
                placeholder="이름을 입력해주세요"
                value={joinData.userName}
                onChange={onChangeFn}
              />
            </li>
            <li className="address-item">
              <button type="button" onClick={() => setOpen(true)}>
                주소 찾기
              </button>
              <input
                type="text"
                name="userAddress"
                id="userAddress"
                placeholder="주소를 입력해주세요"
                value={joinData.userAddress || ""}
                onChange={onChangeFn}
              />
            </li>
            <li>
              <input
                type="text"
                name="userPhone"
                id="userPhone"
                placeholder="핸드폰번호를 입력해주세요"
                value={joinData.userPhone}
                onChange={onChangeFn}
              />
            </li>
            <li>
              <span>
                <select
                  name="interest"
                  id="interest"
                  value={joinData.interest}
                  onChange={onChangeFn}
                >
                  <option value="">없음</option>
                  <option value="DIET">다이어트</option>
                  <option value="WORKOUT">운동</option>
                  <option value="HEALTH">건강관리</option>
                </select>
              </span>
            </li>
            <li>
              <button onClick={onJoinFn}>회원가입</button>
              <Link to="/auth/login">로그인</Link>
            </li>
          </ul>
        </div>
      </div>
    </>
  );
};

export default Join;
