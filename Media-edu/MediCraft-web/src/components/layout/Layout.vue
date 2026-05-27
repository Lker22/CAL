<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  Cpu,
  Guide,
  ChatDotRound,
  DataAnalysis,
  Setting,
  SwitchButton,
  Expand,
  Fold,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)

// 菜单配置
const menuItems = [
  {
    index: '/profile/display',
    title: '学习画像',
    icon: User,
    children: [
      { index: '/profile/display', title: '我的画像' },
      { index: '/profile/build', title: '画像构建' },
      { index: '/profile/query', title: '画像查询' },
      { index: '/user/info', title: '个人信息' }
    ]
  },
  {
    index: '/agent/list',
    title: 'AI资源生成',
    icon: Cpu,
    children: [
      { index: '/agent/list', title: '智能体列表' },
      { index: '/resource/generate', title: '资源生成' },
      { index: '/resource/list', title: '我的资源' }
    ]
  },
  {
    index: '/path/list',
    title: '学习路径',
    icon: Guide,
    children: [
      { index: '/path/generate', title: '生成路径' },
      { index: '/path/list', title: '我的路径' },
      { index: '/recommend/resource', title: '资源推荐' },
      { index: '/path/adjust', title: '路径调整' }
    ]
  },
  {
    index: '/tutor/question',
    title: '智能辅导',
    icon: ChatDotRound,
    children: [
      { index: '/tutor/question', title: 'AI答疑' },
      { index: '/tutor/history', title: '答疑记录' }
    ]
  },
  {
    index: '/assessment/report',
    title: '学习评估',
    icon: DataAnalysis,
    children: [
      { index: '/assessment/report', title: '评估报告' },
      { index: '/assessment/result', title: '评估结果' },
      { index: '/learning/stats', title: '数据统计' }
    ]
  }
]

const userName = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
})

const handleLogout = () => {
  userStore.logout()
}

const navigateTo = (path) => {
  router.push(path)
}

// 获取当前激活的菜单
const activeMenu = computed(() => route.path)
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '240px'" class="layout-aside">
      <div class="aside-header">
        <div class="logo" @click="navigateTo('/profile/display')">
          <el-icon :size="28" color="#409EFF"><Cpu /></el-icon>
          <span v-show="!isCollapsed" class="logo-text">AI学习助手</span>
        </div>
      </div>

      <el-scrollbar class="aside-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :router="true"
          class="aside-menu"
          background-color="#1d1e2c"
          text-color="#a0a4b8"
          active-text-color="#409EFF"
        >
          <template v-for="item in menuItems" :key="item.index">
            <el-sub-menu v-if="item.children" :index="item.index">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.index"
                :index="child.index"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="isCollapsed = !isCollapsed"
          >
            <component :is="isCollapsed ? Expand : Fold" />
          </el-icon>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" @command="handleLogout">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userName.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigateTo('/user/info')">
                  <el-icon><Setting /></el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  background-color: #1d1e2c;
  transition: width 0.3s ease;
  overflow: hidden;
}

.aside-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.aside-scrollbar {
  height: calc(100vh - 64px);
}

.aside-menu {
  border-right: none;
}

.main-container {
  background-color: #f5f7fa;
}

.layout-header {
  height: 64px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  padding: 24px;
  overflow-y: auto;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
