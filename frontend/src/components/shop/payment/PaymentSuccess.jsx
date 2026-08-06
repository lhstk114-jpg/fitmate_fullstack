import { useLocation, useNavigate } from 'react-router-dom';

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const paymentInfo = location.state;

  return (
    <div className="payment-success">
      <div className="payment-success-con">
        <div className="title">결제 완료 및 승인 성공</div>
        <div className="payment-result">
          <ul>
            <li>고객님이 주문하신 상품의 결제가 정상 완료되었습니다.</li>
            <li>주문 상품명: {paymentInfo?.productName}</li>
            <li>결제 금액: {paymentInfo?.amount?.toLocaleString()}원</li>
            <li>이용해 주셔서 감사합니다.</li>
            <li>
              <button onClick={() => navigate('/')}>HOME으로 이동</button>
            </li>
            <li>
              <button
                onClick={() => {
                  const type = paymentInfo?.productType;

                  if (type === "GOODS") {
                    navigate('/order/list');
                  } else if (type === "PREMIUM") {
                    navigate('/mypage/subscription');
                  } else if (type === "PT") {
                    // PT 상품은 바로 예약 페이지로 이동!
                    navigate('/reservation');
                  } else {
                    navigate('/order/list');
                  }
                }}
              >{paymentInfo?.productType === "PT" ? "PT 예약하러 가기" : "구매 내역 확인"}</button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;