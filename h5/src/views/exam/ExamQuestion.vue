<template>
  <div class="exam-question-page">
    <van-nav-bar title="考试中" left-text="退出考试" left-arrow @click-left="confirmExit" />

    <!-- Countdown -->
    <div class="countdown-bar">
      <van-icon name="clock-o" />
      <span>剩余时间：</span>
      <van-count-down :time="remainingTime" format="mm:ss" @finish="autoSubmit" />
    </div>

    <!-- Question Indicator -->
    <div class="question-indicator-row">
      <div
        v-for="(q, index) in questions"
        :key="q.id"
        :class="[
          'question-indicator-dot',
          { current: index === currentIndex, answered: isAnswered(q.id) }
        ]"
        @click="currentIndex = index"
      >
        {{ index + 1 }}
      </div>
    </div>

    <!-- Question -->
    <div class="question-container" v-if="currentQuestion">
      <van-tag :type="tagType" size="medium" class="question-type">
        {{ typeLabel }}
      </van-tag>
      <div class="question-content">{{ currentQuestion.content }}</div>

      <van-radio-group v-if="currentQuestion.type === 'SINGLE'" v-model="answers[currentQuestion.id]" class="options">
        <van-cell v-for="(opt, idx) in currentQuestion.options" :key="idx" clickable :title="opt"
          @click="answers[currentQuestion.id] = idx">
          <template #right-icon><van-radio :name="idx" /></template>
        </van-cell>
      </van-radio-group>

      <van-checkbox-group v-if="currentQuestion.type === 'MULTIPLE'" v-model="answers[currentQuestion.id]" class="options">
        <van-cell v-for="(opt, idx) in currentQuestion.options" :key="idx" clickable :title="opt"
          @click="toggleMultiple(currentQuestion.id, idx)">
          <template #right-icon><van-checkbox :name="idx" /></template>
        </van-cell>
      </van-checkbox-group>

      <div v-if="currentQuestion.type === 'JUDGE'" class="judge-options">
        <van-button :type="answers[currentQuestion.id] === true ? 'primary' : 'default'"
          size="large" @click="answers[currentQuestion.id] = true">正确</van-button>
        <van-button :type="answers[currentQuestion.id] === false ? 'danger' : 'default'"
          size="large" @click="answers[currentQuestion.id] = false">错误</van-button>
      </div>

      <div v-if="currentQuestion.type === 'ESSAY'" class="essay-option">
        <van-field v-model="answers[currentQuestion.id]" type="textarea" rows="4" placeholder="请输入答案..." autosize />
      </div>
    </div>

    <!-- Navigation -->
    <div class="question-nav">
      <van-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</van-button>
      <van-button v-if="currentIndex < questions.length - 1" type="primary" @click="currentIndex++">下一题</van-button>
      <van-button v-else type="danger" :loading="submitting" @click="handleSubmit">交卷</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId
const questions = ref([])
const currentIndex = ref(0)
const answers = reactive({})
const submitting = ref(false)
const cheatCount = ref(0)
const duration = ref(60 * 60) // default 60 min
const remainingTime = ref(duration.value * 1000)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const tagType = computed(() => {
  const map = { SINGLE: 'primary', MULTIPLE: 'warning', JUDGE: 'success', ESSAY: '' }
  return map[currentQuestion.value?.type] || 'primary'
})
const typeLabel = computed(() => {
  const map = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }
  return map[currentQuestion.value?.type] || '未知'
})

function isAnswered(qId) {
  const ans = answers[qId]
  if (ans === undefined || ans === null) return false
  if (Array.isArray(ans)) return ans.length > 0
  return true
}

function toggleMultiple(qId, idx) {
  if (!answers[qId]) answers[qId] = []
  const arr = answers[qId]
  const pos = arr.indexOf(idx)
  if (pos >= 0) arr.splice(pos, 1)
  else arr.push(idx)
}

function confirmExit() {
  showConfirmDialog({
    title: '退出考试',
    message: '确定要退出考试吗？已作答内容可能不会保存，本次考试将记为无效。',
  }).then(() => {
    router.back()
  }).catch(() => {})
}

