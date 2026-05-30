<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTutorStore } from '@/stores/tutor'
import { formatRelativeTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const tutorStore = useTutorStore()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 查看详情
const viewDetail = (record) => {
  router.push(`/tutor/answer/${record.recordId}`)
}

// 删除记录
const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条答疑记录吗？', '删除确认', {
      type: 'warning'
    })
    await tutorStore.deleteRecord(record.recordId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 继续提问
const askMore = () => {
  router.push('/tutor/question')
}

// 分页
const handlePageChange = (page) => {
  currentPage.value = page
  loadHistory()
}

// 加载历史
const loadHistory = async () => {
  loading.value = true
  try {
    await tutorStore.getHistory({ page: currentPage.value, pageSize: pageSize.value })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHistory()
})
</script>

<template>
  <div class="tutor-history-page">
    <div class="page-header">
      <h2>答疑历史记录</h2>
      <p>查看和管理你的历史提问与AI解答</p>
      <el-button type="primary" @click="askMore">继续提问</el-button>
    </div>

    <div v-loading="loading" class="history-list">
      <div
        v-for="record in tutorStore.historyRecords"
        :key="record.recordId"
        class="history-card"
        @click="viewDetail(record)"
      >
        <div class="history-header">
          <div class="question-area">
            <el-tag v-if="record.imageUrl" size="small" type="warning">含图解</el-tag>
            <h4>{{ record.question }}</h4>
          </div>
          <span class="time">{{ formatRelativeTime(record.createTime) }}</span>
        </div>

        <p class="answer-preview">{{ record.textAnswer }}</p>

        <div class="history-actions" @click.stop>
          <el-button type="primary" text size="small" @click="viewDetail(record)">
            查看详情
          </el-button>
          <el-button type="danger" text size="small" @click="handleDelete(record)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="tutorStore.historyTotal > pageSize" class="pagination-area">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="tutorStore.historyTotal"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <EmptyState v-if="!loading && tutorStore.historyRecords.length === 0" description="暂无答疑记录">
      <el-button type="primary" @click="askMore">开始提问</el-button>
    </EmptyState>
  </div>
</template>

<style scoped>
.tutor-history-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0;
}

.page-header p {
  flex: 1;
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.history-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #409eff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.question-area {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.question-area h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.answer-preview {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
