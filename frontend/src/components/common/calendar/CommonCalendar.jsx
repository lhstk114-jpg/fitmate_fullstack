import React from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import koLocale from "@fullcalendar/core/locales/ko";
import "../../../css/common/CommonCalendar.css";

/* ===================== CommonCalendar 사용 방법 =====================
CommonCalendar는 캘린더 화면을 담당하는 공통 컴포넌트입니다.

CommonCalendar 내부에서는 일정 등록, 수정, 삭제 API호출을 담당하지 않습니다.
-------------------------------------------------------------------
1. eventType 지정
eventType으로 CalendarService에서 조회할 일정을 구분.

eventType예시(임시)
ALL            : 전체 일정
SUBSCRIPTION   : 구독 일정
PT             : PT 일정
WORKOUT        : 운동 기록
PERSONAL       : 개인 일정
-------------------------------------------------------------------
2. 일정 조회
각 페이지에서 memberId와 eventType으로 일정을 조회.

const [calendarEvents, setCalendarEvents] = useState([]);

const eventType = "PERSONAL";

const getCalendarList = async () => {
  // memberId 또는 eventType이 없으면
  // 빈 캘린더 표시
  if (!memberId || !eventType) {
    setCalendarEvents([]);
    return;
  }
  const res = await jwtAxios.get(
    `/api/calendar/${memberId}`,
    {
      params: {
        eventType,
      },
    },
  );
  setCalendarEvents(res.data || []);
};

-------------------------------------------------------------------
3. CommonCalendar 사용

조회한 일정 목록을 events로 전달.

<CommonCalendar
  events={calendarEvents}
  onDateClick={openInsertModal}
  onEventClick={openDetailModal}
/>
-> calendar 구현
-------------------------------------------------------------------
4. 모달 처리

날짜 클릭 시 등록 모달,
일정 클릭 시 상세 또는 수정 모달은
각 페이지에서 직접 구현.

const openInsertModal = (info) => { ... }
const openDetailModal = (info) => { ... }
-------------------------------------------------------------------
5. 일정 등록, 수정, 삭제

등록, 수정, 삭제 API 호출도
각 페이지에서 직접 구현.

작업 완료 후 일정을 다시 조회.
await getCalendarList();
=================================================================== */

const CommonCalendar = ({
  // 부모 페이지에서 조회한 캘린더 일정 목록
  events = [],
  // 일정 클릭 시 부모 페이지에서 실행할 함수
  onEventClick,
  // 날짜 클릭 시 부모 페이지에서 실행할 함수
  onDateClick,
  // 날짜 선택 범위 제한가능한 옵션
  validRange,
  showHeader = true,
}) => {
  // CalendarDto의 eventType에 따라 일정별 CSS 클래스 적용
  const getEventClassNames = (info) => {
    // CalendarDto의 eventType은 FullCalendar에서 extendedProps에 저장됨
    const eventType = info.event.extendedProps.eventType?.toUpperCase();
    switch (eventType) {
      // 구독 일정
      case "SUBSCRIPTION":
        return ["calendar-event-subscription"];
      // PT 일정
      case "PT":
        return ["calendar-event-pt"];
      // 운동 기록
      case "WORKOUT":
        return ["calendar-event-workout"];
      // 개인 일정
      case "PERSONAL":
        return ["calendar-event-personal"];
      // eventType이 없거나 정의되지 않은 일정
      default:
        return ["calendar-event-default"];
    }
  };

  return (
    <div className="common-calendar">
      <FullCalendar
        // FullCalendar에서 사용할 플러그인
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        // 캘린더 한국어 설정
        locale={koLocale}
        // 캘린더가 처음 열렸을 때 월간 화면 표시
        initialView="dayGridMonth"
        // 부모 페이지에서 전달받은 일정 목록
        validRange={validRange}
        // 날짜 선택 범위 제한 가능한 옵션
        events={events}
        // 일정 클릭 이벤트
        // 실제 상세 조회 또는 수정 모달 처리는 부모 페이지에서 담당
        eventClick={onEventClick}
        // 날짜 클릭 이벤트
        // 실제 등록 모달 처리는 부모 페이지에서 담당
        dateClick={onDateClick}
        // 일정 종류별 CSS 클래스 적용
        eventClassNames={getEventClassNames}
        // 날짜 영역 선택 가능
        selectable={true}
        // 일정 드래그 이동 및 크기 변경 비활성화
        editable={false}
        // 주간 및 일간 화면에서 현재 시간 표시
        nowIndicator={true}
        // 한 날짜에 표시할 최대 일정 개수
        // 3개를 초과하면 +n more 형태로 표시
        dayMaxEvents={3}
        // 캘린더 높이를 내용에 맞게 자동 조절
        height="auto"
        // 캘린더 상단 버튼 구성
        headerToolbar={
          showHeader
            ? {
                left: "prev,next", // 🔥 'today' 제거: 좌측 이전/다음(< >) 화살표만 유지
                center: "title",   // 중앙 연도/월 제목 유지
                right: "",         // 우측 월/주/일 선택 버튼 숨김
              }
            : false
        }
        // 일정에 표시되는 시간 형식
        eventTimeFormat={{
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }}
        // 주간 및 일간 화면의 시간축 표시 형식
        slotLabelFormat={{
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }}
      />
    </div>
  );
};

export default CommonCalendar;
