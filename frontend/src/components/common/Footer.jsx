import React, { useEffect, useState } from "react";
import "../../css/common/Footer.css";
import ChatBot from "../chatbot/ChatBot";

const Footer = () => {
  //스크롤 버튼 -> scroll y ->300이면 top버튼 show
  const [showTopBtn, setShowTopBtn] = useState(false);
  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 300) {
        setShowTopBtn(true);
      } else {
        setShowTopBtn(false);
      }
    };
    window.addEventListener("scroll", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);
  //스크롤 최상위 버튼
  const moveToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  return (
    <>
      <div className="footer">
        <div className="footer-con">
          <div className="footer-left">
            <ul>
              <li>FitMate</li>
            </ul>
          </div>
          <div className="footer-mid1">
            <ul>
              <li>상호명 및 호스팅 서비스 제공 : FitMate</li>
              <li>대표이사 : 김우송, 김주희, 이용근, 이현성</li>
              <li>서울특별시 노원구 상계동 593-1 화일빌딩 6층</li>
              <li>사업자 등록번호 : 110-12-34567</li>
              <li>통신판매업신고 : 2026-서울노원-0730</li>
            </ul>
          </div>
          <div className="footer-mid2">
            <ul>
              <li>365고객센터 | 전자금융거래분쟁처리담당</li>
              <li className="footer-tel">1500-0000 (유료)</li>
              <li>서울특별시 노원구 상계동 593-1 화일빌딩 3층</li>
              <li>email : fitmate@email.com</li>
            </ul>
          </div>
          <div className="footer-right">
            <ul>
              <li>채무지급보증 안내</li>
              <li>당사는 고객님이 현금 결제 한 금액에 대해</li>
              <li>채무지급보중 계약을 체결하여</li>
              <li>안전거래를 보장하고 있습니다.</li>
            </ul>
          </div>
          {/* 스크롤 버튼 */}
          {showTopBtn && (
            <button className="top-btn" onClick={moveToTop}>
              Top ↑
            </button>
          )}
          {/* 챗봇 */}
          <ChatBot />
        </div>
      </div>
    </>
  );
};

export default Footer;
