import { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/trainer/trainerProfile.css";

const TrainerProfile = () => {
  // 서버에서 불러온 원본 데이터
  const [trainer, setTrainer] = useState({
    career: "",
    specialty: "",
    introduce: "",
    certificate: "",
    profileImage: "",
  });

  // 수정 중인 폼 데이터
  const [form, setForm] = useState({
    career: "",
    specialty: "",
    introduce: "",
    certificate: "",
    profileImage: "",
  });

  // 수정 모드 상태 (true: 수정 중, false: 보기 모드)
  const [isEdit, setIsEdit] = useState(false);

  useEffect(() => {
    getTrainer();
  }, []);

  const getTrainer = async () => {
    try {
      const res = await jwtAxios.get("/api/trainer/my");
      // null 값 방지 기본값 처리
      const initialData = {
        career: res.data?.career || "",
        specialty: res.data?.specialty || "",
        introduce: res.data?.introduce || "",
        certificate: res.data?.certificate || "",
        profileImage: res.data?.profileImage || "",
      };
      setTrainer(initialData);
      setForm(initialData);
    } catch (err) {
      console.error(err);
    }
  };

  // 수정 모드 진입
  const handleEditStart = () => {
    setForm(trainer); // 현재 트레이너 정보로 폼 리셋
    setIsEdit(true);
  };

  // 취소 버튼
  const handleCancel = () => {
    setForm(trainer); // 원래 데이터로 복원
    setIsEdit(false);
  };

  // 입력값 변경 핸들러
  const changeHandler = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  // 저장 (수정 완료)
  const updateHandler = async () => {
    try {
      await jwtAxios.put("/api/trainer/my", form);
      setTrainer(form); // 원본 데이터 업데이트
      setIsEdit(false);
      alert("프로필이 성공적으로 수정되었습니다.");
    } catch (err) {
      console.error(err);
      alert("수정에 실패했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="trainer-profile-container">
      <div className="profile-card">
        {/* 헤더 영역 */}
        <div className="profile-header">
          <div>
            <h2>PT 트레이너 프로필 관리</h2>
            <p className="subtitle">
              회원들에게 보여질 트레이너 프로필 정보입니다.
            </p>
          </div>

          {/* 보기 모드일 때만 '프로필 수정' 버튼 표시 */}
          {!isEdit && (
            <button className="btn btn-edit" onClick={handleEditStart}>
              프로필 수정
            </button>
          )}
        </div>

        {/* 폼 영역 */}
        <div className="profile-form">
          <div className="form-group">
            <label htmlFor="career">경력 및 약력</label>
            {isEdit ? (
              <input
                id="career"
                name="career"
                value={form.career}
                onChange={changeHandler}
                placeholder="예: 피트니스 트레이너 경력 5년, 전 OO 피트니스 헤드트레이너"
              />
            ) : (
              <p className="read-only-text">
                {trainer.career || "등록된 경력이 없습니다."}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="specialty">전문 분야</label>
            {isEdit ? (
              <input
                id="specialty"
                name="specialty"
                value={form.specialty}
                onChange={changeHandler}
                placeholder="예: 다이어트, 근력 증량, 체형 교정, 바디프로필 준비, 재활 운동"
              />
            ) : (
              <p className="read-only-text">
                {trainer.specialty || "등록된 전문 분야가 없습니다."}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="certificate">자격증 및 수상 내역</label>
            {isEdit ? (
              <input
                id="certificate"
                name="certificate"
                value={form.certificate}
                onChange={changeHandler}
                placeholder="예: 생활스포츠지도사 2급(보디빌딩), NASM-CPT, 2023 피트니스 대회 1위"
              />
            ) : (
              <p className="read-only-text">
                {trainer.certificate ||
                  "등록된 자격증 및 수상 내역이 없습니다."}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="introduce">자기소개 및 트레이닝 철학</label>
            {isEdit ? (
              <textarea
                id="introduce"
                name="introduce"
                rows="5"
                value={form.introduce}
                onChange={changeHandler}
                placeholder="회원님들의 목표 달성을 위한 트레이닝 방식이나 지도 철학을 소개해주세요."
              />
            ) : (
              <p className="read-only-text multiline">
                {trainer.introduce || "등록된 자기소개가 없습니다."}
              </p>
            )}
          </div>

          {/* 수정 모드일 때 하단 [저장 / 취소] 버튼 바 표시 */}
          {isEdit && (
            <div className="form-actions">
              <button className="btn btn-cancel" onClick={handleCancel}>
                취소
              </button>
              <button className="btn btn-save" onClick={updateHandler}>
                저장하기
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TrainerProfile;
