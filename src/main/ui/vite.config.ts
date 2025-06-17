import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts: ['all']
  }
})


// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'
//
// // https://vite.dev/config/
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     // allowedHosts: [
//     //   'admin-udi-code-redirect-3.apps.cluster-rs76q.rs76q.sandbox1797.opentlc.com'
//     // ]
//     allowedHosts: 'all'
//   }
// })
