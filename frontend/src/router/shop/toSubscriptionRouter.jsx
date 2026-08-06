import React, { lazy, Suspense } from "react";

const Loading = <div className="loading">...Loading</div>;

const SubscriptionPage = lazy(
  () => import("../../page/shop/subscription/SubscriptionPage")
);
const SubscriptionDetailPage = lazy(
  () => import("../../page/shop/subscription/SubscriptionDetailPage")
);

const toSubscriptionRouter = () => {
  return [
    {
      path: "",
      element: (
        <Suspense fallback={Loading}>
          <SubscriptionPage />
        </Suspense>
      ),
    },
    {
      path: ":id",
      element: (
        <Suspense fallback={Loading}>
          <SubscriptionDetailPage />
        </Suspense>
      ),
    },
    {
      path: "premium",
      element: (
        <Suspense fallback={Loading}>
          <SubscriptionPage />
        </Suspense>
      ),
    },
  ];
};

export default toSubscriptionRouter;