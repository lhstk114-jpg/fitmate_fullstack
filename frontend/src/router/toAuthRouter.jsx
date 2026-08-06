import React, { lazy, Suspense } from "react";
import { Navigate } from "react-router-dom";

const Loading = <div className="loading">...Loading</div>;

const LoginPage = lazy(() => import("../page/auth/LoginPage"));
const JoinPage = lazy(() => import("../page/auth/JoinPage"));

const toAuthRouter = () => {
  return [
    {
      path: "",
      element: <Navigate replace to={"/auth/login"} />,
    },
    {
      path: "login",
      element: (
        <Suspense fallback={Loading}>
          <LoginPage />
        </Suspense>
      ),
    },
    {
      path: "join",
      element: (
        <Suspense fallback={Loading}>
          <JoinPage />
        </Suspense>
      ),
    },
  ];
};

export default toAuthRouter;
