import React, { lazy, Suspense } from "react";

const Loading = <div className="loading">...Loading</div>;

const ProductListPage = lazy(
  () => import("../../page/shop/product/ProductListPage"),
);
const ProductDetailPage = lazy(
  () => import("../../page/shop/product/ProductDetailPage"),
);
const SubscriptionPage = lazy(
  () => import("../../page/shop/subscription/SubscriptionPage")
);
const toProductsRouter = () => {
  return [
    {
      path: "",
      element: (
        <Suspense fallback={Loading}>
          <ProductListPage />
        </Suspense>
      ),
    },
    {
      path: "detail/:productId",
      element: (
        <Suspense fallback={Loading}>
          <ProductDetailPage />
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

export default toProductsRouter;
