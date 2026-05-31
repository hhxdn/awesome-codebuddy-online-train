<template>
  <div class="news-detail-page page-fade-in">
    <van-nav-bar title="资讯详情" left-arrow @click-left="$router.back()" fixed placeholder />
    <div class="detail-content" v-if="article">
      <h1 class="detail-title">{{ article.title }}</h1>
      <div class="detail-meta">
        <span v-if="article.source">{{ article.source }}</span>
        <span>{{ article.viewCount || 0 }} 阅读</span>
        <span>{{ formatTime(article.createTime) }}</span>
      </div>
      <div v-if="article.cover" class="detail-cover">
        <img :src="article.cover" :alt="article.title" />
      </div>
      <div class="detail-body" v-html="article.content"></div>
    </div>
    <div v-else-if="loading" class="loading-wrap">
      <van-loading type="spinner" size="32" />
    </div>
    <EmptyState v-else description="资讯不存在" subText="该资讯可能已被删除或下架" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

async function fetchDetail() {
  loading.value = true
  try {
    const res = await get('/news/' + route.params.id)
    article.value = res.data
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') + ' ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  background: #fff;
}

.detail-content {
  padding: 20px 16px 40px;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.4;
  margin: 0 0 12px;
}

.detail-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #86909C;
  margin-bottom: 16px;
}

.detail-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.detail-cover img {
  width: 100%;
  display: block;
}

.detail-body {
  font-size: 15px;
  color: #4E5969;
  line-height: 1.8;
}

.detail-body :deep(h3) {
  font-size: 17px;
  font-weight: 600;
  color: #1D2129;
  margin: 20px 0 10px;
}

.detail-body :deep(p) {
  margin: 8px 0;
}

.detail-body :deep(img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 10px 0;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}
</style>
