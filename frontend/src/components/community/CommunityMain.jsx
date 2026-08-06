import axios from "axios";
import React, { useEffect, useState } from "react";
import { API_SERVER_URL } from "../../apis/commonApi";
import "../../css/Community/CommunityMain.css";
import jwtAxios from "../../apis/util/jwtUtil";
import { getCookie } from "../../apis/util/cookieUtil";
import { useNavigate } from "react-router-dom";

//날씨 지역 목록 (지역별 날씨 카드의 select 옵션 + API 조회에 사용)
const CITIES = [
  { code: "Seoul", label: "서울" }, //서울
  { code: "Suwon", label: "수원" }, //경기
  { code: "Cheonan", label: "천안" }, //충남
  { code: "Cheongju", label: "청주" }, //충북
  { code: "Jeonju", label: "전주" }, //전북
  { code: "Gwangju", label: "광주" }, //전남
  { code: "Pohang", label: "포항" }, //경북
  { code: "Changwon", label: "창원" }, //경남
  { code: "Chuncheon", label: "춘천" }, //강원
  { code: "Jeju", label: "제주" }, //제주
];

/**
 * 커뮤니티 메인(홈) 페이지
 * - 상단: 지역별 날씨 카드 / 개인화 추천 운동 카드 / 운동 루틴 바로가기 카드
 * - 하단: 탭별 게시글 미리보기 카드 그리드 (전체게시판 + 각 탭)
 */
