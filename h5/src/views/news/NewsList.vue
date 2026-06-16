<template>
  <div class="news-list-page page-fade-in">
    <van-nav-bar title="最新资讯" left-arrow @click-left="$router.back()" fixed placeholder />
    
    <!-- Module Tabs -->
    <div class="module-tabs" v-if="modules.length > 0">
      <div class="tabs-scroll">
        <div
          class="tab-chip"
          :class="{ active: activeModule === 0 }"
          @click="switchModule(0)"
        >全部</div>
        <div
          v-for="mod in modules"
          :key="mod.id"
          class="tab-chip"
          :class="{ active: activeModule === mod.id }"
          @click="switchModule(mod.id)"
        >{{ mod.name }}</div>
      </div>
    </div>

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
const modules = ref([])
const activeModule = ref(0)
const loading = ref(true)
const refreshing = ref(false)

async function fetchModules() {
  try {
    const res = await get('/config/news-modules')
    modules.value = res.data || []
  } catch (e) {
    modules.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (activeModule.value > 0) params.moduleId = activeModule.value
    const res = await get('/news', params)
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function switchModule(id) {
  activeModule.value = id
  fetchData()
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
  fetchModules()
  fetchData()
})
</script>

<style scoped>
.news-list-page {
  min-height: 100vh;
  background: var(--bg-color);
}

.module-tabs {
  background: #fff;
  position: sticky;
  top: 46px;
  z-index: 99;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-light);
}

.tabs-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.tabs-scroll::-webkit-scrollbar {
  display: none;
}

.tab-chip {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 16px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.tab-chip.active {
  background: var(--primary);
  color: #fff;
  font-weight: 600;
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

@media (min-width: 768px) {
  .news-items {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 20px 24px;
  }
  .news-item {
    border-radius: 12px;
    padding: 16px;
    transition: transform 0.2s, box-shadow 0.2s;
  }
  .news-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  }
  .news-cover {
    width: 220px;
    height: 138px;
    border-radius: 8px;
  }
  .news-info.has-cover {
    min-height: 138px;
  }
  .news-title {
    font-size: 17px;
  }
  .news-summary {
    font-size: 13px;
  }
  .news-meta {
    font-size: 12px;
  }
}
</style>
