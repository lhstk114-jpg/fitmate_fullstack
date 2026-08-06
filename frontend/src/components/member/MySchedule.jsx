import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import CommonCalendar from "../common/calendar/CommonCalendar";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/member/MySchedule.css";

// 일정 등록·수정 폼 초기값
const initForm = {
  eventType: "PERSONAL",
  title: "",
  start: "",
  end: "",
  description: "",

  // 새로 등록하거나 교체할 파일
  attachFile: null,

  // 기존 파일 조회용
  oldFileName: "",
  newFileName: "",
};

const MySchedule = () => {
  // 로그인 회원 정보
  const user = useSelector((state) => state.loginSlice);
  const isLogin = !!user?.userEmail;

  const API_URL = API_SERVER_URL;

  // 조회할 일정 유형
  const [eventType, setEventType] = useState("ALL");

  // 캘린더 일정 목록
  const [calendarEvents, setCalendarEvents] = useState([]);

  // 모달 상태
  const [isModalOpen, setIsModalOpen] = useState(false);

  // insert, detail, update
  const [modalMode, setModalMode] = useState("insert");

  // 현재 선택한 캘린더 일정
  const [selectedEvent, setSelectedEvent] = useState(null);

  // 등록·수정 입력값
  const [form, setForm] = useState(initForm);

  // 일정 목록 조회
  const getCalendarList = async () => {
    if (!isLogin) {
      setCalendarEvents([]);
      return;
    }

    try {
      const res = await jwtAxios.get(`${API_URL}/api/calendar/scheduleList`, {
        params: {
          eventType,
        },
      });

      setCalendarEvents(res.data || []);
    } catch (err) {
      console.error("일정 조회 실패:", err);
      setCalendarEvents([]);
    }
  };

  // 로그인 상태 또는 조회 유형 변경 시 재조회
  useEffect(() => {
    getCalendarList();
  }, [isLogin, eventType]);

  // 날짜 클릭 시 등록 모달 열기
  const openInsertModal = (info) => {
    setModalMode("insert");
    setSelectedEvent(null);

    setForm({
      ...initForm,
      start: `${info.dateStr}T09:00`,
      end: `${info.dateStr}T10:00`,
    });

    setIsModalOpen(true);
  };

  // // 일정 클릭 시 상세 모달 열기
  // const openDetailModal = (info) => {
  //   const event = info.event;

  //   setSelectedEvent(event);
  //   setModalMode("detail");

  //   setForm({
  //     ...initForm,

  //     eventType: event.extendedProps.eventType || "PERSONAL",

  //     title: event.title || "",

  //     start: event.startStr ? event.startStr.slice(0, 16) : "",

  //     end: event.endStr
  //       ? event.endStr.slice(0, 16)
  //       : event.startStr?.slice(0, 16) || "",

  //     description: event.extendedProps.description || "",

  //     oldFileName: event.extendedProps.oldFileName || "",

  //     newFileName: event.extendedProps.newFileName || "",
  //   });

  //   setIsModalOpen(true);
  // };

  // 모달 닫기 및 상태 초기화
  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedEvent(null);
    setModalMode("insert");
    setForm(initForm);
  };

  // 일반 입력값 변경
  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 첨부파일 변경
  const handleFileChange = (e) => {
    const selectedFile = e.target.files?.[0] || null;

    setForm((prev) => ({
      ...prev,
      attachFile: selectedFile,
    }));
  };

  // 입력값 검증
  const validateForm = () => {
    if (!form.title.trim()) {
      alert("일정 제목을 입력하세요.");
      return false;
    }

    if (!form.start || !form.end) {
      alert("시작일과 종료일을 입력하세요.");
      return false;
    }

    // 시작 시간과 종료 시간이 같거나
    // 종료 시간이 시작 시간보다 빠른 경우
    if (new Date(form.end) <= new Date(form.start)) {
      alert("종료일은 시작일보다 늦어야 합니다.");
      return false;
    }

    return true;
  };

  //일정 데이터를 FormData로 생성
  const createScheduleFormData = () => {
    const formData = new FormData();

    formData.append("eventType", form.eventType);

    formData.append("title", form.title);

    formData.append("start", form.start);

    formData.append("end", form.end);

    formData.append("description", form.description || "");

    /*파일을 선택한 경우에만 전송
      수정할 때 새 파일을 선택하지 않으면
      기존 파일을 그대로 유지*/
    if (form.attachFile) {
      formData.append("attachFile", form.attachFile);
    }

    return formData;
  };

  // 일정 등록
  const handleInsert = async () => {
    if (!validateForm()) {
      return;
    }

    const formData = createScheduleFormData();

    try {
      await jwtAxios.post(`${API_URL}/api/calendar/insert`, formData);

      // 등록 완료 후 일정 재조회
      await getCalendarList();

      // 모달 닫기 및 폼 초기화
      closeModal();
    } catch (err) {
      console.error("일정 등록 실패:", err);

      alert(
        err.response?.data?.message ||
          err.response?.data ||
          "일정 등록에 실패했습니다.",
      );
    }
  };

  // 수정 모드 전환
  const changeUpdateMode = () => {
    if (!selectedEvent) {
      return;
    }

    const editable = selectedEvent.extendedProps.editable;

    if (editable === false) {
      alert("수정할 수 없는 일정입니다.");
      return;
    }

    setModalMode("update");
  };

  // 일정 수정
  const handleUpdate = async () => {
    if (!selectedEvent) {
      return;
    }

    if (!validateForm()) {
      return;
    }

    const scheduleId = selectedEvent.extendedProps.sourceId || selectedEvent.id;

    const formData = createScheduleFormData();

    try {
      await jwtAxios.put(
        `${API_URL}/api/calendar/update/${scheduleId}`,
        formData,
      );

      // 수정 완료 후 일정 재조회
      await getCalendarList();

      // 상세 모드로 돌아가지 않고 모달 닫기
      closeModal();
    } catch (err) {
      console.error("일정 수정 실패:", err);

      alert(
        err.response?.data?.message ||
          err.response?.data ||
          "일정 수정에 실패했습니다.",
      );
    }
  };

  // 일정 삭제
  const handleDelete = async () => {
    if (!selectedEvent) {
      return;
    }

    const editable = selectedEvent.extendedProps.editable;

    if (editable === false) {
      alert("삭제할 수 없는 일정입니다.");
      return;
    }

    if (!window.confirm("일정을 삭제하시겠습니까?")) {
      return;
    }

    const scheduleId = selectedEvent.extendedProps.sourceId || selectedEvent.id;

    try {
      await jwtAxios.delete(`${API_URL}/api/calendar/delete/${scheduleId}`);

      // 삭제 완료 후 일정 재조회
      await getCalendarList();

      // 모달 닫기 및 초기화
      closeModal();
    } catch (err) {
      console.error("일정 삭제 실패:", err);

      alert(
        err.response?.data?.message ||
          err.response?.data ||
          "일정 삭제에 실패했습니다.",
      );
    }
  };

  // 오늘 일정 리스트

  // 오늘 날짜(00:00:00)
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 오늘이 일정 기간에 포함되는 일정만 조회
  const todayScheduleList = calendarEvents.filter((schedule) => {
    if (!schedule.start) {
      return false;
    }

    // 일정 시작일
    const start = new Date(schedule.start);
    start.setHours(0, 0, 0, 0);

    // 일정 종료일
    // 종료일이 없으면 시작일과 동일하게 처리
    const end = schedule.end
      ? new Date(schedule.end)
      : new Date(schedule.start);

    end.setHours(0, 0, 0, 0);

    // 시작일 <= 오늘 <= 종료일
    return start <= today && today <= end;
  });

  // 일정 데이터를 받아 상세 모달 열기
  const openScheduleDetail = (schedule) => {
    setSelectedEvent({
      id: schedule.id,
      extendedProps: {
        sourceId: schedule.sourceId,
        editable: schedule.editable,
      },
    });

    setModalMode("detail");

    setForm({
      ...initForm,
      eventType: schedule.eventType || "PERSONAL",
      title: schedule.title || "",
      start: schedule.start ? schedule.start.slice(0, 16) : "",
      end: schedule.end ? schedule.end.slice(0, 16) : "",
      description: schedule.description || "",
      oldFileName: schedule.oldFileName || "",
      newFileName: schedule.newFileName || "",
    });

    setIsModalOpen(true);
  };

  // FullCalendar 일정 클릭
  const openDetailModal = (info) => {
    const event = info.event;

    openScheduleDetail({
      id: event.id,
      sourceId: event.extendedProps.sourceId,
      eventType: event.extendedProps.eventType,
      title: event.title,
      start: event.startStr,
      end: event.endStr,
      description: event.extendedProps.description,
      editable: event.extendedProps.editable,
      oldFileName: event.extendedProps.oldFileName,
      newFileName: event.extendedProps.newFileName,
    });
  };

  return (
    <>
      <div className="member-schedule">
        <div className="member-schedule-wrap">
          <div className="member-schedule-title">
            <h2>My Schedule</h2>

            <div className="member-schedule-filter">
              <button type="button" onClick={() => setEventType("ALL")}>
                전체
              </button>

              <button type="button" onClick={() => setEventType("WORKOUT")}>
                운동
              </button>

              <button type="button" onClick={() => setEventType("PERSONAL")}>
                개인
              </button>
            </div>
          </div>

          <div className="member-schedule-con">
            <div className="member-schedule-top">
              <div className="member-schedule-calendar">
                <CommonCalendar
                  events={calendarEvents}
                  onDateClick={openInsertModal}
                  onEventClick={openDetailModal}
                />
              </div>
            </div>

            <div className="member-schedule-bottom">
              <div className="member-schedule-today">
                <div className="member-schedule-today-title">
                  <h3>오늘의 스케줄</h3>
                </div>

                <div className="member-schedule-today-con">
                  {todayScheduleList.length === 0 ? (
                    <p>오늘 등록된 일정이 없습니다.</p>
                  ) : (
                    <ul>
                      {todayScheduleList.map((schedule) => (
                        <li
                          key={`${schedule.eventType}-${schedule.sourceId || schedule.id}`}
                        >
                          <button
                            type="button"
                            onClick={() => openScheduleDetail(schedule)}
                          >
                            <span>{schedule.title}</span>
                            <span>{schedule.start.slice(11, 16)}</span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 일정 등록·상세·수정 모달 */}
      {isModalOpen && (
        <div className="schedule-modal">
          <div className="schedule-modal-wrap">
            <div className="schedule-modal-title">
              <h3>
                {modalMode === "insert" && "일정 등록"}

                {modalMode === "detail" && "일정 상세"}

                {modalMode === "update" && "일정 수정"}
              </h3>

              <button type="button" onClick={closeModal}>
                ×
              </button>
            </div>

            <div className="schedule-modal-con">
              <div>
                <label>일정 유형</label>

                <select
                  name="eventType"
                  value={form.eventType}
                  onChange={handleChange}
                  disabled={modalMode === "detail"}
                >
                  <option value="PERSONAL">개인 일정</option>

                  <option value="WORKOUT">운동 일정</option>
                </select>
              </div>

              <div>
                <label>제목</label>

                <input
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  readOnly={modalMode === "detail"}
                />
              </div>

              <div>
                <label>시작일</label>

                <input
                  type="datetime-local"
                  name="start"
                  value={form.start}
                  onChange={handleChange}
                  readOnly={modalMode === "detail"}
                />
              </div>

              <div>
                <label>종료일</label>

                <input
                  type="datetime-local"
                  name="end"
                  value={form.end}
                  onChange={handleChange}
                  readOnly={modalMode === "detail"}
                />
              </div>

              <div>
                <label>내용</label>

                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  readOnly={modalMode === "detail"}
                />
              </div>

              <div>
                <label>첨부 이미지</label>

                {modalMode !== "detail" && (
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                  />
                )}

                {/* 새로 선택한 파일명 */}
                {form.attachFile && <p>선택 파일: {form.attachFile.name}</p>}

                {form.newFileName && (
                  <div className="schedule-image-preview">
                    <img
                      src={`${API_URL}/upload/schedule/${form.newFileName}`}
                      alt={form.oldFileName || "일정 이미지"}
                    />

                    {form.oldFileName && <p>기존 파일: {form.oldFileName}</p>}
                  </div>
                )}
              </div>
            </div>

            <div className="schedule-modal-buttons">
              {/* 등록 모드 */}
              {modalMode === "insert" && (
                <>
                  <button type="button" onClick={handleInsert}>
                    등록
                  </button>

                  <button type="button" onClick={closeModal}>
                    취소
                  </button>
                </>
              )}

              {/* 상세 모드 */}
              {modalMode === "detail" && (
                <>
                  {selectedEvent?.extendedProps?.editable !== false && (
                    <>
                      <button type="button" onClick={changeUpdateMode}>
                        수정
                      </button>

                      <button type="button" onClick={handleDelete}>
                        삭제
                      </button>
                    </>
                  )}

                  <button type="button" onClick={closeModal}>
                    닫기
                  </button>
                </>
              )}

              {/* 수정 모드 */}
              {modalMode === "update" && (
                <>
                  <button type="button" onClick={handleUpdate}>
                    수정 완료
                  </button>

                  <button type="button" onClick={closeModal}>
                    취소
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default MySchedule;
