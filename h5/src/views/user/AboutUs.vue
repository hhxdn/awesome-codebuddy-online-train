<template>
  <div class="about-us-page">
    <van-nav-bar title="关于我们" left-arrow fixed placeholder @click-left="$router.back()" />
    <div v-if="loading" class="skeleton">
      <van-skeleton title :row="6" />
    </div>
    <div v-else-if="content" class="content" v-html="content" />
    <van-empty v-else description="暂无内容" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../../api'

const loading = ref(true)
const content = ref('')

onMounted(() => {
  get('/config/about_us').then(res => {
    content.value = res.data?.configValue || ''
  }).finally(() => {
    loading.value = false
  })
})
</script>

<style scoped>
.about-us-page {
  min-height: 100vh;
  background: #fff;
}
.skeleton {
  padding: 20px 16px;
}
.content {
  padding: 16px;
  line-height: 1.8;
  font-size: 15px;
  color: #333;
  word-break: break-word;
}
/* 富文本图片自适应 */
.content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
}
.content :deep(p) {
  margin: 8px 0;
}
.content :deep(h1),
.content :deep(h2),
.content :deep(h3) {
  margin: 16px 0 8px;
  color: #1a1a1a;
}
</style>
