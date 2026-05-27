<script setup>
/**
 * 空状态组件
 */
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  icon: {
    type: String,
    default: 'Document'
  }
})
</script>

<template>
  <div class="empty-state">
    <el-empty :description="description">
      <slot name="extra" />
    </el-empty>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}
</style>
