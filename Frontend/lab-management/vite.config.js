import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 浏览器页面导航请求（GET + text/html）返回 index.html，其余代理到后端
const spaBypass = (req) => {
  if (req.method === 'GET' && req.headers?.accept?.includes('text/html')) {
    return '/index.html'
  }
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',  // 允许局域网访问
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/login': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/logout': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/captcha': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/sysUser': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/role': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        bypass: spaBypass
      },
      '/notification': {
        target: 'http://localhost:8082',
          changeOrigin: true,
          secure: false,
          bypass: spaBypass
        },
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        ws: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
