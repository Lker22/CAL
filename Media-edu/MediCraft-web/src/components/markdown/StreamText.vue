<script setup>
import { ref, watch, computed } from 'vue'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'

/**
 * 流式文本渲染组件
 * 支持逐字渲染效果，配合Markdown渲染
 */
const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  isStreaming: {
    type: Boolean,
    default: false
  },
  useMarkdown: {
    type: Boolean,
    default: true
  }
})

const displayText = ref('')
let animationTimer = null

// 监听文本变化，实现打字效果
watch(
  () => props.text,
  (newText) => {
    if (props.isStreaming && newText.length > displayText.value.length) {
      // 流式模式：逐字追加
      animateText(newText)
    } else {
      // 非流式：直接显示
      displayText.value = newText
    }
  },
  { immediate: true }
)

function animateText(targetText) {
  if (animationTimer) clearTimeout(animationTimer)

  const remaining = targetText.slice(displayText.value.length)
  let index = 0

  function type() {
    if (index < remaining.length) {
      // 每次追加多个字符以提高速度
      const chunkSize = Math.min(3, remaining.length - index)
      displayText.value += remaining.slice(index, index + chunkSize)
      index += chunkSize
      animationTimer = setTimeout(type, 16)
    }
  }

  type()
}

// 显示光标
const showCursor = computed(() => props.isStreaming)
</script>

<template>
  <div class="stream-text">
    <MarkdownRenderer v-if="useMarkdown" :content="displayText" />
    <div v-else class="plain-text">{{ displayText }}</div>
    <span v-if="showCursor" class="cursor-blink">|</span>
  </div>
</template>

<style scoped>
.stream-text {
  position: relative;
}

.plain-text {
  white-space: pre-wrap;
  line-height: 1.8;
}

.cursor-blink {
  display: inline-block;
  animation: blink 1s infinite;
  color: #409eff;
  font-weight: bold;
  margin-left: 2px;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}
</style>
