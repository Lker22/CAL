<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

/**
 * Markdown渲染组件
 * 支持Markdown格式化渲染，安全XSS过滤
 */
const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  className: {
    type: String,
    default: ''
  }
})

// 配置marked
marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false
})

// 渲染并过滤Markdown内容
const renderedContent = computed(() => {
  if (!props.content) return ''
  const html = marked.parse(props.content)
  return DOMPurify.sanitize(html, {
    ADD_TAGS: ['iframe'],
    ADD_ATTR: ['allow', 'allowfullscreen', 'frameborder', 'scrolling']
  })
})
</script>

<template>
  <div
    class="markdown-body"
    :class="className"
    v-html="renderedContent"
  />
</template>

<style>
/* Markdown 全局样式 */
.markdown-body {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  word-wrap: break-word;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 16px;
  margin-bottom: 12px;
  font-weight: 600;
  line-height: 1.4;
  color: #1d1e2c;
}

.markdown-body h1 {
  font-size: 24px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.markdown-body h2 {
  font-size: 20px;
}

.markdown-body h3 {
  font-size: 17px;
}

.markdown-body h4 {
  font-size: 15px;
}

.markdown-body p {
  margin: 8px 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 24px;
  margin: 8px 0;
}

.markdown-body li {
  margin: 4px 0;
}

.markdown-body blockquote {
  margin: 12px 0;
  padding: 8px 16px;
  border-left: 4px solid #409eff;
  background-color: #f0f7ff;
  color: #606266;
}

.markdown-body code {
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #f5f7fa;
  color: #e83e8c;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.markdown-body pre {
  margin: 12px 0;
  padding: 16px;
  border-radius: 8px;
  background-color: #1d1e2c;
  overflow-x: auto;
}

.markdown-body pre code {
  padding: 0;
  background-color: transparent;
  color: #e0e0e0;
  font-size: 13px;
  line-height: 1.6;
}

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.markdown-body th,
.markdown-body td {
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  text-align: left;
}

.markdown-body th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.markdown-body a {
  color: #409eff;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.markdown-body hr {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 16px 0;
}

.markdown-body strong {
  font-weight: 600;
  color: #1d1e2c;
}
</style>
