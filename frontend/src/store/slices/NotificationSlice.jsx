import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  notificationList: [],
  unreadCount: 0,
};

const notificationSlice = createSlice({
  name: "notificationSlice",
  initialState,
  reducers: {
    // 알림 목록 저장
    setNotificationList: (state, action) => {
      state.notificationList = action.payload;

      state.unreadCount = action.payload.filter(
        (notification) => notification.isRead === 0
      ).length;
    },

    // 새 알림 추가
    addNotification: (state, action) => {
      state.notificationList.unshift(action.payload);

      if (action.payload.isRead === 0) {
        state.unreadCount++;
      }
    },

    // 읽음 처리
    readNotification: (state, action) => {
      const notification = state.notificationList.find(
        (notification) => notification.id === action.payload
      );

      if (notification && notification.isRead === 0) {
        notification.isRead = 1;
        state.unreadCount--;
      }
    },

    // 모두 읽음
    readAllNotification: (state) => {
      state.notificationList.forEach((notification) => {
        notification.isRead = 1;
      });

      state.unreadCount = 0;
    },

    // 초기화
    clearNotification: (state) => {
      state.notificationList = [];
      state.unreadCount = 0;
    },
  },
});

export const {
  setNotificationList,
  addNotification,
  readNotification,
  readAllNotification,
  clearNotification,
} = notificationSlice.actions;

export default notificationSlice.reducer;