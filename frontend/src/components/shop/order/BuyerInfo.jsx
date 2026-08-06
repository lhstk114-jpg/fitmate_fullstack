import React, { useState } from "react";
import AddressModal from "../../../components/common/map/AddressModal";

const BuyerInfo = ({ member, orderInfo, setOrderInfo }) => {
  const [addressOpen, setAddressOpen] = useState(false);
  const [customMemo, setCustomMemo] = useState("");
  const [isCustomMemo, setIsCustomMemo] = useState(false);
  const handleChange = (e) => {
    setOrderInfo({
      ...orderInfo,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="buyerInfo">
      <h2>배송 정보</h2>

      <div className="inputBox">
        <label>받는 분</label>
        <input
          name="receiverName"
          value={orderInfo.receiverName || ""}
          onChange={handleChange}
        />
      </div>

      <div className="inputBox">
        <label>연락처</label>
        <input
          name="receiverPhone"
          value={orderInfo.receiverPhone || ""}
          onChange={handleChange}
        />
      </div>

      <div className="inputBox">
        <label>주소</label>
        <button type="button" onClick={() => setAddressOpen(true)}>
          주소 찾기
        </button>
        <input
          name="receiverAddress"
          value={orderInfo.receiverAddress || ""}
          readOnly
        />
      </div>
      <div className="inputBox">
        <label>상세주소</label>
        <input
          type="text"
          placeholder="동/호수, 건물명 입력"
          value={orderInfo.receiverDetailAddress}
          onChange={(e) =>
            setOrderInfo({
              ...orderInfo,
              receiverDetailAddress: e.target.value,
            })
          }
        />
      </div>

      <div className="inputBox">
        <label>배송 요청사항</label>

        <select
          name="deliveryMemo"
          value={isCustomMemo ? "직접 입력" : orderInfo.deliveryMemo || ""}
          onChange={(e) => {
            const value = e.target.value;

            if (value === "직접 입력") {
              setIsCustomMemo(true);
              setOrderInfo({
                ...orderInfo,
                deliveryMemo: "",
              });
            } else {
              setIsCustomMemo(false);
              setOrderInfo({
                ...orderInfo,
                deliveryMemo: value,
              });
            }
          }}
        >
          <option value="">배송 요청사항 선택</option>
          <option value="문 앞에 놓아주세요">문 앞에 놓아주세요</option>
          <option value="경비실에 맡겨주세요">경비실에 맡겨주세요</option>
          <option value="택배함에 넣어주세요">택배함에 넣어주세요</option>
          <option value="부재 시 연락주세요">부재 시 연락주세요</option>
          <option value="파손 주의해주세요">파손 주의해주세요</option>
          <option value="직접 입력">직접 입력</option>
        </select>
        {isCustomMemo && (
          <input
            placeholder="배송 요청사항 직접 입력"
            value={customMemo}
            onChange={(e) => {
              setCustomMemo(e.target.value);
              setOrderInfo({
                ...orderInfo,
                deliveryMemo: e.target.value,
              });
            }}/>
        )}
      </div>
      <AddressModal
        open={addressOpen}
        onClose={() => setAddressOpen(false)}
        onSelect={({ zonecode, address }) => {
          setOrderInfo({
            ...orderInfo,
            receiverAddress: `${address} (${zonecode})`,
          });
        }}
      />
    </div>
  );
};

export default BuyerInfo;
