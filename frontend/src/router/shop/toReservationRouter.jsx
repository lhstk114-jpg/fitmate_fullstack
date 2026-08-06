import { lazy } from "react";

const ReservationPage = lazy(
  () => import("../../page/shop/reservation/ReservationPage"),
);

const toReservationRouter = () => {
  return [
    {
      path: "",
      element: <ReservationPage />,
    },
  ];
};

export default toReservationRouter;
