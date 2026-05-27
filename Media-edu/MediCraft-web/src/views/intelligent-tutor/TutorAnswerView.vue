<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'

const router = useRouter()

// 示例解答数据
const answer = ref({
  question: '如何理解神经网络的反向传播算法？',
  answer: `### 反向传播算法详解

反向传播（Backpropagation）是训练神经网络的核心算法。

#### 1. 核心思想

反向传播的核心思想是通过计算损失函数对每个参数的梯度，从输出层向输入层逐层传播误差。

#### 2. 数学原理

\`\`\`
δ^l = ((W^{l+1})^T δ^{l+1}) ⊙ σ'(z^l)
\`\`\`

其中：
- **δ^l** 是第l层的误差项
- **W^{l+1}** 是第l+1层的权重矩阵
- **σ'** 是激活函数的导数

#### 3. 计算步骤

1. **前向传播**：计算每一层的输出
2. **计算损失**：使用损失函数衡量预测误差
3. **反向传播**：从输出层向输入层计算梯度
4. **参数更新**：使用梯度下降更新权重

#### 4. 代码实现

\`\`\`python
import numpy as np

def backprop(X, y, params):
    m = X.shape[0]
    grads = {}

    # 前向传播
    A1 = np.tanh(np.dot(X, params['W1']) + params['b1'])
    A2 = sigmoid(np.dot(A1, params['W2']) + params['b2'])

    # 反向传播
    dZ2 = A2 - y
    grads['dW2'] = (1/m) * np.dot(A1.T, dZ2)
    grads['db2'] = (1/m) * np.sum(dZ2, axis=0)

    return grads
\`\`\`

> 💡 **理解提示**：可以把反向传播想象成"责任分配"机制，每个神经元根据自己对最终误差的"贡献"来调整自己的权重。`,
  format: 'text',
  hasImage: true,
  imageUrl: 'https://via.placeholder.com/600x400?text=Neural+Network+Diagram',
  hasVideo: false
})

const goBack = () => {
  router.push('/tutor/question')
}
</script>

<template>
  <div class="tutor-answer-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回提问
      </el-button>
    </div>

    <div class="answer-container">
      <!-- 问题展示 -->
      <div class="question-card">
        <h4>问题</h4>
        <p>{{ answer.question }}</p>
      </div>

      <!-- AI解答 -->
      <div class="answer-card">
        <div class="answer-header">
          <div class="ai-avatar">AI</div>
          <h4>AI解答</h4>
        </div>

        <div class="answer-content">
          <MarkdownRenderer :content="answer.answer" />
        </div>

        <!-- 图片讲解 -->
        <div v-if="answer.hasImage" class="image-explanation">
          <h5>图解说明</h5>
          <el-image
            :src="answer.imageUrl"
            fit="contain"
            :preview-src-list="[answer.imageUrl]"
            class="explanation-image"
          />
        </div>

        <!-- 视频讲解 -->
        <div v-if="answer.hasVideo" class="video-explanation">
          <h5>视频讲解</h5>
          <div class="video-placeholder">
            <el-icon :size="48" color="#409EFF"><VideoPlay /></el-icon>
            <p>点击播放视频讲解</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, VideoPlay }
}
</script>

<style scoped>
.tutor-answer-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;
}

.answer-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: #f0f7ff;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #409eff;
}

.question-card h4 {
  font-size: 14px;
  color: #409eff;
  margin: 0 0 8px;
}

.question-card p {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
  font-weight: 500;
}

.answer-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.answer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d1e2c, #2d2e42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.answer-header h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0;
}

.answer-content {
  margin-bottom: 24px;
}

.image-explanation {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 10px;
}

.image-explanation h5 {
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.explanation-image {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
}

.video-explanation {
  margin-top: 24px;
}

.video-explanation h5 {
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #1d1e2c;
  border-radius: 10px;
  color: #fff;
  cursor: pointer;
  transition: opacity 0.3s;
}

.video-placeholder:hover {
  opacity: 0.9;
}

.video-placeholder p {
  margin: 12px 0 0;
  font-size: 14px;
}
</style>
