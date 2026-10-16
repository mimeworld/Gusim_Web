import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      "/api": process.env.API_PROXY_TARGET || "http://gusim-backend-1:8080",
      "/uploads": process.env.API_PROXY_TARGET || "http://gusim-backend-1:8080",
    },
  },
});
