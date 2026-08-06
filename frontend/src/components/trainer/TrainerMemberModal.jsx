import React, { useEffect, useState } from "react";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/trainer/trainerModal.css";
import { maskPhone } from "../../apis/member/memberApi";

const TrainerMemberModal = ({ setIsBool, modalMemberId }) => {
  const [memberData, setMemberData] = useState(null);
  const getMemberList = async () => {
    const url = `${API_SERVER_URL}/api/member/members/summary/${modalMemberId}`;
    try {
      const res = await jwtAxios.get(url);
      setMemberData(res.data.result);
      // console.log(res.data);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };
  useEffect(() => {
    getMemberList();
  }, []);
  if (memberData === null) {
    return "";
  }
  return (
    <div className="trainerModal">
      <div className="trainerModal-con">
        <span onClick={() => setIsBool(false)}>X</span>
        <ul>
          <li>
            <h1>{memberData.userName} 회원님 상세정보</h1>
          </li>
          <li>
            <span className="profile-preview">
              {memberData && memberData.newFileName ? (
                //파일을 아직 고르지 않았을때 & 기존에 저장된 이미지가 있을경우(기존 이미지)
                <img
                  src={`${API_SERVER_URL}/upload/member/${memberData.newFileName}`}
                  alt="프로필 사진"
                  className="prev-img"
                />
              ) : (
                //기존이미지도 없고 선택도 안했을경우(기본 이미지 추가)
                <img
                  src="/images/member/wanderercreative-blank-profile-picture-973460.svg"
                  alt="기본이미지"
                  className="prev-img"
                />
              )}
            </span>
          </li>
          <li>
            <span>이름</span>
            <span>{memberData.userName}</span>
          </li>
          <li>
            <span>전화번호</span>
            <span>{maskPhone(memberData.userPhone) || "정보 없음"}</span>
          </li>
          <li>
            <span>관심사</span>
            <span>{memberData.interest || "정보 없음"}</span>
          </li>
          <li>
            <span>키</span>
            <span>
              {memberData.height ? `${memberData.height} cm` : "정보 없음"}
            </span>
          </li>
          <li>
            <span>몸무게</span>
            <span>
              {memberData.weight ? `${memberData.weight} kg` : "정보 없음"}
            </span>
          </li>
          <li>
            <span>목표몸무게</span>
            <span>
              {memberData.goalWeight
                ? `${memberData.goalWeight} kg`
                : "정보 없음"}
            </span>
          </li>
        </ul>
      </div>
    </div>
  );
};

export default TrainerMemberModal;
