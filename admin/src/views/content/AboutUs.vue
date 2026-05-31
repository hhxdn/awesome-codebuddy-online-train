<template>
  <div class="about-us-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>关于我们</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </template>
      <div v-loading="loading">
        <RichTextEditor v-model="content" height="500" />
        <div style="color: #909399; font-size: 12px; margin-top: 8px;">
          提示：编辑完成后点击右上角「保存」按钮。用户端个人中心将展示此内容。
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, put } from '@/api'
import RichTextEditor from '@/components/RichTextEditor.vue'

const loading = ref(false)
const saving = ref(false)
const content = ref('')

onMounted(() => {
  load()
})

function load() {
  loading.value = true
  get('/admin/config/about_us').then(res => {
    content.value = res.data?.configValue || ''
  }).finally(() => {
    loading.value = false
  })
}

function handleSave() {
  saving.value = true
  put('/admin/config/about_us', { configValue: content.value }).then(() => {
    ElMessage.success('保存成功')
  }).finally(() => {
    saving.value = false
  })
}
</script>

<style scoped>
.about-us-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
