import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import axios from "axios";
import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Pagination, Navigation } from "swiper/modules";

import "swiper/css";
import "swiper/css/pagination";
import "swiper/css/navigation";

const ShopIndex = () => {
  const navigate = useNavigate();
  const user = useSelector((state) => state.loginSlice);
  const isLogin = !!user?.userEmail;
  const API_URL = API_SERVER_URL;


  const [productList, setProductList] = useState([]);
  const [trainerList, setTrainerList] = useState([]);
  const [products, setProducts] = useState([]);

  const getTrainerList = async () => {
    try {
      const res = isLogin
        ? await jwtAxios.get(`${API_URL}/api/trainer/list`)
        : await axios.get(`${API_URL}/api/trainer/list`);
      const trainers = res?.data?.content || res?.data || [];
      const fixedTrainers = [
        trainers[0] || null,
        trainers[1] || null,
        trainers[2] || null,
      ];
      setTrainerList(fixedTrainers);
    } catch (err) {
      console.error("트레이너 조회 실패", err);
      setTrainerList([null, null, null]);
    }
  };
  //상품 조회
  const getProductData = async () => {
    try {
      const res = isLogin
        ? await jwtAxios.get(`${API_URL}/api/main`)
        : await axios.get(`${API_URL}/api/main`);

      setProductList(res.data.productList || []);
    } catch (err) {
      console.error(err);
    }
  };

  //인기 상품 TOP 8 조회
  useEffect(() => {
    console.log("useEffect 실행");
    const getTopProducts = async () => {
      try {
        const res = isLogin
          ? await jwtAxios.get(`${API_URL}/api/product/top-sales`)
          : await axios.get(`${API_URL}/api/product/top-sales`);
          console.log(res.data);
          console.log(res.data.length)
        setProducts(res?.data || []);
      } catch (err) {
        console.error("인기 상품 조회 실패:", err?.response?.data || err?.message || err);
        setProducts([]);
      }
    };

    getTopProducts();
  }, [isLogin]);

  // 메인 데이터 & 트레이너 목록 조회 실행
  useEffect(() => {
    getProductData();
    getTrainerList();
  }, [isLogin]);

  return (
    <div className="shopIndex">
      <div className="shopIndex-wrap">
        {/* 상단 스와이퍼 배너 섹션 */}
        <div className="shopIndex-top">
          <div className="shop-slides-container">
            <Swiper
              modules={[Autoplay, Pagination, Navigation]}
              spaceBetween={0}
              slidesPerView={1}
              loop={true}
              autoplay={{
                delay: 4000,
                disableOnInteraction: false,
              }}
              pagination={{ clickable: true }}
              navigation={true}
              className="shop-banner-swiper"
            >
              <SwiperSlide>
                <a href="/products?productType=GYM" className="banner-slide-link">
                  <div className="banner-img-wrapper">
                    <img src="/images/shop/gymbanner.png" alt="헬스장 배너" />
                    <div className="banner-overlay">
                      <span className="badge">HOT FITNESS</span>
                      <h2>프리미엄 헬스장 전용 이용권</h2>
                      <p>FitMate 제휴 센터 최대 30% 할인 혜택</p>
                    </div>
                  </div>
                </a>
              </SwiperSlide>
              <SwiperSlide>
                <a href="/products?productType=PT" className="banner-slide-link">
                  <div className="banner-img-wrapper">
                    <img src="/images/shop/trainerbanner.png" alt="트레이너 배너" />
                    <div className="banner-overlay">
                      <span className="badge">1:1 MATCHING</span>
                      <h2>검증된 전문 트레이너 PT</h2>
                      <p>나에게 딱 맞는 트레이너를 맞춤 추천받아 보세요</p>
                    </div>
                  </div>
                </a>
              </SwiperSlide>
            </Swiper>
          </div>
        </div>

        {/* 전문 트레이너 섹션 */}
        <section className="trainer-section">
          <div className="section-header">
            <h2>FitMate 전문 트레이너</h2>
            <p className="section-sub">엄격한 검증을 거친 분야별 수석 트레이너진입니다.</p>
          </div>

          <div className="trainer-list">
            {trainerList.map((trainer, index) => (
              <div className="trainer-card" key={index}>
                {trainer ? (
                  <>
                    <div className="trainer-img-box">
                      <img
                        src={
                          trainer?.profileImage
                            ? `${API_SERVER_URL}/upload/member/${trainer.profileImage}`
                            : "/images/default-profile.png"
                        }
                        alt="트레이너 프로필"
                      />
                      <span className="trainer-status-badge">인기 PT</span>
                    </div>

                    <div className="trainer-info">
                      <h3>{trainer.name} <span className="trainer-title">트레이너</span></h3>
                      <p className="specialty">{trainer.specialty || "전문 분야 준비중"}</p>

                      <div className="trainer-meta">
                        <span>경력: {trainer.career || "정보 준비중"}</span>
                        {trainer.certificate && (
                          <span className="cert-tag">{trainer.certificate}</span>
                        )}
                      </div>

                      <button onClick={() => navigate("/products?productType=PT")} className="trainer-btn" >예약하기</button>
                    </div>
                  </>
                ) : (
                  <div className="empty-trainer-card">
                    <div className="empty-profile">+</div>
                    <h3>트레이너 준비중</h3>
                    <p>곧 새로운 트레이너가 등록됩니다</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </section>

        {/* 인기/추천 상품 섹션 */}
        <section className="shopIndex-bottom">
          <div className="section-header">
            <h2>추천 상품 목록</h2>
            <Link to="/products" className="view-more">전체보기 &gt;</Link>
          </div>

          <div className="shopIndex-bottom-productList">
            {products.slice(0, 8).map((product) => {
              const thumbnail = product.fileDtos?.find(
                (file) => file.imageType === "THUMBNAIL"
              );
              return (
                <div className="product-card" key={product.id} onClick={() => navigate(`/products/detail/${product.id}`)}>
                  <div className="product-thumb-container">
                    <img
                      src={thumbnail ? `${API_SERVER_URL}/upload/product/${thumbnail.newFileName}` : "/images/no-image.png"}
                      alt={product.productName}
                    />
                    <div className="product-hover-actions">
                    </div>
                  </div>

                  <div className="product-details">
                    <span className="product-category">FitMate Shop</span>
                    <div className="product-name">{product.productName}</div>
                    <div className="product-price-box">
                      <span className="product-price">
                        {product.price?.toLocaleString()}
                        <span className="unit">원</span>
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </div>
    </div>
  );
};

export default ShopIndex;