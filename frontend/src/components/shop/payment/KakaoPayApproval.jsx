import React, { useEffect } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";


const KakaoPayApproval = () => {

  const navigate = useNavigate();
  const { paymentId } = useParams();
  const [searchParams] = useSearchParams();


  useEffect(() => {
    const pgToken = searchParams.get("pg_token");
    const approval = async () => {
      try {
        const response = await fetch(
          `/api/payment/approval/${paymentId}?pg_token=${pgToken}`
        );
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText);
        }
        const result = await response.json();

        // 승인 완료 후 공통 성공 페이지 이동
        navigate("/payment/success", {
          state: result
        });
      } catch(error) {
        console.error("카카오 결제 승인 실패", error);
      }
    };
    approval();
  }, [paymentId, searchParams, navigate]);

  return (
    <div>
      결제 승인 처리중입니다...
    </div>
  );
};

export default KakaoPayApproval;