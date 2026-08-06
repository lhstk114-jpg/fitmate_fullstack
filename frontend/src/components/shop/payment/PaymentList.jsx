import React, { useEffect, useState } from 'react'
import jwtAxios from "../../../apis/util/jwtUtil";
// 관리자 조회페이지로 변경하기
const PaymentList = () => {

  const [data, setData] = useState([]);

  useEffect(() => {

    const fetchPaymentList = async () => {
      try {
        const response = await jwtAxios.get("/api/payment/list"
        );

        console.log("결제목록 응답:", response.data);

        setData(response.data);

      } catch (error) {
        console.error(error);
      }
    };
    fetchPaymentList();
  }, []);


  return (
    <>
      <div className="kakaoPayList">
        <div className="kakaoPayList-con">
          <h3>KakoPay 결제 목록</h3>
          <ul>
            <li>
              <span>결제아이디</span>
              <span>상품명</span>
              <span>결제금액</span>
              <span>결제상태</span>
            </li>
            {Array.isArray(data) && data.map((payment)=>{
              return (
                <li key={payment.id}>
                  <span>{payment.id}</span>
                  <span>{payment.productName}</span>
                  <span>{payment.amount?.toLocaleString()}원</span>
                  <span>{payment.paymentStatus}</span>
                </li>
              )
            })}
            
          </ul>
        </div>
      </div>
    </>
  )
}

export default PaymentList;