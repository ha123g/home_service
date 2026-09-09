import { defineConfig } from "umi";

export default defineConfig({
  routes: [
    { path: "/login", component: "auth/login" },
    { path: "/register", component: "auth/register" },
    { path: "/502", component: "502" },
    { path: "/404", component: "404" },
    { path: "/messages", component: "messages" },
    { path: "/profile", component: "profile" },
    { path: "/", component: "home/index" },
    { path: "/shops", component: "shops/index" },
    { path: "/shops/:id", component: "shops/detail" },
    { path: "/appointments", component: "appointments/index" },
    { path: "/orders", component: "orders/index" },
    { path: "/merchant/apply", component: "merchant/apply" },
    {
      path: "/admin",
      component: "admin/index",
      routes: [
        { path: "/admin", redirect: "/admin/dashboard" },
        { path: "/admin/dashboard", component: "admin/dashboard" },
        { path: "/admin/users", component: "admin/users" },
        { path: "/admin/rag", component: "admin/rag" },
        { path: "/admin/applications", component: "admin/applications" },
        { path: "/admin/shops", component: "admin/shops" },
        { path: "/admin/ops/alerts", component: "admin/ops/alerts" },
        { path: "/admin/ops/logs", component: "admin/ops/logs" },
      ],
    },
    { path: "*", component: "404" },
  ],
  npmClient: 'pnpm',
  // 当前项目优先保证开发热更新稳定；MFSU 在 Windows/混合缓存场景下会生成不兼容的 Webpack runtime。
  mfsu: false,
  // 避免多入口异步 chunk 在 Windows 开发/构建环境中复用同名 esbuild helper。
  esbuildMinifyIIFE: true,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
});
