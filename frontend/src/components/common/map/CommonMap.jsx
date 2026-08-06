/* ===================== CommonMap =====================
공통 지도 컴포넌트

기능
1. 전달받은 주소를 좌표로 변환
2. Kakao Map을 생성하여 지도 표시
3. 검색한 위치에 고정 마커 표시

props
- address : 표시할 주소
- width : 지도 가로 크기 (기본값 100%)
- height : 지도 세로 크기 (기본값 400px)
- level : 지도 확대 레벨 (기본값 3)

주의사항
- 주소 검색 기능은 포함하지 않습니다.
- 주소 검색은 AddressModal에서 처리.
- CRUD 등은 부모 컴포넌트에서 따로 처리.
===================================================== */
/*======================= CommonMap 공통 컴포넌트 사용 예시 ======================
// 표시할 주소
const address = "서울특별시 노원구 상계동 593-1";

// 원하는 크기 및 확대 레벨 설정
<CommonMap
  address={address}     // 표시할 주소
  width="100%"          // 지도 가로 크기
  height="400px"        // 지도 세로 크기
  level={3}             // 지도 확대 레벨
/>
=========================================================================*/

import React, { useEffect, useRef } from "react";
import axios from "axios";
import { API_SERVER_URL } from "../../../apis/commonApi";

const CommonMap = ({
  address,
  width = "100%",
  height = "400px",
  level = 3,
}) => {
  // 지도가 표시될 div
  const mapRef = useRef(null);

  // Kakao Map 객체
  const mapInstance = useRef(null);

  // 지도에 표시되는 마커 객체
  const markerInstance = useRef(null);

  useEffect(() => {
    let isMounted = true;

    // 전달받은 주소를 좌표로 변환하여 지도와 마커 표시
    const displayAddress = () => {
      if (
        !isMounted ||
        !address ||
        !mapInstance.current ||
        !window.kakao?.maps?.services
      ) {
        return;
      }

      const geocoder = new window.kakao.maps.services.Geocoder();

      geocoder.addressSearch(address, (result, status) => {
        if (!isMounted) {
          return;
        }

        if (status !== window.kakao.maps.services.Status.OK) {
          console.error("주소에 해당하는 위치를 찾을 수 없습니다.", address);
          return;
        }

        // 주소 검색 결과의 위도, 경도
        const latitude = Number(result[0].y);
        const longitude = Number(result[0].x);

        const position = new window.kakao.maps.LatLng(latitude, longitude);

        // 검색된 위치로 지도 이동
        mapInstance.current.setCenter(position);
        mapInstance.current.setLevel(level);

        // 기존 마커 제거
        if (markerInstance.current) {
          markerInstance.current.setMap(null);
        }

        // 검색된 위치에 고정 마커 표시
        markerInstance.current = new window.kakao.maps.Marker({
          position,
          map: mapInstance.current,
          draggable: false,
        });
      });
    };

    // Kakao Map 생성
    const createMap = () => {
      if (!isMounted || !mapRef.current || !window.kakao?.maps) {
        return;
      }

      // 지도가 이미 생성된 경우 주소만 다시 표시
      if (mapInstance.current) {
        displayAddress();
        return;
      }

      // 주소 검색 전 기본 위치
      const defaultPosition = new window.kakao.maps.LatLng(37.5665, 126.978);

      mapInstance.current = new window.kakao.maps.Map(mapRef.current, {
        center: defaultPosition,
        level,
      });

      displayAddress();
    };

    // Kakao Maps SDK 로드
    const loadKakaoMap = async () => {
      try {
        // SDK가 이미 로드된 경우
        if (window.kakao?.maps) {
          window.kakao.maps.load(createMap);
          return;
        }

        // 다른 컴포넌트에서 SDK를 로드 중인 경우
        const existingScript = document.getElementById("kakao-map-sdk");

        if (existingScript) {
          const handleLoad = () => {
            window.kakao.maps.load(createMap);
          };

          existingScript.addEventListener("load", handleLoad);

          return;
        }

        // 백엔드에서 Kakao JavaScript Key 조회
        const res = await axios.get(`${API_SERVER_URL}/api/map/kakaoMap`);

        const kakaoKey = res.data.kakaoKey;

        if (!kakaoKey) {
          throw new Error("Kakao JavaScript Key가 없습니다.");
        }

        // Kakao Maps SDK 동적 로드
        const script = document.createElement("script");

        script.id = "kakao-map-sdk";
        script.async = true;
        script.src =
          `https://dapi.kakao.com/v2/maps/sdk.js` +
          `?autoload=false` +
          `&appkey=${kakaoKey}` +
          `&libraries=services`;

        script.onload = () => {
          window.kakao.maps.load(createMap);
        };

        script.onerror = () => {
          console.error("Kakao Maps SDK 로드에 실패했습니다.");
        };

        document.head.appendChild(script);
      } catch (error) {
        console.error("카카오맵을 불러오지 못했습니다.", error);
      }
    };

    loadKakaoMap();

    return () => {
      isMounted = false;

      if (markerInstance.current) {
        markerInstance.current.setMap(null);
        markerInstance.current = null;
      }

      mapInstance.current = null;
    };
  }, [address, level]);

  return (
    <div
      ref={mapRef}
      style={{
        width,
        height,
      }}
    />
  );
};

export default CommonMap;
