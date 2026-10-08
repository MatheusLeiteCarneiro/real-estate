import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'
import process from 'node:process'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const bffUrl = env.BFF_URL || 'http://localhost:8081'

  return {
    plugins: [react()],
    server: {
      port: 3000,
      strictPort: true,
      proxy: {
        '/api': bffUrl,
        '/oauth2': bffUrl,
        '/login': bffUrl,
        '/logout': bffUrl,
      },
    },
  }
})
