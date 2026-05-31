import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import AutoImport from 'unplugin-auto-import/vite'
import pxtorem from 'postcss-pxtorem'

export default defineConfig({
  base: '/h5/',
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  css: {
    postcss: {
      plugins: [
        pxtorem({
          rootValue: 37.5,
          unitPrecision: 5,
          propList: ['*'],
          selectorBlackList: [],
          replace: true,
          mediaQuery: false,
          minPixelValue: 1,
          exclude: /node_modules/i
        })
      ]
    }
  },
  server: {
    host: '127.0.0.1',
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8089',
        changeOrigin: true
      }
    }
  }
})
