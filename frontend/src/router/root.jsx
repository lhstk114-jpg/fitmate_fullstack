import React, { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";
import toShopRouter from "./toShopRouter.jsx";
import toCommunityRouter from "./toCommunityRouter";
import toAdminRouter from "./toAdminRouter";
import toAuthRouter from "./toAuthRouter";
import toProductsRouter from "./shop/toProductsRouter";
import toCartRouter from "./shop/toCartRouter";
import toOrderRouter from "./shop/toOrderRouter";
import toPaymentRouter from "./shop/toPaymentRouter";
import toMemberRouter from "./toMemberRouter";
import toReservationRouter from "./shop/toReservationRouter";
import toSubscriptionRouter from "./shop/toSubscriptionRouter";
import toTrainerRouter from "./toTrainerRouter";

const Loading = (
  <div className="loading">
    <h1>...Loading</h1>
  </div>
);

const MainPage = lazy(() => import("../page/MainPage"));
const ShopLayout = lazy(() => import("../layout/./ShopLayout"));
const CommunityLayout = lazy(() => import("../layout/CommunityLayout"));
const AdminLayout = lazy(() => import("../layout/AdminLayout"));
const AuthLayout = lazy(() => import("../layout/AuthLayout"));
const MemberLayout = lazy(() => import("../layout/MemberLayout"));

const root = createBrowserRouter([
  {
    path: "",
    element: (
      <Suspense fallback={Loading}>
        <MainPage />
      </Suspense>
    ),
  },
  {
    path: "shop",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toShopRouter(),
  },
  {
    path: "products",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toProductsRouter(),
  },
  {
    path: "cart",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toCartRouter(),
  },
  {
    path: "order",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toOrderRouter(),
  },
  {
    path: "payment",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toPaymentRouter(),
  },
  {
    path: "reservation",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toReservationRouter(),
  },
  {
    path: "subscription",
    element: (
      <Suspense fallback={Loading}>
        <ShopLayout />
      </Suspense>
    ),
    children: toSubscriptionRouter(),
  },
  {
    path: "trainer",
    element: (
      <Suspense fallback={Loading}>
        <MemberLayout />
      </Suspense>
    ),
    children: toTrainerRouter(),
  },
  {
    path: "community",
    element: (
      <Suspense fallback={Loading}>
        <CommunityLayout />
      </Suspense>
    ),
    children: toCommunityRouter(),
  },
  {
    path: "admin",
    element: (
      <Suspense fallback={Loading}>
        <AdminLayout />
      </Suspense>
    ),
    children: toAdminRouter(),
  },
  //로그인,회원가입
  {
    path: "auth",
    element: (
      <Suspense fallback={Loading}>
        <AuthLayout />
      </Suspense>
    ),
    children: toAuthRouter(),
  },
  //개인페이지
  {
    path: "mypage",
    element: (
      <Suspense fallback={Loading}>
        <MemberLayout />
      </Suspense>
    ),
    children: toMemberRouter(),
  },
]);

export default root;