const CommunityMain = () => {
  // 메인 화면에 필요한 게시글 데이터 (탭별 미리보기, 전체 추천글, 탭 목록)
  const [mainData, setMainData] = useState({
    byTab: {},
    all: [],
    tabs: [],
  });
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  // 지역별 날씨 정보 상태
  const [weatherMap, setWeatherMap] = useState({}); // { cityCode: weatherData }
  const [isWeatherLoading, setIsWeatherLoading] = useState(true);
  const [selectedCity, setSelectedCity] = useState("Seoul"); // 현재 선택된 지역

  // ★ 변경 - 날씨 기반 추천 대신, 로그인 사용자의 최근 루틴 3개를 제외한 운동 5개를 무작위로 받아온다.
  const [personalizedPicks, setPersonalizedPicks] = useState([]);
  const [isPickLoading, setIsPickLoading] = useState(true);
  const [pickError, setPickError] = useState(null);

  // 메인 게시글 데이터(탭별 미리보기 + 전체 추천글) 조회
  useEffect(() => {
    const fetchMainData = async () => {
      try {
        setIsLoading(true);
        const res = await axios.get(`${API_SERVER_URL}/api/community/main`);
        if (res.data?.result) {
          setMainData(res.data.result);
        }
      } catch (err) {
        console.error("메인페이지 데이터 로드 실패", err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchMainData();
  }, []);

  //지역별 날씨 불러오기 (날씨 카드는 그대로 유지, 추천 운동과는 더 이상 연결되지 않음)
  // 모든 도시의 날씨를 병렬로 조회하고, 일부 도시가 실패해도 나머지는 정상 표시되도록 allSettled 사용
  const fetchAllWeather = async () => {
    try {
      setIsWeatherLoading(true);
      const results = await Promise.allSettled(
        CITIES.map((city) =>
          axios.get(
            `${API_SERVER_URL}/api/community/weather?city=${city.code}`,
          ),
        ),
      );

      const nextMap = {};
      results.forEach((res, idx) => {
        const cityCode = CITIES[idx].code;
        if (res.status === "fulfilled" && res.value.data?.result) {
          nextMap[cityCode] = res.value.data.result;
        }
      });
      setWeatherMap(nextMap);
    } catch (err) {
      console.error("날씨 정보를 불러오지 못했습니다.", err);
    } finally {
      setIsWeatherLoading(false);
    }
  };

  useEffect(() => {
    fetchAllWeather();
  }, []);

  // ★ 추가 - 최근 루틴 3개를 제외한 운동 5개 무작위 조회.
  // 로그인 사용자 기준 개인화라 jwtAxios 사용, 비로그인 시에는 API 호출 자체를 생략하고 안내 문구를 표시
  useEffect(() => {
    const member = getCookie("member");
    if (!member?.access) {
      setIsPickLoading(false);
      setPersonalizedPicks([]);
      return;
    }

    const fetchPersonalizedPicks = async () => {
      try {
        setIsPickLoading(true);
        setPickError(null);
        const res = await jwtAxios.get(
          `${API_SERVER_URL}/api/exercise/quick-pick/personalized`,
        );
        setPersonalizedPicks(res.data || []);
      } catch (err) {
        console.error("추천 운동을 불러오지 못했습니다.", err);
        setPickError("추천 운동을 불러오지 못했습니다.");
      } finally {
        setIsPickLoading(false);
      }
    };
    fetchPersonalizedPicks();
  }, []);

  // 현재 선택된 지역의 날씨 데이터
  const selectedWeather = weatherMap[selectedCity];

  // 작성자 이름 2번째 글자 마스킹 함수 (* 처리) - userName 기준 반영
  const maskWriterName = (name) => {
    if (!name) return "익명";
    if (name.length <= 1) return name;
    return name.substring(0, 1) + "*" + name.substring(2);
  };

  const EXCLUDED_CATEGORY_KEYWORDS = ["QNA"]; //추가로 제외할 카테고리 이름
  // 메인 화면에 표시할 게시판 카드 목록 구성
  // - "전체게시판 추천글": 관리자 전용 탭 게시글과 QNA 카테고리는 제외하고 필터링
  // - 이후 각 탭별로 카드 하나씩 추가 (byTab에 해당 탭의 미리보기 목록이 들어있음)
  const boardCards = [
    {
      key: "all",
      tabName: "전체게시판 추천글",
      list: mainData.all.filter((item) => {
        const tab = mainData.tabs.find((t) => t.id === item.tabId);
        if (tab?.adminOnly) return false; // 관리자 전용(공지사항) 탭 게시글은 전체 추천글에서 제외
        const categoryName = item.categoryName || "";
        const isExcluded = EXCLUDED_CATEGORY_KEYWORDS.some((keyword) =>
          categoryName.toUpperCase().includes(keyword.toUpperCase()),
        );
        if (isExcluded) return false; // QNA 등 제외 대상 카테고리 필터링
        return true;
      }),
    },
    ...mainData.tabs.map((tab) => ({
      key: tab.id,
      tabName: tab.tabName,
      list: mainData.byTab?.[tab.id] || [],
    })),
  ];

  // "운동 루틴" 카드 클릭: 로그인 안 되어 있으면 로그인 페이지로, 되어 있으면 루틴 페이지로 이동
  const handleWriteClick = () => {
    const member = getCookie("member");
    if (!member?.access) {
      alert("로그인이 필요한 기능입니다");
      navigate("/auth/login");
      return;
    }
    navigate("/community/routine");
  };

  return (
    <div className="comMain">
      <div className="comMain-wrap">
        {/* 배너 자리 -> 날씨 + 추천운동 + 운동루틴 */}
        <div className="comMain-top">
          <div className="comMain-dashboard-layout">
            {/* 지역별 날씨 카드: select로 지역 선택 시 해당 지역 날씨 표시 */}
            <div className="board-card weather-card-wrap weather-mini-card">
              <div className="board-card-header weather-card-header">
                <h3>지역별 날씨</h3>
                <select
                  value={selectedCity}
                  onChange={(e) => setSelectedCity(e.target.value)}
                  className="weather-city-select"
                >
                  {CITIES.map((city) => (
                    <option key={city.code} value={city.code}>
                      {city.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="board-card-body weather-card-body">
                {isWeatherLoading ? (
                  <p className="board-card-empty">날씨 정보를 불러오는 중...</p>
                ) : selectedWeather ? (
                  <div className="weather-slide">
                    <div className="weather-slide-city">
                      {CITIES.find((c) => c.code === selectedCity)?.label}
                    </div>
                    <img
                      className="weather-slide-icon"
                      src={`https://openweathermap.org/img/wn/${selectedWeather.weather[0].icon}@2x.png`}
                      alt={selectedWeather.weather[0].description}
                    />
                    <div className="weather-slide-temp">
                      {Math.round(selectedWeather.main.temp)}°C
                    </div>
                    <div className="weather-slide-desc">
                      {selectedWeather.weather[0].description}
                    </div>
                  </div>
                ) : (
                  <div className="weather-slide-empty">정보 없음</div>
                )}
              </div>
            </div>
            {/*최근 루틴 3개를 제외한 운동 5개 무작위 추천 카드 */}
            <div className="board-card recommend-card">
              <div className="board-card-header">
                <h3>오늘의 추천 운동</h3>
              </div>
              <div className="board-card-body recommend-card-body">
                {!getCookie("member")?.access ? (
                  <p className="board-card-empty">
                    로그인하면 최근에 안 한 운동 위주로 추천해드려요.
                  </p>
                ) : isPickLoading ? (
                  <p className="board-card-empty">추천 운동을 불러오는 중...</p>
                ) : pickError ? (
                  <p className="board-card-empty">{pickError}</p>
                ) : personalizedPicks.length === 0 ? (
                  <p className="board-card-empty">추천할 운동이 없습니다.</p>
                ) : (
                  <ul className="recommend-exercise-list">
                    {personalizedPicks.map((ex) => (
                      <li key={ex.id} className="recommend-exercise-item">
                        <span className="recommend-exercise-name">
                          {ex.name}
                        </span>
                        <span className="recommend-exercise-meta muted">
                          {ex.target} · {ex.equipment} · {ex.sets}세트 {ex.reps}
                          회
                        </span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>

            {/* 운동 루틴 페이지로 이동하는 배너형 카드 */}
            <div className="board-card routine-card" onClick={handleWriteClick}>
              <div className="board-card-header">
                <h3>운동 루틴</h3>
              </div>
              <div className="board-card-body routine-card-body">
                <img
                  className="routine-card-img"
                  src="/images/test/routine.png"
                  alt="운동 루틴 바로가기"
                />
              </div>
            </div>
          </div>
        </div>

        {/* 게시판 카드 그리드 (전체 너비): 전체게시판 추천글 + 각 탭별 미리보기 */}
        <div className="comMain-center">
          <div className="comMain-center-con">
            <div className="comMain-board-grid">
              {isLoading ? (
                <p className="comMain-loading">불러오는 중입니다</p>
              ) : (
                boardCards.map((board) => (
                  <div className="board-card" key={board.key}>
                    <div className="board-card-header">
                      <h3>{board.tabName}</h3>
                    </div>
                    <div className="board-card-body">
                      {board.list.length === 0 ? (
                        <p className="board-card-empty">게시글이 없습니다</p>
                      ) : (
                        <ul>
                          {board.list.map((item) => (
                            <li key={item.id}>
                              <a
                                href={`/community/detail/${item.id}`}
                                className="board-item"
                              >
                                {item.thumbnail ? (
                                  <img
                                    className="board-item-thumb"
                                    src={item.thumbnail}
                                    alt=""
                                  />
                                ) : (
                                  <div className="board-item-thumb board-item-thumb-empty" />
                                )}
                                <div className="board-item-content">
                                  <p className="board-item-title">
                                    {item.title}
                                  </p>
                                  <div className="board-item-sub">
                                    <span className="board-item-writer">
                                      {maskWriterName(
                                        item.userName ||
                                          item.writer ||
                                          item.nickname ||
                                          item.memberName,
                                      )}
                                    </span>
                                    <span className="board-item-views">
                                      조회 {item.hit || 0}
                                    </span>
                                  </div>
                                </div>
                              </a>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CommunityMain;
