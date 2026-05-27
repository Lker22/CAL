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
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">新时代大学生</h1>
          <h2 class="brand-subtitle">AI学习辅助系统</h2>
          <p class="brand-desc">
            智能画像 · 个性化资源 · 学习路径规划 · AI辅导
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">🧠</span>
              <span>AI智能学习画像</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📚</span>
              <span>多智能体资源生成</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🗺️</span>
              <span>个性化学习路径</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💡</span>
              <span>智能答疑辅导</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-container">
          <h3 class="form-title">欢迎回来</h3>
          <p class="form-subtitle">请登录您的账号</p>

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
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="default-account">
            <p>演示账号（无需后端）</p>
            <div class="account-info">
              <span>用户名：<strong>admin</strong></span>
              <span>密码：<strong>admin123</strong></span>
            </div>
          </div>

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 900px;
  min-height: 580px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #1d1e2c 0%, #2d2e42 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.brand-content {
  color: #fff;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.brand-subtitle {
  font-size: 20px;
  font-weight: 400;
  color: #409eff;
  margin: 0 0 16px;
}

.brand-desc {
  font-size: 14px;
  color: #a0a4b8;
  margin: 0 0 32px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #d0d3e0;
}

.feature-icon {
  font-size: 20px;
}

.form-section {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #909399;
}

.default-account {
  margin-top: 16px;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 8px;
  text-align: center;
}

.default-account p {
  font-size: 12px;
  color: #909399;
  margin: 0 0 6px;
}

.account-info {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}

.account-info strong {
  color: #409eff;
}
</style>
