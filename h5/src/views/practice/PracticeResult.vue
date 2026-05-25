<template>
  <div class="practice-result-page">
    <van-nav-bar title="练习结果" left-text="返回课程" left-arrow @click-left="goBackCourse" />

    <!-- Score Display -->
    <div class="score-circle" :class="result.pass ? 'pass' : 'fail'">
      <span class="score-num">{{ result.score }}</span>
      <span class="score-label">分</span>
    </div>
    <div class="result-status">
      <h3 v-if="result.pass">🎉 太棒了!</h3>
      <h3 v-else>💪 继续加油!</h3>
      <p>共{{ result.totalCount }}题，答对{{ result.correctCount }}题，正确率{{ correctRate }}%</p>
    </div>

    <van-divider>答题回顾</van-divider>

    <!-- Question Review -->
    <div class="review-list">
      <div v-for="(q, index) in result.questions" :key="q.id" class="review-item">
        <div class="review-header">
          <span class="review-index">{{ index + 1 }}</span>
          <span class="review-type">{{ getTypeLabel(q.type) }}</span>
          <van-tag :type="isCorrect(index) ? 'success' : 'danger'" size="small">
            {{ isCorrect(index) ? '正确' : '错误' }}
          </van-tag>
        </div>
        <div class="review-content">{{ q.content }}</div>
        <div class="review-answer">
          <p><span class="label">我的答案：</span>{{ formatUserAnswer(index, q) }}</p>
          <p><span class="label">正确答案：</span>{{ formatCorrectAnswer(index, q) }}</p>
          <p v-if="result.explanations[index]" class="review-explanation">
            <span class="label">解析：</span>{{ result.explanations[index] }}
          </p>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="result-actions">
      <van-button block round @click="retry">重新练习</van-button>
      <van-button block round type="primary" style="margin-top: 12px;" @click="goBackCourse">返回课程</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const chapterId = route.params.chapterId

const result = ref({
  score: 0,
  totalCount: 0,
  correctCount: 0,
  pass: false,
  questions: [],
  userAnswers: {},
  correctAnswers: [],
  explanations: []
})

const correctRate = computed(() => {
  if (result.value.totalCount === 0) return 0
  return Math.round((result.value.correctCount / result.value.totalCount) * 100)
})

function getTypeLabel(type) {
  const map = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
  return map[type] || type
}

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
  if (q.type === 'MULTIPLE') {
    return (answer || []).map(i => q.options?.[i]).join(', ') || '未作答'
  }
  if (q.type === 'JUDGE') {
    if (answer === null) return '未作答'
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

function retry() {
  router.replace('/practice/' + chapterId + '/do')
}

function goBackCourse() {
  router.push('/')
}

onMounted(() => {
  try {
    const saved = localStorage.getItem('practice_result_' + chapterId)
    if (saved) {
      result.value = JSON.parse(saved)
      result.value.pass = correctRate.value >= 60
    }
  } catch {
    // 数据损坏，使用默认值
  }
})
</script>

<style scoped>
.practice-result-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.result-status {
  text-align: center;
  margin: 0 20px 20px;
}

.result-status h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.result-status p {
  font-size: 14px;
  color: var(--text-secondary);
}

.review-list {
  margin: 0 12px;
}

.review-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.review-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.review-type {
  font-size: 12px;
  color: var(--text-secondary);
}

.review-content {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-answer {
  font-size: 14px;
  line-height: 1.8;
  padding: 10px;
  background: var(--bg-color);
  border-radius: 4px;
}

.review-answer .label {
  color: var(--text-secondary);
}

.review-explanation {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-color);
  color: var(--primary);
}

.result-actions {
  padding: 20px 16px 30px;
}
</style>
