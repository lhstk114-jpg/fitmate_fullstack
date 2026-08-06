import { API_SERVER_URL } from "../commonApi";
import jwtAxios from "../util/jwtUtil";

//pw제외 회원정보 수정
export const memberUpdate = async ({
  //관리자, 일반회원수정 공통변수들
  memberData,
  navigate,
  redirectUrl,
  //일반회원페이지에 사용할 변수들
  originData,
  onRefresh,
  onLogout,
  onSuccessToggle,
}) => {
  if (!confirm("회원수정을 하시겠습니까?")) return;
  try {
    //이메일 형식에 맞지않는지 체크

    if (!memberData.userName) {
      alert("이름을 입력해주세요");
      return;
    }

    //file정보 가져오기
    const fileInput = document.getElementById("memberFile");
    const file = fileInput?.files[0];

    const sendData = new FormData();

    Object.keys(memberData).forEach((key) => {
      // 값의 유무 체크 후 추가 (null 반환 방지)
      if (memberData[key] !== null && memberData[key] !== undefined) {
        sendData.append(key, memberData[key]);
      }
    });
    //파일이 존재할때만 FormData에 memberFile이름으로 추가
    if (file) {
      sendData.append("memberFile", file);
    }

    //회원수정 공통api호출
    const res = await jwtAxios.put(
      `${API_SERVER_URL}/api/member/update`,
      sendData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );
    // console.log(res);

    //200이 아닌 호출엔 오류발생 알람 & 함수종료
    if (res.status !== 200) {
      console.log(res);
      alert("회원수정 오류발생");
      return;
    }
    if (originData && memberData.userEmail !== originData.userEmail) {
      if (typeof onLogout === "function") onLogout();
      alert("이메일 변경확인. 다시 로그인 해주시기 바랍니다.");
      navigate("/auth/login");
    } else {
      alert("회원수정에 성공하였습니다.");
      if (typeof onRefresh === "function") {
        try {
          await onRefresh();
        } catch (err) {
          console.error("최신 회원 정보 가져오기 실패:", err);
        }
      }
      if (typeof onSuccessToggle === "function") onSuccessToggle();
      if (redirectUrl) navigate(redirectUrl);
    }
  } catch (err) {
    console.log(err);
    alert("회원수정중 오류가 발생했습니다.");
  }
};

export const checkEmail = async (email, emailRegex) => {
  try {
    //이메일형식 체크
    if (emailRegex && !emailRegex.test(email.trim())) {
      alert("이메일 형식이 올바르지 않습니다.");
      return;
    }
    //비교할 이메일만 폼으로 담음
    const formData = new FormData();
    formData.append("userEmail", email);

    //email체크용 api서버 호출
    const res = await jwtAxios.post(
      `${API_SERVER_URL}/api/member/email`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      },
    );

    // "no"가 오면 중복(true), 아니면 사용가능(false)
    return res.data === "no";
  } catch (err) {
    console.error("이메일 중복 체크 오류:", err);
    throw err; // 필요시 컴포넌트 단에서 에러 처리하도록 던짐
  }
};

export const maskEmail = (email) => {
  if (!email) return "";
  const [localPart, domain] = email.split("@");

  if (localPart.length <= 3) {
    // ID가 너무 짧으면 첫 글자만 남기고 마스킹
    return `${localPart[0]}**@${domain}`;
  }

  // 앞 2글자 + 중간 마스킹 + 마지막 1글자 유지
  const visibleStart = localPart.slice(0, 2);
  const visibleEnd = localPart.slice(-1);
  const maskedLength = localPart.length - 3;
  const asterisks = "*".repeat(maskedLength > 0 ? maskedLength : 2);

  return `${visibleStart}${asterisks}${visibleEnd}@${domain}`;
};

// 2. 전화번호 마스킹 (예: 010-1234-5678 -> 010-****-5678)
export const maskPhone = (phone) => {
  if (!phone) return "";

  // 하이픈이 포함된 경우 (010-1234-5678)
  if (phone.includes("-")) {
    const parts = phone.split("-");
    if (parts.length === 3) {
      return `${parts[0]}-****-${parts[2]}`;
    }
  }

  // 하이픈이 없는 경우 (01012345678) 숫자를 잘라서 마스킹
  if (phone.length === 11) {
    return `${phone.slice(0, 3)}****${phone.slice(7)}`;
  } else if (phone.length === 10) {
    return `${phone.slice(0, 3)}***${phone.slice(6)}`;
  }

  return phone;
};
