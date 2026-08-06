import React from 'react';

const PaymentMethod = ({ payment, setPayment }) => {
  return (
    <div className="paymentMethod">

      <h2>결제 방법</h2>

      <div>
        <label>
          <input 
            type="radio"
            name="payment"
            value="kakao"
            checked={payment === "kakao"}
            onChange={(e)=>setPayment(e.target.value)}
          />
          카카오페이
        </label>
      </div>


      <div>
        <label>
          <input 
            type="radio"
            name="payment"
            value="card"
            checked={payment === "card"}
            onChange={(e)=>setPayment(e.target.value)}
          />
          일반 결제
        </label>
      </div>

    </div>
  );
};

export default PaymentMethod;