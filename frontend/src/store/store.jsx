import { configureStore } from "@reduxjs/toolkit";
import loginSlice from "./slices/loginSlice";

//Redux Store기본 세팅
const store = configureStore({
  reducer: {
    //계정 리듀서
    loginSlice: loginSlice.reducer,
  },
});

export default store;
