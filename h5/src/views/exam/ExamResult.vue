<template>
  <div class="exam-result-page">
    <van-nav-bar title="考试结果" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Score Display -->
    <div class="score-circle" :class="result.passed ? 'pass' : 'fail'">
      <span class="score-num">{{ result.score }}</span>
      <span class="score-label">/ {{ result.totalScore }}分</span>
    </div>

    <div class="result-status">
      <h3>
        <van-icon :name="result.passed ? 'success' : 'fail'" size="24" />
        {{ result.passed ? '恭喜通过!' : '未通过' }}
      </h3>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-value">{{ result.duration || 0 }}分钟</span>
        <span class="stat-label">答题时长</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ result.correctCount }}/{{ result.totalCount }}</span>
        <span class="stat-label">答对题数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ result.cheatCount || 0 }}</span>
        <span class="stat-label">切出次数</span>
      </div>
    </div>

    <van-divider>题目回顾</van-divider>

    <!-- Question Review -->
    <div class="review-list">
      <div v-for="(q, index) in result.questions" :key="q.id" class="review-item">
        <van-collapse>
          <van-collapse-item>
            <template #title>
              <div class="collapse-header">
                <span class="review-index">{{ index + 1 }}</span>
                <span class="review-title-text">{{ q.content?.substring(0, 30) }}{{ q.content?.length > 30 ? '...' : '' }}</span>
                <van-tag :type="isCorrect(index) ? 'success' : 'danger'" size="small">
                  {{ isCorrect(index) ? '正确' : '错误' }}
                </van-tag>
              </div>
            </template>
            <div class="review-content">{{ q.content }}</div>
            <div class="review-answer">
              <p><span class="label">我的答案：</span>{{ formatUserAnswer(index, q) }}</p>
              <p><span class="label">正确答案：</span>{{ formatCorrectAnswer(index, q) }}</p>
              <p v-if="result.explanations[index]" class="review-explanation">
                <span class="label">解析：</span>{{ result.explanations[index] }}
              </p>
            </div>
          </van-collapse-item>
        </van-collapse>
      </div>
    </div>

    <div class="result-actions">
      <van-button block round type="primary" @click="goBack">返回课程</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId

const result = ref({
  score: 0,
  totalScore: 100,
  passScore: 60,
  passed: false,
  duration: 0,
  correctCount: 0,
  totalCount: 0,
  cheatCount: 0,
  questions: [],
  userAnswers: {},
  correctAnswers: [],
  explanations: []
})

function isCorrect(index) {
  const q = result.value.questions[index]
  const userAns = result.value.userAnswers[q.id]
  const correctAns = result.value.correctAnswers[index]
  if (q.type === 'MULTIPLE') {
    return JSON.stringify([...(userAns || [])].sort()) === JSON.stringify([...(correctAns || [])].sort())
  }
  if (q.type === 'ESSAY') return true
  return userAns === correctAns
}

function formatUserAnswer(index, q) {
  const answer = result.value.userAnswers[q.id]
  if (q.type === 'SINGLE') return q.options?.[answer] || '未作答'
  if (q.type === 'MULTIPLE') return (answer || []).map(i => q.options?.[i]).join(', ') || '未作答'
  if (q.type === 'JUDGE') {
    if (answer === null || answer === undefined) return '未作答'
    return answer ? '正确' : '错误'
  }
  return answer || '未作答'
}

function formatCorrectAnswer(index, q) {
  const answer = result.value.correctAnswers[index]
  if (q.type === 'SINGLE') return q.options?.[answer] || ''
  if (q.type === 'MULTIPLE') return (answer || []).map(i => q.options?.[i]).join(', ')
  if (q.type === 'JUDGE') return answer ? '正确' : '错误'
  return '（主观题，教师批改）'
}

function goBack() {
  router.push('/')
}

onMounted(() => {
  const saved = localStorage.getItem('exam_result_' + recordId)
  if (saved) {
    result.value = JSON.parse(saved)
  }
})
</script>

<style scoped>
.exam-result-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.result-status {
  text-align: center;
  margin-bottom: 20px;
}

.result-status h3 {
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.review-list {
  margin: 0 12px;
}

.review-item {
  margin-bottom: 8px;
}

.collapse-header {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.review-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}

.review-title-text {
  flex: 1;
  font-size: 13px;
  color: #666;
}

.review-content {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
  padding: 0 4px;
}

.review-answer {
  font-size: 14px;
  line-height: 1.8;
  padding: 10px;
  background: var(--bg-color);
  border-radius: 4px;
}

.review-answer .label { color: var(--text-secondary); }
.review-explanation { margin-top: 8px; padding-top: 8px; border-top: 1px dashed var(--border-color); color: var(--primary); }

.result-actions {
  padding: 20px 16px 40px;
}
</style>
