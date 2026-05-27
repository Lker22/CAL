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
