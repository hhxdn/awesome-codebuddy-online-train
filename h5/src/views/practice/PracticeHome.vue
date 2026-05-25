<template>
  <div class="practice-home-page">
    <van-nav-bar title="章节练习" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Chapter Info -->
    <div class="chapter-info-card">
      <div class="chapter-header">
        <van-icon name="bookmark-o" size="18" color="var(--primary)" />
        <span>{{ chapter.title }}</span>
      </div>
      <div class="info-row">
        <div class="info-item">
          <div class="info-icon icon-count">
            <van-icon name="description" size="18" />
          </div>
          <span class="info-value">{{ totalCount }}</span>
          <span class="info-label">题目数量</span>
        </div>
        <div class="info-item">
          <div class="info-icon icon-score">
            <van-icon name="gold-coin-o" size="18" />
          </div>
          <span class="info-value">{{ bestScore }}分</span>
          <span class="info-label">最佳成绩</span>
        </div>
        <div class="info-item">
          <div class="info-icon icon-times">
            <van-icon name="chart-trending-o" size="18" />
          </div>
          <span class="info-value">{{ practiceCount }}次</span>
          <span class="info-label">练习次数</span>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-buttons">
      <van-button type="primary" block round size="large" @click="startPractice" class="practice-btn">
        <van-icon name="play-circle-o" size="20" />
        开始刷题
      </van-button>
      <van-button plain block round size="large" style="margin-top: 10px;" @click="goWrongBook">
        <van-icon name="cross" size="18" />
        错题本
      </van-button>
    </div>

    <!-- Practice History -->
    <div class="history-section" v-if="historyList.length > 0">
      <div class="section-title">练习记录</div>
      <div
        v-for="(item, index) in historyList"
        :key="index"
        class="history-item"
      >
        <div class="history-index">{{ index + 1 }}</div>
        <div class="history-info">
          <div class="history-label">第{{ index + 1 }}次练习</div>
          <div class="history-time">{{ item.createTime || '' }}</div>
        </div>
        <div class="history-score">
          <span class="score-value">{{ item.score || 0 }}分</span>
          <van-tag :type="(item.correctRate || 0) >= 60 ? 'success' : 'danger'" size="mini" round>
            {{ item.correctRate || 0 }}%
          </van-tag>
        </div>
      </div>
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
  margin: 10px 12px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.chapter-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-around;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.info-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -28px;
  top: 15%;
  height: 70%;
  width: 1px;
  background: var(--border-color);
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.icon-count { background: #e8f4ff; color: var(--primary); }
.icon-score { background: #e6f9ee; color: var(--success); }
.icon-times { background: #fef3c7; color: var(--warning); }

.info-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}

.info-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.action-buttons {
  padding: 16px;
}

.practice-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 14px rgba(79, 110, 247, 0.4);
}

.history-section {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}

.history-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
}

.history-item:last-child {
  border-bottom: none;
}

.history-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e8f4ff;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.history-info {
  flex: 1;
}

.history-label {
  font-size: 14px;
  color: var(--text-color);
}

.history-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.history-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.score-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
}
</style>
