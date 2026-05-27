<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { emailRules } from '@/utils/validator'
import FileUpload from '@/components/upload/FileUpload.vue'

const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const avatarList = ref([])

const userForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ],
  email: emailRules,
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = () => {
  const info = userStore.userInfo
  if (info) {
    userForm.nickname = info.nickname || ''
    userForm.email = info.email || ''
    userForm.phone = info.phone || ''
    userForm.avatar = info.avatar || ''
    if (info.avatar) {
      avatarList.value = [{ url: info.avatar, name: 'avatar' }]
    }
  }
}

// 处理头像上传
const handleAvatarSuccess = (result) => {
  userForm.avatar = result.url
}

// 保存用户信息
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.updateUserInfo(userForm)
    ElMessage.success('信息更新成功')
  } catch (error) {
    ElMessage.error('更新失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="user-info-page">
    <div class="page-header">
      <h2>个人信息</h2>
      <p>管理你的账号信息</p>
    </div>

    <div class="info-card">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <h4>头像</h4>
        <FileUpload
          type="image"
          :max-count="1"
          :max-size="2"
          :model-value="avatarList"
          use-base64
          @success="handleAvatarSuccess"
        />
      </div>

      <!-- 信息表单 -->
      <div class="form-section">
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="rules"
          label-width="80px"
          class="user-form"
        >
          <el-form-item label="用户名">
            <el-input
              :model-value="userStore.userInfo?.username"
              disabled
            />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSave">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-info-page {
  max-width: 700px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
}

.avatar-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-section h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.user-form {
  max-width: 450px;
}
</style>
