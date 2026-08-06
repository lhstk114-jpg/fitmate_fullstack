import axios from "axios";
import React, { useEffect, useState } from "react";
import { API_SERVER_URL } from "../apis/commonApi";
import jwtAxios from "../apis/util/jwtUtil";
import { useSelector } from "react-redux";
import "../css/main/Main.css";
//배너 swiper 관련
import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Pagination, Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/pagination";
import "swiper/css/navigation";
import CommonCalendar from "./common/calendar/CommonCalendar";
import { useNavigate } from "react-router-dom";
import { getMyMembership } from "../apis/shop/memberProductApi";

const Main = () => {
  const navigate = useNavigate();
  //로그인 여부 판단
  const user = useSelector((state) => state.loginSlice); //user 정보
  const isLogin = !!user?.userEmail;
  const API_URL = API_SERVER_URL;

  // 상태 관리
  const [selectMenu, setSelectMenu] = useState("notice");
  const [bestTab, setBestTab] = useState("추천");
  const [defaultCommunityList, setDefaultCommunityList] = useState([]);
  const [communityList, setCommunityList] = useState([]);

  const [productList, setProductList] = useState([]);

  const [noticeList, setNoticeList] = useState([]);
  const [popupList, setPopupList] = useState([]);
  const [myMembership, setMyMembership] = useState([]);
  const [calendarEvents, setCalendarEvents] = useState([]);

  //오늘 하루 그만 보기
  const closeToday = (popupId) => {
    const today = new Date().toISOString().slice(0, 10);
    localStorage.setItem(`mainPopupHideDate_${popupId}`, today);

    setPopupList((prev) => prev.filter((popup) => popup.id !== popupId));
  };
  //팝업 닫기
  const closePopup = (popupId) => {
    setPopupList((prev) => prev.filter((popup) => popup.id !== popupId));
  };

  // 캘린더 조회
  const getCalendarList = async () => {
    if (!isLogin) {
      setCalendarEvents([]);
      return;
    }
    try {
      const res = await jwtAxios.get(`${API_URL}/api/calendar/scheduleList`, {
        params: { eventType: "ALL" },
      });
      setCalendarEvents(res.data || []);
    } catch (err) {
      console.error("캘린더 조회 오류:", err);
      setCalendarEvents([]);
    }
  };
  // 내 이용권 조회
  const loadMyMembership = async () => {
    if (!isLogin) {
      setMyMembership([]);
      return;
    }

    try {
      const data = await getMyMembership();
      console.log(data);
      setMyMembership(data || []);
    } catch (err) {
      console.error("내 이용권 조회 실패:", err);
      setMyMembership([]);
    }
  };

  // 추천 리스트 가져오는 함수
  const getMainData = async () => {
    try {
      const res = isLogin
        ? await jwtAxios.get(`${API_URL}/api/main`)
        : await axios.get(`${API_URL}/api/main`);

      // 커뮤니티 리스트
      const mainCommunityList = Array.isArray(res.data.communityList)
        ? res.data.communityList
        : [];

      setCommunityList(mainCommunityList);
      setDefaultCommunityList(mainCommunityList);

      // 백엔드에서 조합한 최종 상품 최대 8개
      const mainProductList = Array.isArray(res.data.productList)
        ? res.data.productList
        : [];

      setProductList(mainProductList.slice(0, 8));

      // 공지사항
      setNoticeList(
        Array.isArray(res.data.noticeList) ? res.data.noticeList : [],
      );

      // 팝업
      const today = new Date().toISOString().slice(0, 10);

      const visiblePopupList = (
        Array.isArray(res.data.popupList) ? res.data.popupList : []
      ).filter((popup) => {
        const hideDate = localStorage.getItem(`mainPopupHideDate_${popup.id}`);

        return hideDate !== today;
      });

      setPopupList(visiblePopupList.slice(0, 2));
    } catch (err) {
      console.error("메인 데이터 조회 오류:", err);

      setCommunityList([]);
      setDefaultCommunityList([]);
      setProductList([]);
      setNoticeList([]);
      setPopupList([]);
    }
  };

  // 선택한 게시판 탭의 조회수 높은 게시글 TOP 5 조회
  const getBestCommunityList = async (tabName) => {
    setBestTab(tabName);
    // 추천 탭은 메인 최초 조회에서 받은 목록 다시 사용
    if (tabName === "추천") {
      setCommunityList(defaultCommunityList.slice(0, 5));
      return;
    }
    try {
      const res = await axios.get(`${API_URL}/api/main/community/best`, {
        params: {
          tabName,
        },
      });
      setCommunityList(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error("베스트 게시글 조회 오류:", err);
      setCommunityList([]);
    }
  };

  // 메인 데이터 조회
  useEffect(() => {
    setBestTab("추천");
    getMainData();
  }, [isLogin]);

  // 로그인 상태에 따라 개인 캘린더 조회
  useEffect(() => {
    getCalendarList();
    loadMyMembership();
  }, [isLogin]);

  // 상위 8개 상품 가져오기
  const displayProducts = productList.slice(0, 8);

  useEffect(() => {
    console.log("메인 상품 목록:", productList);
    console.log("메인 상품 개수:", productList.length);
  }, [productList]);
  return (
    <div className="main-container">
      {/* 팝업 모달 */}
      {popupList.length > 0 && (
        <div className="main-popup-area">
          {popupList.map((popup, index) => (
            <div className="main-popup" key={popup.id}>
              <button
                className="main-popup-close"
                onClick={() => closePopup(popup.id)}
              >
                ×
              </button>
              <a href={popup.linkUrl || "#"}>
                {popup.newFileName && (
                  <img
                    src={`${API_SERVER_URL}/upload/popup/${popup.newFileName}`}
                    alt={popup.title}
                  />
                )}
              </a>
              <h3>{popup.title}</h3>
              <p>{popup.content}</p>
              <div className="main-popup-bottom">
                <button onClick={() => closeToday(popup.id)}>
                  오늘 그만보기
                </button>
                <button onClick={() => closePopup(popup.id)}>닫기</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 1. 메인 슬라이드 배너 (1200 x 373 규격) */}
      <section className="main-banner-section">
        <div className="banner-wrapper">
          <Swiper
            modules={[Autoplay, Pagination, Navigation]}
            spaceBetween={0}
            slidesPerView={1}
            loop={true}
            autoplay={{ delay: 3500, disableOnInteraction: false }}
            pagination={{ clickable: true }}
            navigation={true}
            className="main-banner-swiper"
          >
            <SwiperSlide>
              <a href="/products/premium">
                <img src="/images/main/event.png" alt="프리미엄 혜택 이벤트" />
              </a>
            </SwiperSlide>
            <SwiperSlide>
              <a href="/community/index">
                <img
                  src="/images/main/communitybanner.png"
                  alt="메인 커뮤니티 배너"
                />
              </a>
            </SwiperSlide>
          </Swiper>
        </div>
      </section>

      {/* 2. 게시판 & 사용자 캘린더 / 이용권 영역 */}
      <section className="main-middle-section">
        <div className="middle-container">
          {/* 좌측: 공지사항 & 베스트 게시글 */}
          <div className="board-card">
            <div className="board-header-tabs">
              <button
                className={`tab-btn ${selectMenu === "notice" ? "active" : ""}`}
                onClick={() => setSelectMenu("notice")}
              >
                공지사항
              </button>
              <button
                className={`tab-btn ${selectMenu === "best" ? "active" : ""}`}
                onClick={() => setSelectMenu("best")}
              >
                베스트 게시글
              </button>
            </div>

            <div className="board-content">
              {selectMenu === "notice" && (
                <ul className="notice-list">
                  {Array.isArray(noticeList) && noticeList.length > 0 ? (
                    noticeList.slice(0, 3).map((notice) => (
                      <li key={notice.id} className="board-item">
                        <a href={`/community/detail/${notice.id}`}>
                          <span className="notice-badge">공지</span>
                          <span className="item-title">{notice.title}</span>
                        </a>
                      </li>
                    ))
                  ) : (
                    <li className="empty-msg">등록된 공지사항이 없습니다.</li>
                  )}
                </ul>
              )}

              {selectMenu === "best" && (
                <div className="best-wrapper">
                  <div className="sub-tab-group">
                    {["추천", "운동정보", "자유게시판"].map((tab) => (
                      <button
                        key={tab}
                        className={`sub-tab ${bestTab === tab ? "active" : ""}`}
                        onClick={() => getBestCommunityList(tab)}
                      >
                        {tab === "운동정보" ? "운동게시판" : tab}
                      </button>
                    ))}
                  </div>
                  <ul className="best-list">
                    {Array.isArray(communityList) &&
                    communityList.length > 0 ? (
                      communityList.slice(0, 3).map((item) => (
                        <li key={item.id} className="board-item">
                          <a href={`/community/detail/${item.id}`}>
                            <span className="item-title">{item.title}</span>
                          </a>
                        </li>
                      ))
                    ) : (
                      <li className="empty-msg">게시글이 존재하지 않습니다.</li>
                    )}
                  </ul>
                </div>
              )}
            </div>
          </div>

          {/* 우측: 캘린더 및 보유 이용권 */}
          <div className="user-dashboard-card">
            {isLogin ? (
              <>
                <div className="calendar-box">
                  <CommonCalendar
                    events={calendarEvents}
                    onDateClick={() => navigate("/mypage/schedule")}
                    onEventClick={() => navigate("/mypage/schedule")}
                  />
                </div>

                <div className="membership-box">
                  <div className="membership-header">
                    <h4>내 보유 이용권</h4>
                    <button
                      className="more-btn"
                      onClick={() => navigate("/mypage/memberships")}
                    >
                      전체보기 &rsaquo;
                    </button>
                  </div>
                  <div className="membership-list">
                    {myMembership.length === 0 ? (
                      <p className="no-membership">
                        보유 중인 이용권이 없습니다.
                      </p>
                    ) : (
                      myMembership.slice(0, 2).map((item) => (
                        <div className="membership-item" key={item.id}>
                          <div className="item-info">
                            <span className="item-name">
                              {item.productName}
                            </span>
                            <span className="item-count">
                              {item.productType === "PT"
                                ? `잔여 ${item.remainingCount}/${item.totalCount}회`
                                : "이용 중"}
                            </span>
                          </div>
                          <span className="item-date">
                            ~{item.endDate?.substring(0, 10)}
                          </span>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </>
            ) : (
              <div className="login-prompt-box">
                <div className="prompt-icon">📅</div>
                <h3>나의 운동 일정을 관리해보세요</h3>
                <p>
                  로그인하시면 개인 스케줄 및 PT 일정을
                  <br />
                  한눈에 쉽게 확인하실 수 있습니다.
                </p>
                <button
                  className="login-btn"
                  onClick={() => navigate("/auth/login")}
                >
                  로그인하고 일정 확인하기
                </button>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* 3. 운동 루틴 보러가기 이벤트를 알리는 배너 */}
      <section className="routine-banner-section">
        <div
          className="routine-banner"
          onClick={() => navigate("/community/routine")}
        >
          <div className="routine-content">
            <span className="routine-tag">ROUTINE COMMUNITY</span>
            <h2>나에게 딱 맞는 맞춤형 운동 루틴 찾아보기</h2>
            <p>전문 트레이너와 유저들이 직접 작성한 인기 운동 꿀팁 모음!</p>
          </div>
          <button className="routine-btn">루틴 보러가기 &rarr;</button>
        </div>
      </section>

      {/* 4. 인기 상품 목록 (8개 배치) */}
      <section className="main-products-section">
        <div className="products-container">
          <div className="section-header">
            <h3>🔥 베스트 인기 상품</h3>
            <button className="more-btn" onClick={() => navigate("/shop")}>
              전체 상품보기 &rsaquo;
            </button>
          </div>

          <div className="product-grid">
            {displayProducts.length > 0 ? (
              displayProducts.map((product) => (
                <div
                  className="product-card"
                  key={product.id}
                  onClick={() => navigate(`/products/detail/${product.id}`)}
                >
                  <div className="product-img-wrapper">
                    <img
                      src={
                        product.fileDtos?.[0]?.newFileName
                          ? `${API_SERVER_URL}/upload/product/${product.fileDtos[0].newFileName}`
                          : "/images/test/placeholder.png"
                      }
                      alt={product.productName}
                    />
                  </div>
                  <div className="product-info">
                    <h4 className="product-name">{product.productName}</h4>
                    <p className="product-price">
                      {product.price?.toLocaleString()}
                      <span className="unit">원</span>
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-products">등록된 상품이 없습니다</div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default Main;
