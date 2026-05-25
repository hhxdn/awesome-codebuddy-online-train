<template>
  <div class="practice-home-page">
    <van-nav-bar title="章节练习" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Chapter Info -->
    <div class="chapter-info-card">
      <h3>{{ chapter.title }}</h3>
      <div class="info-row">
        <div class="info-item">
          <span class="info-value">{{ totalCount }}</span>
          <span class="info-label">题目数量</span>
        </div>
        <div class="info-item">
          <span class="info-value">{{ bestScore }}分</span>
          <span class="info-label">最佳成绩</span>
        </div>
        <div class="info-item">
          <span class="info-value">{{ practiceCount }}次</span>
          <span class="info-label">练习次数</span>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-buttons">
      <van-button type="primary" block round @click="startPractice">开始刷题</van-button>
      <van-button plain block round style="margin-top: 12px;" @click="goWrongBook">错题本</van-button>
    </div>

    <!-- Practice History -->
    <div class="history-section" v-if="historyList.length > 0">
      <van-divider>练习记录</van-divider>
      <van-cell
        v-for="(item, index) in historyList"
        :key="index"
        :title="`第${index + 1}次练习`"
        :label="item.createTime || ''"
        :value="`${item.score || 0}分`"
      >
        <template #right-icon>
          <van-tag :type="(item.correctRate || 0) >= 60 ? 'success' : 'danger'" size="small">
            正确率{{ item.correctRate || 0 }}%
          </van-tag>
        </template>
      </van-cell>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '../../api'

const route = useRoute()
const router = useRouter()
const chapterId = route.params.chapterId
const chapter = ref({})
const totalCount = ref(0)
const bestScore = ref(0)
const practiceCount = ref(0)
const historyList = ref([])

async function fetchData() {
  try {
    const res = await get('/chapters/' + chapterId)
    if (res.data) chapter.value = res.data
  } catch (e) {
    chapter.value = { id: chapterId, title: '第一章：Spring Boot入门' }
  }

  try {
    const res = await get('/chapters/' + chapterId + '/practice/stats')
    if (res.data) {
      totalCount.value = res.data.totalCount || 10
      bestScore.value = res.data.bestScore || 0
      practiceCount.value = res.data.practiceCount || 0
      historyList.value = res.data.records || []
    }
  } catch (e) {
    totalCount.value = 10
    bestScore.value = 0
    practiceCount.value = 3
    historyList.value = [
      { score: 80, correctRate: 80, createTime: '2024-01-15' },
      { score: 60, correctRate: 60, createTime: '2024-01-14' }
    ]
  }
}

function startPractice() {
  router.push('/practice/' + chapterId + '/do')
}

function goWrongBook() {
  router.push('/my-wrong')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.practice-home-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.chapter-info-card {
  background: #fff;
  margin: 12px;
  padding: 20px;
  border-radius: 8px;
}

.chapter-info-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-around;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}

.info-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.action-buttons {
  padding: 16px;
}

.history-section {
  background: #fff;
  margin: 0 12px;
  border-radius: 8px;
}
</style>
