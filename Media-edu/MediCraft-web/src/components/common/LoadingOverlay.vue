<script setup>
import { Loading } from '@element-plus/icons-vue'

/**
 * 加载遮罩组件
 */
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: '加载中...'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="loading-overlay"
      :class="{ fullscreen }"
    >
      <div class="loading-content">
        <el-icon class="loading-icon" :size="36" color="#409EFF">
          <Loading />
        </el-icon>
        <p class="loading-text">{{ text }}</p>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border-radius: 8px;
}

.loading-overlay.fullscreen {
  position: fixed;
  border-radius: 0;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
