import { defineConfig } from "vite";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), babel({ presets: [reactCompilerPreset()] })],
  server: {
    host: true,
    port: 3000,

    proxy: {
      "/api": {
        target: "http://localhost:8090",
        ws: true, // 웹소켓 업그레이드 에러제거
        changeOrigin: true,
      },
    },
  },
  define: {
    // global 변수를 window로 매핑하여 SockJS 에러 해결
    global: "window",
  },
});
