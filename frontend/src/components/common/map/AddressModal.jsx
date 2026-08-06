/* ================================== AddressModal ==================================
공통 주소검색 컴포넌트

기능
1. Daum 우편번호 검색
2. 선택한 주소를 CommonMap에 표시
3. 선택 버튼 클릭 시 onSelect를 통해 부모 컴포넌트로 값 전달

사용 방법
- 부모 컴포넌트에서 onSelect를 필요한 형태로 저장
- CRUD 화면에 맞게 state, form 등을 구성하여 사용

예시)
onSelect={({ zonecode, address }) => {
    ...
}}

사용 예시는 하단을 참고
==================================================================== */

/*==================================AddressModal 사용 예시==================================
const AddressTest = () => {
  // 주소찾기 모달 열기 여부
  const [open, setOpen] = useState(false);

  // 선택된 주소 정보
  // 우편번호, 주소 -> 상세주소(ex: 3층) 필요하면 따로 추가
  const [addressInfo, setAddressInfo] = useState({
    zonecode: "", 
    address: "",
  });

  // AddressModal에서 선택한 주소 반환
  // 원하는 CRUD 폼에 맞게 자유롭게 저장하여 사용
  const handleSelect = ({ zonecode, address }) => {
    setAddressInfo({
      zonecode,
      address,
    });
  };

  return (
    <div style={{ padding: "30px" }}>
      <h2>AddressModal 테스트</h2>
      <button
        type="button"
        onClick={() => setOpen(true)}
      >
        주소 찾기
      </button>
      <div style={{ marginTop: "30px" }}>
        <p>
          <strong>우편번호 :</strong> {addressInfo.zonecode} -> 모달에서 반환된 우편번호
        </p>
        <p>
          <strong>주소 :</strong> {addressInfo.address} -> 모달에서 반환된 주소
        </p>
      </div>
      <AddressModal
        open={open}
        onClose={() => setOpen(false)}
        onSelect={handleSelect}
        mapWidth="100%"
        mapHeight="400px"
        mapLevel={3}
      />
    </div>
  );
};
====================================================================*/
import React, { useEffect, useState } from "react";
import CommonMap from "./CommonMap";
import "../../../css/common/addressModal.css";

const AddressModal = ({
  open,
  onClose,
  onSelect,
  mapWidth = "100%",
  mapHeight = "350px",
  mapLevel = 3,
}) => {
  // 검색한 우편번호
  const [zonecode, setZonecode] = useState("");

  // 검색한 기본주소
  const [address, setAddress] = useState("");

  // Daum 우편번호 SDK 로드 상태
  const [postcodeLoaded, setPostcodeLoaded] = useState(
    Boolean(window.daum?.Postcode),
  );

  useEffect(() => {
    if (!open) {
      return;
    }

    // 모달을 새로 열 때 이전 검색값 초기화
    setZonecode("");
    setAddress("");

    // SDK가 이미 로드된 경우
    if (window.daum?.Postcode) {
      setPostcodeLoaded(true);
      return;
    }

    // SDK 스크립트가 이미 추가된 경우
    const existingScript = document.getElementById("daum-postcode-sdk");

    if (existingScript) {
      const handleLoad = () => {
        setPostcodeLoaded(true);
      };

      existingScript.addEventListener("load", handleLoad);

      return () => {
        existingScript.removeEventListener("load", handleLoad);
      };
    }

    // Daum 우편번호 SDK 동적 로드
    const script = document.createElement("script");

    script.id = "daum-postcode-sdk";
    script.async = true;
    script.src =
      "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";

    script.onload = () => {
      setPostcodeLoaded(true);
    };

    script.onerror = () => {
      setPostcodeLoaded(false);
      console.error("Daum 우편번호 SDK 로드에 실패했습니다.");
    };

    document.head.appendChild(script);
  }, [open]);

  // Daum 주소 검색창 열기
  const handleSearchAddress = () => {
    if (!window.daum?.Postcode) {
      alert("주소 검색 기능을 불러오는 중입니다.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data) => {
        // 도로명주소 우선, 없으면 지번주소 사용
        const selectedAddress =
          data.roadAddress || data.jibunAddress || data.address;

        setZonecode(data.zonecode);
        setAddress(selectedAddress);
      },
    }).open();
  };

  // 검색한 값을 부모에게 반환하고 모달 닫기
  const handleSelect = () => {
    if (!address) {
      alert("주소를 먼저 검색해주세요.");
      return;
    }

    onSelect({
      zonecode,
      address,
    });

    onClose();
  };

  if (!open) {
    return null;
  }

  return (
    <div className="address-modal-overlay" onMouseDown={onClose}>
      <div className="address-modal" onMouseDown={(e) => e.stopPropagation()}>
        {/* 모달 헤더 */}
        <div className="address-modal-header">
          <h2>주소 찾기</h2>

          <button type="button" onClick={onClose} aria-label="주소 찾기 닫기">
            ✕
          </button>
        </div>

        {/* 주소 검색 */}
        <div className="address-modal-content">
          <div className="address-search-row">
            <input
              type="text"
              value={zonecode}
              placeholder="우편번호"
              readOnly
            />

            <button
              type="button"
              onClick={handleSearchAddress}
              disabled={!postcodeLoaded}
            >
              {postcodeLoaded ? "주소 찾기" : "불러오는 중"}
            </button>
          </div>

          <input
            type="text"
            value={address}
            placeholder="주소를 검색해주세요."
            readOnly
          />

          {/* 검색한 주소가 있을 때만 지도 표시 */}
          {address && (
            <CommonMap
              address={address}
              width={mapWidth}
              height={mapHeight}
              level={mapLevel}
            />
          )}
        </div>

        {/* 모달 하단 버튼 */}
        <div className="address-modal-footer">
          <button type="button" onClick={onClose}>
            취소
          </button>

          <button type="button" onClick={handleSelect} disabled={!address}>
            선택
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddressModal;
