<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Picture, Document, Delete } from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'
import { beforeImageUpload, beforeDocumentUpload, fileToBase64 } from '@/utils/upload'

/**
 * 文件上传组件
 * 支持图片和文档上传，预览和删除
 */
const props = defineProps({
  type: {
    type: String,
    default: 'image', // image | document
    validator: (val) => ['image', 'document'].includes(val)
  },
  maxSize: {
    type: Number,
    default: 5 // MB
  },
  maxCount: {
    type: Number,
    default: 1
  },
  modelValue: {
    type: Array,
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: false
  },
  useBase64: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'error'])

const fileList = ref([...props.modelValue])
const uploading = ref(false)

// 文件上传前验证
const beforeUpload = (file) => {
  let checkResult
  if (props.type === 'image') {
    checkResult = beforeImageUpload(file, props.maxSize)
  } else {
    checkResult = beforeDocumentUpload(file, props.maxSize)
  }

  if (!checkResult.valid) {
    ElMessage.warning(checkResult.message)
    return false
  }

  // 检查文件数量
  if (!props.multiple && fileList.value.length >= props.maxCount) {
    ElMessage.warning(`最多上传${props.maxCount}个文件`)
    return false
  }

  return true
}

// 自定义上传
const customUpload = async (options) => {
  const { file } = options
  uploading.value = true

  try {
    if (props.useBase64) {
      // 使用Base64
      const base64 = await fileToBase64(file)
      const result = {
        name: file.name,
        url: base64,
        type: file.type,
        size: file.size
      }

      fileList.value.push(result)
      emitUpdate()
      emit('success', result)
    } else {
      // 上传到服务器
      const response =
        props.type === 'image'
          ? await uploadApi.uploadImage(file)
          : await uploadApi.uploadDocument(file)

      const result = {
        name: file.name,
        url: response.data.url,
        type: file.type,
        size: file.size,
        id: response.data.id
      }

      fileList.value.push(result)
      emitUpdate()
      emit('success', result)
    }
  } catch (error) {
    ElMessage.error('文件上传失败')
    emit('error', error)
  } finally {
    uploading.value = false
  }
}

// 删除文件
const removeFile = (index) => {
  fileList.value.splice(index, 1)
  emitUpdate()
}

// 触发更新
const emitUpdate = () => {
  emit('update:modelValue', [...fileList.value])
}

// 接受的文件类型
const acceptTypes = props.type === 'image' ? 'image/*' : '.pdf,.doc,.docx,.txt,.md'
</script>

<template>
  <div class="file-upload">
    <!-- 图片上传 -->
    <template v-if="type === 'image'">
      <div class="image-list">
        <div
          v-for="(item, index) in fileList"
          :key="index"
          class="image-item"
        >
          <el-image
            :src="item.url"
            fit="cover"
            class="image-preview"
            :preview-src-list="[item.url]"
          />
          <div class="image-overlay">
            <el-icon class="delete-btn" @click="removeFile(index)">
              <Delete />
            </el-icon>
          </div>
        </div>

        <el-upload
          v-if="fileList.length < maxCount || multiple"
          class="image-uploader"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="customUpload"
          :accept="acceptTypes"
          :disabled="uploading"
        >
          <div class="upload-trigger">
            <el-icon :size="24"><Picture /></el-icon>
            <span class="upload-text">
              {{ uploading ? '上传中...' : '上传图片' }}
            </span>
          </div>
        </el-upload>
      </div>
    </template>

    <!-- 文档上传 -->
    <template v-else>
      <el-upload
        drag
        class="document-uploader"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :http-request="customUpload"
        :accept="acceptTypes"
        :disabled="uploading"
        :multiple="multiple"
      >
        <div class="upload-area">
          <el-icon :size="40" color="#409EFF"><Upload /></el-icon>
          <div class="upload-info">
            <p class="upload-title">
              {{ uploading ? '上传中...' : '拖拽文件到此处，或点击上传' }}
            </p>
            <p class="upload-hint">
              支持 PDF、Word、TXT、Markdown 格式，单个文件不超过{{ maxSize }}MB
            </p>
          </div>
        </div>
      </el-upload>

      <!-- 已上传文件列表 -->
      <div v-if="fileList.length" class="document-list">
        <div
          v-for="(item, index) in fileList"
          :key="index"
          class="document-item"
        >
          <el-icon color="#409EFF"><Document /></el-icon>
          <span class="document-name">{{ item.name }}</span>
          <el-icon class="delete-btn" @click="removeFile(index)">
            <Delete />
          </el-icon>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.file-upload {
  width: 100%;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.image-preview {
  width: 100%;
  height: 100%;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.delete-btn {
  color: #fff;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.3s;
}

.delete-btn:hover {
  color: #f56c6c;
}

.upload-trigger {
  width: 120px;
  height: 120px;
  border: 2px dashed #e4e7ed;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-text {
  font-size: 12px;
}

.document-uploader {
  width: 100%;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.upload-info {
  text-align: center;
}

.upload-title {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin: 4px 0 0;
}

.document-list {
  margin-top: 12px;
}

.document-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
}

.document-name {
  flex: 1;
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-item .delete-btn {
  color: #909399;
  cursor: pointer;
  font-size: 16px;
}

.document-item .delete-btn:hover {
  color: #f56c6c;
}
</style>