async function fetchExamData() {
  try {
    const res = await get('/exam/records/' + recordId + '/questions')
    if (res.data) {
      questions.value = res.data.questions || []
      duration.value = res.data.duration || 3600
      remainingTime.value = duration.value * 1000
      initAnswers()
    }
  } catch (e) {
    questions.value = [
      { id: 1, content: 'Java中，int类型的默认值是什么？', type: 'SINGLE',
        options: ['0', 'null', 'undefined', 'false'], answer: 0 },
      { id: 2, content: '以下哪些是Java关键字？', type: 'MULTIPLE',
        options: ['class', 'public', 'string', 'interface'], answer: [0, 1, 3] },
      { id: 3, content: 'Java是纯面向对象语言', type: 'JUDGE', answer: false }
    ]
    initAnswers()
  }
}

function initAnswers() {
  questions.value.forEach(q => {
    if (q.type === 'MULTIPLE') {
      if (!answers[q.id]) answers[q.id] = []
    } else if (q.type === 'ESSAY') {
      if (!answers[q.id]) answers[q.id] = ''
    } else if (q.type === 'JUDGE') {
      if (answers[q.id] === undefined) answers[q.id] = null
    } else {
      if (answers[q.id] === undefined) answers[q.id] = null
    }
  })
}

function autoSubmit() {
  showToast('考试时间已到，自动交卷')
  doSubmit()
}

function handleSubmit() {
  const unanswered = questions.value.filter(q => !isAnswered(q.id)).length
  const msg = unanswered > 0
    ? `还有${unanswered}题未作答，确定交卷吗？`
    : '确定交卷吗？'
  showConfirmDialog({ title: '交卷确认', message: msg })
    .then(() => doSubmit())
    .catch(() => {})
}

async function doSubmit() {
  submitting.value = true
  try {
    const res = await post('/exam/submit', {
      recordId: parseInt(recordId),
      answers: Object.keys(answers).map(key => ({
        questionId: parseInt(key),
        answer: answers[key]
      })),
      cheatCount: cheatCount.value
    })
    if (res.data) {
      const resultData = {
        ...res.data,
        questions: questions.value,
        userAnswers: { ...answers },
        cheatCount: cheatCount.value
      }
      localStorage.setItem('exam_result_' + recordId, JSON.stringify(resultData))
      router.replace('/exam/result/' + recordId)
    }
  } catch (e) {
    // Mock submit
    const resultData = {
      recordId,
      score: 80,
      totalScore: 100,
      passScore: 60,
      passed: true,
      duration: 30,
      correctCount: 2,
      totalCount: questions.value.length,
      cheatCount: cheatCount.value,
      questions: questions.value,
      userAnswers: { ...answers },
      correctAnswers: questions.value.map(q => q.answer),
      explanations: questions.value.map(() => '')
    }
    localStorage.setItem('exam_result_' + recordId, JSON.stringify(resultData))
    router.replace('/exam/result/' + recordId)
  }
  submitting.value = false
}

// Cheat detection
function handleVisibilityChange() {
  if (document.hidden) {
    cheatCount.value++
    if (cheatCount.value <= 3) {
      showToast(`警告：第${cheatCount.value}次切出考试页面！`)
    }
    if (cheatCount.value > 3) {
      showToast('切出次数过多，系统将自动交卷！')
      setTimeout(() => doSubmit(), 1000)
    }
  }
}

onMounted(() => {
  fetchExamData()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.exam-question-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 60px;
}

.question-container {
  background: #fff;
  margin: 12px;
  padding: 20px;
  border-radius: 8px;
}

.question-type { margin-bottom: 12px; }
.question-content { font-size: 16px; line-height: 1.6; margin-bottom: 20px; }
.options .van-cell { margin-bottom: 4px; }

.judge-options { display: flex; gap: 16px; }
.judge-options .van-button { flex: 1; }
.essay-option { margin-top: 12px; }

.question-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
}
.question-nav .van-button { flex: 1; }
</style>
