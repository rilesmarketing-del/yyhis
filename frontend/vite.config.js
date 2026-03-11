import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

function manualChunks(id) {
  if (!id.includes("node_modules")) {
    return;
  }

  if (id.includes("node_modules/vue") || id.includes("node_modules/@vue")) {
    return "vue-vendor";
  }

  if (id.includes("node_modules/vue-router")) {
    return "router-vendor";
  }

  if (id.includes("node_modules/@element-plus/icons-vue")) {
    return "element-plus-icons";
  }

  if (id.includes("node_modules/element-plus")) {
    if (/(date-picker|time-picker|select|table|tree)/.test(id)) {
      return "element-plus-data";
    }

    return "element-plus-core";
  }

  return "vendor";
}

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks,
      },
    },
  },
});