<template>
  <div class="image-upload">
    <el-upload
      class="image-uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      accept="image/*"
      name="file"
    >
      <img v-if="modelValue" :src="modelValue" class="image-preview" />
      <div v-else class="upload-placeholder">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <span>点击上传图片</span>
      </div>
      <div v-if="uploading" class="upload-progress">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>上传中...</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="image-actions">
      <el-button size="small" type="danger" link @click="clearImage">删除</el-button>
    </div>
    <div class="upload-tip" v-if="tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Plus, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  tip: {
    type: String,
    default: '支持 jpg/png/gif/webp，建议尺寸 750×400'
  },
  maxSize: {
    type: Number,
    default: 10 // MB
  }
})

const emit = defineEmits(['update:modelValue'])
const uploading = ref(false)

const uploadUrl = computed(() => '/api/admin/upload/image')
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (getToken() || '')
}))

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  uploading.value = true
  return true
}

function handleSuccess(response) {
  uploading.value = false
  if (response.code === 0 || response.code === 200) {
    const url = response.data?.url || response.data
    emit('update:modelValue', url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleError() {
  uploading.value = false
  ElMessage.error('上传失败，请检查网络或配置')
}

function handleProgress() {
  // uploading is already true from beforeUpload
}

function clearImage() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #8c939d;
  font-size: 12px;
}

.upload-icon {
  font-size: 28px;
}

.upload-progress {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #409eff;
  font-size: 13px;
  z-index: 1;
}

.image-actions {
  display: flex;
  align-items: center;
  padding-top: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  width: 100%;
  margin-top: 2px;
}
</style>
