import CommonCalendar from "../common/calendar/CommonCalendar";
import { useState } from "react";

const AdminCalendar = () => {
  // 일정 목록
  // 추후 백엔드 조회 결과를 저장하는 상태로 변경
  // const [events, setEvents]=useState([]);
  const [events, setEvents] = useState([
    {
      id: "1",
      sourceId: "1",
      eventType: "PERSONAL",
      title: "개인 일정",
      start: "2026-07-06T10:00",
      end: "2026-07-06T11:00",
      content: "기본 일정입니다.",
      editable: true,
    },
    {
      id: "2",
      sourceId: "2",
      eventType: "PT",
      title: "PT 일정",
      start: "2026-07-07T14:00",
      end: "2026-07-07T15:00",
      content: "PT 일정입니다.",
      editable: false,
    },
  ]);

  // 분류별 메뉴
  const [selectMenu, setSelectMenu] = useState("PERSONAL");

  // 선택한 메뉴에 맞는 일정만 필터링
  const filteredEvents = events.filter((event) => {
    if (selectMenu === "ALL") {
      return true;
    }
    return event.eventType === selectMenu;
  });

  // 모달 입력값
  const [form, setForm] = useState({
    title: "",
    start: "",
    end: "",
    content: "",
  });

  // 모달 상태
  const [isModalOpen, setIsModalOpen] = useState(false);

  // insert, detail, update
  const [modalMode, setModalMode] = useState("insert");

  // 현재 선택한 일정
  const [selectedEvent, setSelectedEvent] = useState(null);

  // 날짜 클릭 시 등록 모달 열기
  // CommonCalendar의 onDateClick으로 전달
  const openInsertModal = (info) => {
    setModalMode("insert");
    setSelectedEvent(null);

    setForm({
      title: "",
      start: `${info.dateStr}T09:00`,
      end: `${info.dateStr}T10:00`,
      content: "",
    });

    setIsModalOpen(true);
  };

  // 일정 클릭 시 상세 모달 열기
  // CommonCalendar의 onEventClick으로 전달
  const openDetailModal = (info) => {
    const event = info.event;

    setSelectedEvent(event);
    setModalMode("detail");

    setForm({
      title: event.title,
      start: event.startStr.slice(0, 16),
      end: event.endStr
        ? event.endStr.slice(0, 16)
        : event.startStr.slice(0, 16),
      content: event.extendedProps.content || "",
    });

    setIsModalOpen(true);
  };

  // 모달 닫기
  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedEvent(null);
    setModalMode("insert");

    setForm({
      title: "",
      start: "",
      end: "",
      content: "",
    });
  };

  // 입력값 변경
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({
      ...form,
      [name]: value,
    });
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
    if (new Date(form.end) < new Date(form.start)) {
      alert("종료일은 시작일보다 빠를 수 없습니다.");
      return false;
    }
    return true;
  };

  // 일정 등록
  // 추후 개인 일정 등록 API 호출로 변경
  const handleInsert = () => {
    if (!validateForm()) {
      return;
    }

    const newEvent = {
      id: Date.now().toString(),
      sourceId: Date.now().toString(),
      eventType: "PERSONAL",
      title: form.title,
      start: form.start,
      end: form.end,
      content: form.content,
      editable: true,
    };

    setEvents([...events, newEvent]);
    closeModal();
  };
  // 일정 수정
  // 추후 개인 일정 수정 API 호출로 변경
  const handleUpdate = () => {
    if (!selectedEvent) {
      return;
    }
    if (!validateForm()) {
      return;
    }
    setEvents(
      events.map((event) =>
        event.id === selectedEvent.id
          ? {
              ...event,
              title: form.title,
              start: form.start,
              end: form.end,
              content: form.content,
            }
          : event,
      ),
    );
    closeModal();
  };

  // 일정 삭제
  // 추후 개인 일정 삭제 API 호출로 변경
  const handleDelete = () => {
    if (!selectedEvent) {
      return;
    }
    if (!window.confirm("일정을 삭제하시겠습니까?")) {
      return;
    }
    setEvents(events.filter((event) => event.id !== selectedEvent.id));
    closeModal();
  };

  return (
    <div className="admin-main">
      <div className="adminCalendar-wrap">
        {/* 일정 분류 메뉴 */}
        <div className="adminCalendar-left">
          <ul>
            <li
              onClick={() => setSelectMenu("PERSONAL")}
              className={selectMenu === "PERSONAL" ? "active" : ""}
            >
              내 일정
            </li>
            <li
              onClick={() => setSelectMenu("PT")}
              className={selectMenu === "PT" ? "active" : ""}
            >
              PT 일정
            </li>
            <li
              onClick={() => setSelectMenu("ALL")}
              className={selectMenu === "ALL" ? "active" : ""}
            >
              전체 일정
            </li>
          </ul>
        </div>

        {/* 캘린더 영역 */}
        <div className="adminCalendar">
          <div className="adminCalendar-con">
            <div className="adminCalendar-title">
              <h2>
                {selectMenu === "PERSONAL" && "내 일정"}
                {selectMenu === "PT" && "PT 일정"}
                {selectMenu === "ALL" && "전체 일정"}
              </h2>
            </div>
            {/* 공통 캘린더 사용 */}
            <CommonCalendar
              // 선택한 메뉴에 맞게 필터링된 일정 목록
              events={filteredEvents}
              // 날짜 클릭 시 등록 모달 열기
              onDateClick={openInsertModal}
              // 일정 클릭 시 상세 모달 열기
              onEventClick={openDetailModal}
            />
          </div>
        </div>
      </div>

      {/* 일정 등록, 상세, 수정 공통 모달 */}
      {isModalOpen && (
        <div className="calendar-modal-bg" onClick={closeModal}>
          <div className="calendar-modal" onClick={(e) => e.stopPropagation()}>
            <div className="calendar-modal-header">
              <h3>
                {modalMode === "insert" && "일정 등록"}
                {modalMode === "detail" && "일정 상세"}
                {modalMode === "update" && "일정 수정"}
              </h3>
              <button type="button" onClick={closeModal}>
                ×
              </button>
            </div>

            <div className="calendar-modal-body">
              <label>일정 제목</label>
              <input
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                disabled={modalMode === "detail"}
              />

              <label>시작일</label>
              <input
                type="datetime-local"
                name="start"
                value={form.start}
                onChange={handleChange}
                disabled={modalMode === "detail"}
              />

              <label>종료일</label>
              <input
                type="datetime-local"
                name="end"
                value={form.end}
                onChange={handleChange}
                disabled={modalMode === "detail"}
              />

              <label>내용</label>
              <textarea
                name="content"
                value={form.content}
                onChange={handleChange}
                disabled={modalMode === "detail"}
              />
            </div>

            <div className="calendar-modal-footer">
              {modalMode === "insert" && (
                <button type="button" onClick={handleInsert}>
                  등록
                </button>
              )}

              {modalMode === "detail" && (
                <>
                  <button type="button" onClick={() => setModalMode("update")}>
                    수정
                  </button>

                  <button type="button" onClick={handleDelete}>
                    삭제
                  </button>
                </>
              )}

              {modalMode === "update" && (
                <button type="button" onClick={handleUpdate}>
                  수정 완료
                </button>
              )}

              <button type="button" onClick={closeModal}>
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCalendar;
