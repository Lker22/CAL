import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import { printDefaultCredentials } from './utils/mockAuth'

const app = createApp(App)
const pinia = createPinia()

// 全局错误处理：抑制Vue过渡动画中常见的parentNode错误
app.config.errorHandler = (err, instance, info) => {
  if (err && err.message && err.message.includes('parentNode')) {
    // 静默处理，这是Vue transition/router-view的已知问题
    return
  }
  console.error(`[Vue Error] ${info}:`, err)
}

// 全局未捕获Promise异常处理
window.addEventListener('unhandledrejection', (event) => {
  if (event.reason && event.reason.message && event.reason.message.includes('parentNode')) {
    event.preventDefault()
  }
})

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

// 初始化用户信息并打印默认账号
const userStore = useUserStore()
userStore.initUserInfo()
printDefaultCredentials()

// 如果已登录，跳转首页
if (userStore.isLoggedIn) {
  router.push('/profile/display')
}

app.mount('#app')
