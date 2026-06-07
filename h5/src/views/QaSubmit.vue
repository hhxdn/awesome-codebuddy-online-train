<template>
  <div class="qa-submit-page">
    <!-- 导航栏 -->
    <van-nav-bar title="答疑解惑" left-text="返回" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="qa-content">
      <div class="qa-intro">
        <van-icon name="chat-o" size="20" color="#1989fa" />
        <span>有问题随时问，我们将尽快回复您</span>
      </div>

      <van-cell-group inset>
        <!-- 问题描述 -->
        <van-field
          v-model="form.content"
          type="textarea"
          rows="5"
          placeholder="请详细描述您的问题（必填）"
          :rules="[{ required: true, message: '请填写问题描述' }]"
        />

        <!-- 图片上传 -->
        <div class="upload-section">
          <div class="upload-label">上传图片（可选）</div>
          <van-uploader
            v-model="fileList"
            :max-count="6"
            :before-read="beforeRead"
            :after-read="afterRead"
            accept="image/*"
            :disabled="uploading"
          />
          <div class="upload-tip">支持 jpg、png 格式，最多6张</div>
        </div>

        <!-- 手机号 -->
        <van-field
          v-model="form.phone"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号（必填）"
          :rules="[{ required: true, message: '请填写手机号' }]"
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button type="primary" round block :loading="submitting" @click="handleSubmit">
          提交问题
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { post } from '../api'
import { uploadFile } from '../api/upload'

const fileList = ref([])
const uploading = ref(false)
const submitting = ref(false)

const form = reactive({
  content: '',
  phone: '',
  images: ''
})

function beforeRead(file) {
  const isValid = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isValid) {
    showToast('仅支持 jpg、png 格式图片')
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    showToast('图片大小不能超过 5MB')
  }
  return isValid && isLt5M
}

async function afterRead(file) {
  uploading.value = true
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200) {
      file.status = 'done'
      file.url = res.data.url
    } else {
      file.status = 'failed'
      file.message = '上传失败'
    }
  } catch (e) {
    file.status = 'failed'
    file.message = '上传失败'
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  if (!form.content.trim()) {
    showToast('请填写问题描述')
    return
  }
  if (!form.phone.trim()) {
    showToast('请填写手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    showToast('请输入正确的手机号')
    return
  }

  submitting.value = true
  try {
    const urls = fileList.value
      .filter(f => f.status === 'done' && f.url)
      .map(f => f.url)
    form.images = urls.join(',')

    const res = await post('/qa/submit', {
      content: form.content.trim(),
      phone: form.phone.trim(),
      images: form.images
    })
    if (res.code === 200) {
      showSuccessToast('提交成功')
      setTimeout(() => {
        window.history.back()
      }, 1000)
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.qa-submit-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.qa-content {
  padding: 16px;
}

.qa-intro {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: #eaf4ff;
  border-radius: 8px;
  color: #1989fa;
  font-size: 14px;
}

.upload-section {
  padding: 12px 16px;
}

.upload-label {
  font-size: 14px;
  color: #323233;
  margin-bottom: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #969799;
  margin-top: 6px;
}

.submit-btn {
  padding: 24px 16px;
}
</style>
