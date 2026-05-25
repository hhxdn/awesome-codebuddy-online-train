<template>
  <div class="exam-result-page">
    <van-nav-bar title="考试结果" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Score Display -->
    <div class="result-hero">
      <div class="score-circle" :class="result.passed ? 'pass' : 'fail'">
        <span class="score-num">{{ result.score }}</span>
        <span class="score-label">/ {{ result.totalScore }}分</span>
      </div>
      <div class="result-verdict" :class="result.passed ? 'pass' : 'fail'">
        <van-icon :name="result.passed ? 'success' : 'cross'" size="22" />
        {{ result.passed ? '恭喜通过！' : '很遗憾，未通过' }}
      </div>
      <p class="result-pass-line" v-if="!result.passed">
        及格线 {{ result.passScore }}分，下次继续加油！
      </p>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-value">{{ result.duration || 0 }}<small>分钟</small></span>
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

    <!-- Question Review -->
    <div class="section-card">
      <div class="section-title">题目回顾</div>
      <div class="review-list">
        <van-collapse v-for="(q, index) in result.questions" :key="q.id">
          <van-collapse-item>
            <template #title>
              <div class="collapse-header">
                <span class="review-index" :class="isCorrect(index) ? 'correct' : 'wrong'">
                  {{ index + 1 }}
                </span>
                <span class="review-title-text">
                  {{ q.content?.substring(0, 28) }}{{ q.content?.length > 28 ? '...' : '' }}
                </span>
                <van-tag :type="isCorrect(index) ? 'success' : 'danger'" size="mini" round>
                  {{ isCorrect(index) ? '正确' : '错误' }}
                </van-tag>
              </div>
            </template>
            <div class="review-body">
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
          </van-collapse-item>
        </van-collapse>
      </div>
    </div>

    <div class="result-actions">
      <van-button block round type="primary" size="large" @click="goBack">返回首页</van-button>
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

function goBack() {
  router.push('/')
}

onMounted(() => {
  try {
    const saved = localStorage.getItem('exam_result_' + recordId)
    if (saved) {
      result.value = JSON.parse(saved)
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.exam-result-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 20px;
}

.result-hero {
  padding-top: 10px;
}

.result-verdict {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.result-verdict.pass { color: var(--success); }
.result-verdict.fail { color: var(--danger); }

.result-pass-line {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 6px;
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

.review-list :deep(.van-collapse-item) {
  margin-bottom: 8px;
  border-radius: 10px;
  overflow: hidden;
}

.collapse-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
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

.review-title-text {
  flex: 1;
  font-size: 13px;
  color: #666;
}

.review-body {
  padding: 4px 0;
}

.review-question {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 14px;
  color: var(--text-color);
  font-weight: 500;
}

.review-answer-box {
  background: var(--bg-color);
  border-radius: 8px;
  padding: 12px;
}

.answer-row {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.answer-row:last-child { margin-bottom: 0; }

.answer-label {
  font-size: 13px;
  color: var(--text-muted);
  flex-shrink: 0;
  min-width: 56px;
}

.answer-value {
  font-size: 14px;
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
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
}

.review-explanation .answer-label {
  display: block;
  margin-bottom: 4px;
}

.review-explanation p {
  font-size: 13px;
  color: var(--primary);
  line-height: 1.7;
}

.result-actions {
  padding-top: 20px; padding-left: 16px; padding-right: 16px; padding-bottom: 40px;
}

.stat-item { position: relative; }
.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -28px;
  top: 20%;
  height: 60%;
  width: 1px;
  background: var(--border-color);
}

.stat-value small {
  font-size: 13px;
  font-weight: 400;
}
</style>
