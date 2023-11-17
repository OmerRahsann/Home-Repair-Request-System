const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();

// Enable CORS for all routes
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
  res.header('Access-Control-Allow-Headers', 'Content-Type');
  next();
});

// Proxy requests to the actual backend only for /api/login
app.use('/api/login', createProxyMiddleware({ target: 'http://52.90.18.125:8080', changeOrigin: true }));
app.use('/api/account/type', createProxyMiddleware({ target: 'http://52.90.18.125:8080', changeOrigin: true }));
app.use('/api/register', createProxyMiddleware({ target: 'http://52.90.18.125:8080', changeOrigin: true }));
// Your other routes and middleware...

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
