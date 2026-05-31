<template>
  <div class="news-list-page page-fade-in">
    <van-nav-bar title="最新资讯" left-arrow @click-left="$router.back()" fixed placeholder />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="news-items" v-if="list.length > 0">
        <div
          v-for="item in list"
          :key="item.id"
          class="news-item"
          @click="$router.push('/news/' + item.id)"
        >
          <img v-if="item.cover" :src="item.cover" class="news-cover" />
          <div class="news-info" :class="{ 'has-cover': item.cover }">
            <div class="news-title">{{ item.title }}</div>
            <div class="news-summary" v-if="item.summary">{{ item.summary }}</div>
            <div class="news-meta">
              <span v-if="item.source">{{ item.source }}</span>
              <span>{{ item.viewCount || 0 }}阅读</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <EmptyState v-else-if="!loading" description="暂无资讯" />
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const list = ref([])
const loading = ref(true)
const refreshing = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/news')
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onRefresh() {
  refreshing.value = true
  fetchData().finally(() => { refreshing.value = false })
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.news-list-page {
  min-height: 100vh;
  background: var(--bg-color);
}

.news-items {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  gap: 10px;
  cursor: pointer;
  align-items: flex-start;
}

.news-cover {
  width: 110px;
  height: 72px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.news-info {
  flex: 1;
  min-width: 0;
}

.news-info.has-cover {
  min-height: 72px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.news-title {
  font-size: 15px;
  font-weight: 500;
  color: #1D2129;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-summary {
  font-size: 12px;
  color: #86909C;
  line-height: 1.5;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #C9CDD4;
  margin-top: 6px;
}
</style>
