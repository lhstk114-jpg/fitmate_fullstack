import React, { lazy, Suspense } from "react";
import { Navigate } from "react-router-dom";

const Loading = <div className="loading">...Loading</div>;

const CartPage = lazy(() => import("../../page/shop/cart/CartPage"));

const toCartRouter = () => {
  return [
    {
      path: "",
      element: (
        <Suspense fallback={Loading}>
          <CartPage />
        </Suspense>
      ),
    },
  ];
};

export default toCartRouter;
