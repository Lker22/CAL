<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import { RESOURCE_TYPES } from '@/utils/constants'
import { formatDate, formatFileSize } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const resourceStore = useResourceStore()

const loading = ref(false)
const activeType = ref('all')
const searchKeyword = ref('')

// 从 store 获取资源列表
const resources = computed(() => {
  let list = Array.isArray(resourceStore.resources) ? resourceStore.resources : []

  // 类型筛选
  if (activeType.value !== 'all') {
    list = list.filter((r) => r.resourceType === activeType.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter((r) => (r.resourceTitle || '').toLowerCase().includes(keyword))
  }

  return list
})

// 类型选项
const typeOptions = [
  { key: 'all', label: '全部' },
  ...Object.values(RESOURCE_TYPES)
]

// 获取资源类型配置
const getTypeConfig = (type) => {
  return RESOURCE_TYPES[type] || RESOURCE_TYPES.document
}

// 加载资源列表
const loadResources = async () => {
  loading.value = true
  try {
    await resourceStore.getResources()
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (resource) => {
  router.push(`/resource/detail/${resource.id}`)
}

// 删除资源
const handleDelete = async (resource) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${resource.title}"吗？`, '删除确认', {
      type: 'warning'
    })
    await resourceStore.deleteResource(resource.id)
    ElMessage.success('删除成功')
  } catch {
    // 取消删除
  }
}

onMounted(() => {
  loadResources()
})
</script>

<template>
  <div class="resource-list-page">
    <div class="page-header">
      <h2>我的学习资源</h2>
      <p>管理和查看AI为你生成的个性化学习资源</p>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <div class="type-tabs">
        <el-tag
          v-for="type in typeOptions"
          :key="type.key"
          :effect="activeType === type.key ? 'dark' : 'plain'"
          :type="activeType === type.key ? '' : 'info'"
          class="type-tab"
          @click="activeType = type.key"
        >
          {{ type.label }}
        </el-tag>
      </div>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索资源..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 资源列表 -->
    <div v-loading="loading" class="resource-grid">
      <div
        v-for="resource in resources"
        :key="resource.id"
        class="resource-card"
        @click="viewDetail(resource)"
      >
        <div class="resource-icon" :style="{ backgroundColor: getTypeConfig(resource.resourceType).color + '15' }">
          <el-icon :size="28" :color="getTypeConfig(resource.resourceType).color">
            <component :is="getTypeConfig(resource.resourceType).icon" />
          </el-icon>
        </div>
        <div class="resource-info">
          <h4 class="resource-title">{{ resource.resourceTitle }}</h4>
          <p class="resource-meta">
            <span>{{ resource.difficulty || '标准' }}</span>
            <span>{{ formatDate(resource.createTime, 'MM-DD') }}</span>
          </p>
          <p class="resource-size">{{ resource.courseName || '' }}</p>
        </div>
        <div class="resource-actions" @click.stop>
          <el-button type="danger" text size="small" @click="handleDelete(resource)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <EmptyState v-if="!loading && resources.length === 0" description="暂无资源" />
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
export default {
  components: { Search }
}
</script>

<style scoped>
.resource-list-page {
  padding: 0;
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

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.type-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.type-tab {
  cursor: pointer;
  transition: all 0.3s;
}

.search-input {
  width: 250px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.resource-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #409eff;
}

.resource-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
  min-width: 0;
}

.resource-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1e2c;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-meta {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px;
  display: flex;
  gap: 12px;
}

.resource-size {
  font-size: 12px;
  color: #c0c4cc;
  margin: 0;
}

.resource-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.resource-card:hover .resource-actions {
  opacity: 1;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}
</style>
