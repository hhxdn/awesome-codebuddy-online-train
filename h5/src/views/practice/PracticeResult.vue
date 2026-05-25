<template>
  <div class="practice-result-page">
    <van-nav-bar title="练习结果" left-text="返回课程" left-arrow @click-left="goBackCourse" />

    <!-- Score Display -->
    <div class="result-hero">
      <div class="score-circle" :class="result.pass ? 'pass' : 'fail'">
        <span class="score-num">{{ result.score }}</span>
        <span class="score-label">分</span>
      </div>
      <div class="result-verdict">
        <span v-if="result.pass" class="verdict-pass">🎉 太棒了！继续保持</span>
        <span v-else class="verdict-fail">💪 继续加油！</span>
      </div>
      <p class="result-summary">共{{ result.totalCount }}题，答对{{ result.correctCount }}题，正确率{{ correctRate }}%</p>
    </div>

    <!-- Question Review -->
    <div class="section-card">
      <div class="section-title">答题回顾</div>
      <div class="review-list">
        <div v-for="(q, index) in result.questions" :key="q.id" class="review-card">
          <div class="review-card-header">
            <span class="review-index" :class="isCorrect(index) ? 'correct' : 'wrong'">
              {{ index + 1 }}
            </span>
            <span class="review-type">{{ getTypeLabel(q.type) }}</span>
            <van-tag :type="isCorrect(index) ? 'success' : 'danger'" size="mini" round>
              {{ isCorrect(index) ? '正确' : '错误' }}
            </van-tag>
          </div>
          <div class="review-question">{{ q.content }}</div>
          <div class="review-answer-box">
            <div class="answer-row">
              <span class="answer-label">我的答案</span>
              <span class="answer-value" :class="{ wrong: !isCorrect(index) }">
                {{ formatUserAnswer(index, q) }}
              </span>
            </div>
            <div class="answer-row" v-if="!isCorrect(index)">
              <span class="answer-label">正确答案</span>
              <span class="answer-value correct">{{ formatCorrectAnswer(index, q) }}</span>
            </div>
            <div class="review-explanation" v-if="result.explanations && result.explanations[index]">
              <span class="answer-label">解析</span>
              <p>{{ result.explanations[index] }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="result-actions">
      <van-button block round type="primary" size="large" @click="retry">重新练习</van-button>
      <van-button block round plain style="margin-top: 10px;" @click="goBackCourse">返回首页</van-button>
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
  if (q.type === 'MULTIPLE') return (answer || []).map(i => q.options?.[i]).join('、') || '未作答'
  if (q.type === 'JUDGE') {
    if (answer === null || answer === undefined) return '未作答'
    return answer === '0' ? '正确' : '错误'
  }
  return answer || '未作答'
}

function formatCorrectAnswer(index, q) {
  const answer = result.value.correctAnswers[index]
  if (q.type === 'SINGLE') return q.options?.[answer] || ''
  if (q.type === 'MULTIPLE') return (answer || []).map(i => q.options?.[i]).join('、')
  if (q.type === 'JUDGE') return answer === '0' ? '正确' : '错误'
  return '（主观题，待教师批改）'
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
      const parsed = JSON.parse(saved)
      result.value = { ...result.value, ...parsed }
      result.value.pass = correctRate.value >= 60
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.practice-result-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 20px;
}

.result-hero {
  padding: 10px 0 0;
}

.result-verdict {
  text-align: center;
  margin-bottom: 8px;
}

.verdict-pass {
  font-size: 18px;
  font-weight: 600;
  color: var(--success);
}

.verdict-fail {
  font-size: 18px;
  font-weight: 600;
  color: var(--warning);
}

.result-summary {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.section-card {
  background: #fff;
  border-radius: 12px;
  margin: 10px 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  background: var(--bg-color);
  border-radius: 10px;
  padding: 14px;
}

.review-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.review-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  color: #fff;
}

.review-index.correct { background: var(--success); }
.review-index.wrong { background: var(--danger); }

.review-type {
  font-size: 12px;
  color: var(--text-muted);
  flex: 1;
}

.review-question {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  color: var(--text-color);
  font-weight: 500;
}

.review-answer-box {
  background: #fff;
  border-radius: 8px;
  padding: 10px;
}

.answer-row {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
}

.answer-row:last-child { margin-bottom: 0; }

.answer-label {
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
  min-width: 52px;
}

.answer-value {
  font-size: 13px;
  color: var(--text-color);
  flex: 1;
}

.answer-value.wrong {
  color: var(--danger);
  text-decoration: line-through;
  text-decoration-color: var(--danger);
}

.answer-value.correct {
  color: var(--success);
  font-weight: 500;
}

.review-explanation {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-color);
}

.review-explanation .answer-label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.review-explanation p {
  font-size: 13px;
  color: var(--primary);
  line-height: 1.7;
}

.result-actions {
  padding: 20px 16px 40px;
}
</style>
