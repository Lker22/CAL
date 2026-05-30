<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { usernameRules, passwordRules } from '@/utils/validator'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: usernameRules,
  password: passwordRules
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/profile/display')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <!-- 装饰性浮动元素 -->
    <div class="floating-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
      <div class="shape shape-5"></div>
    </div>

    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">MediCraft</h1>
          <h2 class="brand-subtitle">AI 智能学习平台</h2>
          <p class="brand-desc">
            基于多智能体协同的个性化学习系统
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>AI 智能学习画像构建</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>6大智能体协同生成资源</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>个性化学习路径规划</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>智能答疑与效果评估</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-container">
          <h3 class="form-title">欢迎回来 👋</h3>
          <p class="form-subtitle">登录你的账号，继续学习之旅</p>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 浮动装饰元素 */
.floating-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 300px; height: 300px;
  background: #409eff;
  top: -100px; left: -50px;
  animation-delay: 0s;
}
.shape-2 {
  width: 200px; height: 200px;
  background: #67c23a;
  bottom: -60px; right: 10%;
  animation-delay: -5s;
}
.shape-3 {
  width: 150px; height: 150px;
  background: #e6a23c;
  top: 30%; right: -40px;
  animation-delay: -10s;
}
.shape-4 {
  width: 100px; height: 100px;
  background: #f56c6c;
  bottom: 20%; left: 5%;
  animation-delay: -15s;
}
.shape-5 {
  width: 180px; height: 180px;
  background: #b37feb;
  top: 10%; left: 40%;
  animation-delay: -7s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(15px, 10px) scale(1.02); }
}

.login-container {
  display: flex;
  width: 960px;
  min-height: 600px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  background: linear-gradient(160deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  width: 400px; height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.15), transparent);
  top: -100px; right: -100px;
}

.brand-content {
  color: #fff;
  position: relative;
  z-index: 1;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 48px;
  display: inline-block;
  animation: pulse 3s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 8px;
  background: linear-gradient(90deg, #fff, #409eff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 18px;
  font-weight: 400;
  color: #8b8fa8;
  margin: 0 0 12px;
}

.brand-desc {
  font-size: 13px;
  color: #6b6f8a;
  margin: 0 0 36px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #b0b4cc;
  transition: color 0.3s;
}

.feature-item:hover {
  color: #fff;
}

.feature-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.5);
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.form-container {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #1d1e2c;
  margin: 0 0 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
}

.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #409eff, #667eea);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
}

.login-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;
}
</style>
