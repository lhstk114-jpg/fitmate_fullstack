import React, { useState } from "react";
import { API_SERVER_URL } from "../../../apis/commonApi";
import { checkEmail, memberUpdate } from "../../../apis/member/memberApi";
import AddressModal from "../../common/map/AddressModal";

const MemberDetailUpdateView = ({
  member,
  updateData,
  logoutFn,
  setMember,
  setUpdateData,
  memberDelete,
  navigate,
  getMemberDetail,
  setIsUpdate,
}) => {
  //이미지 수정 시 미리보기url을 변경하기 위한 상수선언
  const [prevUrl, setPrevUrl] = useState("");

  //이메일 정규식
  const emailRegex =
    /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;

  //기본 데이터 onChange함수
  const onChangeFn = (e) => {
    const { name, value } = e.target;
    setUpdateData({ ...updateData, [name]: value });
  };
  //파일 데이터 onChange함수
  const onChangeFileFn = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      //선택된 파일로 임시 미리보기 URL 생성 후 세팅
      setPrevUrl(URL.createObjectURL(selectedFile));
    }
  };

  //멤버수정 비동기 함수
  const memberUpdateFn = async () => {
    //이메일 변경감지를 위해 원본데이터와 바꾼데이터를 비교
    const emailChanged = member.userEmail !== updateData.userEmail;
    //만약 기존 이메일의 변경이 있었다면 이메일중복체크
    if (emailChanged) {
      const emailCheckResult = await checkEmail(
        updateData.userEmail,
        emailRegex,
      );
      if (emailCheckResult) {
        alert("이메일이 중복되었습니다.");
        return;
      }
    }
    //이메일 변경 함수 실행
    memberUpdate({
      memberData: updateData,
      originData: member,
      navigate,
      redirectUrl: "/mypage", // 성공 시 바로 이동할 주소 주입
      apiUrl: API_SERVER_URL,
      onRefresh: async () => {
        const newData = await getMemberDetail();
        if (newData && newData.result) setMember(newData.result);
      },
      onLogout: logoutFn,
      onSuccessToggle: () => setIsUpdate((prev) => !prev),
    });
  };

  // 주소찾기 모달 열기 여부
  const [open, setOpen] = useState(false);

  // AddressModal에서 선택한 주소 반환
  // 원하는 CRUD 폼에 맞게 자유롭게 저장하여 사용
  const handleSelect = ({ address }) => {
    setUpdateData((prev) => ({
      ...prev,
      userAddress: address, // 기본 주소
    }));
  };
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
      <div className="memberUpdateContainer">
        {/* 1. 프로필 사진 변경 섹션 */}
        <div className="profileHeader">
          <div className="profilePhotoEdit">
            <div className="profile-preview">
              {prevUrl ? (
                <img
                  src={prevUrl}
                  alt="새 이미지 미리보기"
                  className="prev-img"
                />
              ) : member?.newFileName ? (
                <img
                  src={`${API_SERVER_URL}/upload/member/${member.newFileName}`}
                  alt="프로필 사진"
                  className="prev-img"
                />
              ) : (
                <img
                  src="/images/member/wanderercreative-blank-profile-picture-973460.svg"
                  alt="기본이미지"
                  className="prev-img"
                />
              )}
            </div>
            <div className="fileInputArea">
              <label htmlFor="memberFile" className="fileLabel">
                사진 변경
              </label>
              <input
                type="file"
                name="memberFile"
                id="memberFile"
                onChange={onChangeFileFn}
                accept="image/*"
              />
            </div>
          </div>
        </div>

        {/* 2. 기본 계정 정보 수정 섹션 */}
        <div className="infoSection">
          <h3>기본 정보 수정</h3>
          <ul className="infoGrid">
            <li>
              <span>유저명</span>
              <input
                type="text"
                value={updateData.userName || ""}
                id="userName"
                name="userName"
                onChange={onChangeFn}
              />
            </li>
            <li>
              <span>이메일</span>
              <input
                type="email"
                value={updateData.userEmail || ""}
                id="userEmail"
                name="userEmail"
                onChange={onChangeFn}
              />
            </li>
            <li>
              <span>전화번호</span>
              <input
                type="text"
                value={updateData.userPhone || ""}
                id="userPhone"
                name="userPhone"
                onChange={onChangeFn}
                placeholder="010-0000-0000"
              />
            </li>
            <li className="fullWidth">
              <span>주소</span>
              <button type="button" onClick={() => setOpen(true)}>
                주소 찾기
              </button>
              <input
                type="text"
                value={updateData.userAddress || ""}
                id="userAddress"
                name="userAddress"
                onChange={onChangeFn}
              />
            </li>
          </ul>
        </div>

        {/* 3. 신체 & 운동 프로필 수정 섹션 (추가된 5개 데이터) */}
        <div className="infoSection">
          <h3>신체 & 운동 프로필 수정</h3>
          <ul className="infoGrid">
            <li>
              <label htmlFor="height">신장 (cm)</label>
              <input
                type="number"
                value={updateData.height || ""}
                id="height"
                name="height"
                onChange={onChangeFn}
                placeholder="예: 175"
              />
            </li>
            <li>
              <label htmlFor="weight">현재 체중 (kg)</label>
              <input
                type="number"
                value={updateData.weight || ""}
                id="weight"
                name="weight"
                onChange={onChangeFn}
                placeholder="예: 70"
              />
            </li>
            <li>
              <label htmlFor="goalWeight">목표 체중 (kg)</label>
              <input
                type="number"
                value={updateData.goalWeight || ""}
                id="goalWeight"
                name="goalWeight"
                onChange={onChangeFn}
                placeholder="예: 65"
              />
            </li>
            <li>
              <span>관심사</span>
              <span>
                <select
                  name="interest"
                  id="interest"
                  value={updateData.interest || ""}
                  onChange={onChangeFn}
                >
                  <option value="">없음</option>
                  <option value="DIET">다이어트</option>
                  <option value="WORKOUT">운동</option>
                  <option value="HEALTH">건강관리</option>
                </select>
              </span>
            </li>
          </ul>
        </div>

        {/* 4. 하단 버튼 영역 */}
        <div className="buttonArea">
          <button className="saveBtn" onClick={memberUpdateFn}>
            저장하기
          </button>
          <button
            className="cancelBtn"
            onClick={() => {
              setIsUpdate((prev) => !prev);
              setPrevUrl("");
            }}
          >
            취소
          </button>
          <button onClick={memberDelete} className="deleteBtn">
            회원탈퇴
          </button>
        </div>
      </div>
    </>
  );
};

export default MemberDetailUpdateView;
