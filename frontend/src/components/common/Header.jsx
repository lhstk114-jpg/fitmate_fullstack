import React, { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../../css/common/Header.css";
import { useDispatch, useSelector } from "react-redux";
import loginSlice, { logout, logoutAsync } from "../../store/slices/loginSlice";
import axios from "axios";
import { API_SERVER_URL } from "../../apis/commonApi";

const Header = () => {
  //변수 선언
  const dispatch = useDispatch();
  const navigate = useNavigate();

  //로그인 여부 판단
  const { memberData } = useSelector((state) => state.loginSlice); //user 정보
  const isLogin = !!memberData?.result?.userEmail;

  //관리자페이지 허용 여부
  const isAdminOrManager =
    memberData?.result?.role === "ADMIN" ||
    memberData?.result?.role === "MANAGER";
  //로그아웃
  const logoutFn = async () => {
    //기존 그냥 로그아웃함수만 불러오던것 -> 비동기청크로 실제 customLogoutFilter를 거칠수있게 설정
    try {
      //로그아웃이 될때까지 기다림
      await dispatch(logoutAsync()).unwrap();
      alert("로그아웃 되었습니다.");
      navigate("/");
    } catch (error) {
      //로그아웃api가 실패하거나 서버가 다운되어있으면 로그를 남기고, 멤버쿠키만 제거하는 기존 로그아웃으로 진행
      console.error("로그아웃 처리 중 에러 발생:", error);
      dispatch(logout());
      navigate("/");
    }
  };
  //드롭다운 메뉴 위치조정
  const shopRef = useRef(null);
  const communityRef = useRef(null);
  const [menuPosition, setMenuPosition] = useState(0);
  // 검색기능변수
  const [keyword, setKeyword] = useState("");
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  //메뉴 depth 기능 -> onMouseEnter시 오픈
  const [activeMenu, setActiveMenu] = useState(null);

  // 커뮤니티 탭 + 카테고리 목록
  const [communityTabs, setCommunityTabs] = useState([]);

  useEffect(() => {
    const fetchCommunityTabs = async () => {
      try {
        const res = await axios.get(`${API_SERVER_URL}/api/community/tabList`);
        setCommunityTabs(res.data?.result || []);
      } catch (err) {
        console.error("커뮤니티 탭 로딩 실패", err);
      }
    };
    fetchCommunityTabs();
  }, []);
  // 탭 이름으로 FAQ 탭 조회 - 탭 이름 변경되면 변경!
  const faqTab = communityTabs.find((tab) => tab.tabName === "건의게시판");

  //header 검색기능 -> 나중에 추가
  // const handleSearch = (e) => {
  //   e.preventDefault();
  //   if (!keyword.trim()) return;

  //   // 검색 페이지로 검색어를 포함해 이동
  //   setIsSearchOpen(false);
  //   navigate(`/search?search=${encodeURIComponent(keyword)}`); //검색어 encoding ->데이터에 포함된 특수문자나 공백이 URL의 구조를 깨뜨리거나 변조되는 것을 방지
  //   setKeyword(""); // 입력창 비우기
  // };
  return (
    <>
      <div className="header" onMouseLeave={() => setActiveMenu(null)}>
        <div className="header-con">
          <div className="nav-wrap">
            <div className="gnb-left">
              <ul>
                <li
                  ref={shopRef}
                  onMouseEnter={() => {
                    setActiveMenu("shop");
                    setMenuPosition(shopRef.current.offsetLeft);
                  }}
                >
                  <Link to={`/shop`}>스토어</Link>
                </li>
                <li
                  ref={communityRef}
                  onMouseEnter={() => {
                    setActiveMenu("community");
                    setMenuPosition(communityRef.current.offsetLeft);
                  }}
                >
                  {" "}
                  <Link to={`/community`}>게시판</Link>
                </li>
                <li>
                  {faqTab ? (
                    <Link to={`/community/tab/${faqTab.id}`} className="header-faq">FAQ</Link>
                  ) : (
                    <span className="header-faq">FAQ</span>
                  )}
                </li>
              </ul>
            </div>
            <div className="gnb-logo">
              <Link to={`/`}>
                <h1>
                  <img src="/images/main/가로로고.png" alt="logo" />
                </h1>
              </Link>
            </div>
            {/* 검색기능 -> 나중에 추가 */}
            {/* <div className={`header_search ${isSearchOpen ? "active" : ""}`}>
              <form className="search-bar" onSubmit={handleSearch}>
                <div className="search_box">
                  <input
                    type="text"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    placeholder="검색어를 입력하세요"
                  />
                  <button type="submit" className="search-btn">
                    검색
                  </button>
                </div>
                <span
                  className="search-close"
                  onClick={() => {
                    setIsSearchOpen(false);
                    setKeyword(""); // 검색어도 같이 초기화
                  }}
                >
                  ×
                </span>
              </form>
            </div> */}
            <div className="gnb-right">
              <ul>
                {/* 검색기능용 돋보기 아이콘 */}
                {/* <span
                  className="header_auth_btn header_search_btn"
                  onClick={() => setIsSearchOpen(true)}
                >
                  <img
                    src="/images/magnifier-icon.png"
                    alt="돋보기 아이콘"
                    style={{ width: "25px", height: "25px" }}
                  />
                </span> */}
                {!isLogin && (
                  <>
                    <li>
                      <Link to={`/auth/login`}>로그인</Link>
                    </li>
                    <li>
                      <Link to={`/auth/join`}>회원가입</Link>
                    </li>
                  </>
                )}
                {isLogin && isAdminOrManager && (
                  <li>
                    <Link to="/admin">관리자</Link>
                  </li>
                )}
                {isLogin && memberData?.result?.role !== "ADMIN" && (
                  <li>
                    <Link to="/mypage">{memberData?.result?.userName}님</Link>
                  </li>
                )}

                {isLogin && (
                  <>
                    <li>
                      <Link to={`/cart`}>장바구니</Link>
                    </li>
                    <li>
                      <Link to={`/mypage`}>내정보</Link>
                    </li>
                    <li>
                      <button className="header-logout-btn" onClick={logoutFn}>
                        로그아웃
                      </button>
                    </li>
                  </>
                )}
              </ul>
            </div>
          </div>
          <div
            className={`header_depth ${activeMenu ? "active" : ""}`}
            style={{ left: `${menuPosition}px` }}
          >
            {activeMenu === "shop" && (
              <ul>
                <li>
                  <Link to={`/shop/index`}>스토어</Link>
                  <Link to="/products?productType=GOODS&category=운동기구">운동기구</Link>
                  <Link to="/products?productType=GOODS&category=식품">식품</Link>
                  <Link to="/products?productType=GOODS&category=트레이닝복">트레이닝복</Link>
                </li>
                <li>
                  <Link to={`/shop/index`}>구독상품</Link>
                  <Link to="/products/premium">FitMate Plus+</Link>
                  <Link to="/products?productType=GYM">헬스장</Link>
                  <Link to="/products?productType=PT">PT</Link>
                </li>
              </ul>
            )}
            {activeMenu === "community" && (
              <ul>
                {communityTabs.map((tab) => (
                  <li key={tab.id}>
                    <Link
                      to={`/community/tab/${tab.id}`}
                      onClick={() => setActiveMenu(null)}
                    >
                      {tab.tabName}
                    </Link>
                    {tab.categoryList?.map((cat) => (
                      <Link
                        key={cat.id}
                        to={`/community/tab/${tab.id}/category/${cat.id}`}
                        onClick={() => setActiveMenu(null)}
                      >
                        {cat.categoryName}
                      </Link>
                    ))}
                  </li>
                ))}

                {/* 운동루틴을 마지막 li 항목으로 배치 */}
                <li>
                  <Link
                    to={`/community/routine`}
                    onClick={() => setActiveMenu(null)}
                  >
                    운동루틴
                  </Link>
                </li>
              </ul>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default Header;
