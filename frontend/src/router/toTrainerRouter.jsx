import { lazy } from "react";

const TrainerReservationPage = lazy(
  () => import("../page/trainer/TrainerReservationPage")
);

const TrainerProfilePage = lazy(
  () => import("../page/trainer/TrainerProfilePage")
);

const TrainerSchedulePage = lazy(
  () => import("../page/trainer/TrainerSchedulePage")
);

const toTrainerRouter = () => {
  return [
    {
      path: "pt",
      element: <TrainerReservationPage />
    },    
    {
      path: "profile",
      element: <TrainerProfilePage />
    },
    {
      path: "schedule",
      element: <TrainerSchedulePage />
    },
  ]
}

export default toTrainerRouter