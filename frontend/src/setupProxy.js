const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  // /api로 시작하는 요청은 http://localhost:8080로 프록시
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true, // Host 헤더를 타겟 URL로 바꿔 줌
      // 만약 백엔드가 HTTPS인 경우
      // secure: false,
      pathRewrite: { '^/api': '' }, // /api 부분을 제거하고 보내고 싶다면
    })
  );
}