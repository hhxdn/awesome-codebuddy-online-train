<template>
  <div class="video-upload">
    <el-upload
      class="video-uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      accept="video/*"
      name="file"
      drag
    >
      <div v-if="!uploading && !modelValue" class="upload-drop">
        <el-icon class="upload-icon"><VideoCamera /></el-icon>
        <div class="upload-text">点击或拖拽视频文件到此处</div>
      </div>
      <div v-if="uploading" class="upload-progress">
        <el-icon class="is-loading"><Loading /></el-icon>
        <div>
          <div>视频上传中，请稍候...</div>
          <div class="progress-note">上传完成后将自动提交到腾讯云点播处理</div>
        </div>
      </div>
    </el-upload>

    <div v-if="modelValue" class="video-preview">
      <div class="video-info">
        <el-icon><VideoCameraFilled /></el-icon>
        <span class="video-name" :title="modelValue">{{ modelValue }}</span>
      </div>
      <el-button size="small" type="danger" link @click="clearVideo">删除</el-button>
    </div>

    <div v-if="vodInfo.taskId || vodInfo.fileId" class="vod-info">
      <el-tag v-if="vodInfo.taskId" type="warning" size="small">
        VOD任务: {{ vodInfo.taskId }}
      </el-tag>
      <el-tag v-if="vodInfo.fileId" type="success" size="small">
        FileId: {{ vodInfo.fileId }}
      </el-tag>
      <span class="vod-tip">视频正在腾讯云点播中转码处理，请稍后查看播放地址</span>
    </div>

    <div class="upload-tip" v-if="tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { VideoCamera, VideoCameraFilled, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  tip: {
    type: String,
    default: '支持 mp4/flv/mov/mkv 等常见视频格式，最大 500MB'
  },
  maxSize: {
    type: Number,
    default: 500 // MB
  }
})

const emit = defineEmits(['update:modelValue', 'update:vodInfo'])
const uploading = ref(false)
const vodInfo = reactive({
  taskId: '',
  fileId: ''
})

const uploadUrl = computed(() => '/api/admin/upload/video')
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (getToken() || '')
}))

function beforeUpload(file) {
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    ElMessage.error('只能上传视频文件')
    return false
  }
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`视频大小不能超过 ${props.maxSize}MB`)
    return false
  }
  uploading.value = true
  return true
}

function handleSuccess(response) {
  uploading.value = false
  if (response.code === 0 || response.code === 200) {
    const data = response.data || {}
    const url = data.cosUrl || data.url || ''
    emit('update:modelValue', url)

    vodInfo.taskId = data.taskId || ''
    vodInfo.fileId = data.fileId || ''
    emit('update:vodInfo', { ...vodInfo })

    ElMessage.success('视频已提交到腾讯云点播处理')
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

function clearVideo() {
  emit('update:modelValue', '')
  vodInfo.taskId = ''
  vodInfo.fileId = ''
  emit('update:vodInfo', { ...vodInfo })
}
</script>

<style scoped>
.video-upload {
  width: 100%;
}

.video-uploader {
  width: 100%;
}

.video-uploader :deep(.el-upload) {
  width: 100%;
}

.video-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 30px;
}

.upload-drop {
  text-align: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.upload-progress {
  text-align: center;
  color: #409eff;
  padding: 20px;
}

.upload-progress .el-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.progress-note {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.video-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 6px;
}

.video-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 14px;
  overflow: hidden;
}

.video-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
}

.vod-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.vod-tip {
  font-size: 12px;
  color: #909399;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
