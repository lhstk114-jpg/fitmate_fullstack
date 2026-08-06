import React, { lazy, Suspense } from "react";

const Loading = <div className="loading">...Loading</div>;

const PaymentSuccessPage = lazy(() => import("../../page/shop/payment/PaymentSuccessPage"));
const PaymentFailPage = lazy(() => import("../../page/shop/payment/PaymentFailPage"));
const PaymentListPage = lazy(() => import("../../page/shop/payment/PaymentListPage"));
const KakaoPayApproval = lazy(() => import("../../components/shop/payment/KakaoPayApproval"));


const toPaymentRouter = () => {
  return [
    {
      path: "fail",
      element: (
        <Suspense fallback={Loading}>
          <PaymentFailPage />
        </Suspense>
      ),
    },
    {
      // 카카오 결제 승인 처리
      path: "approval/:paymentId",
      element: (
        <Suspense fallback={Loading}>
          <KakaoPayApproval />
        </Suspense>
      ),
    },
    // 모든 결제 완료 화면
    {
      path: "success",
      element: (
        <Suspense fallback={Loading}>
          <PaymentSuccessPage />
        </Suspense>
      ),
    },
    {
      path: "list",
      element: (
        <Suspense fallback={Loading}>
          <PaymentListPage />
        </Suspense>
      ),
    },
  ];
};

export default toPaymentRouter;
