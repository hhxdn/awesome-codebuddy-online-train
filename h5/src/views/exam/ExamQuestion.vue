<template>
  <div class="exam-question-page">
    <van-nav-bar title="考试中" left-text="退出考试" left-arrow @click-left="confirmExit" />

    <QuestionViewer
      ref="viewerRef"
      :questions="questions"
      :currentIndex="currentIndex"
      :showIndicator="true"
      :submitText="'交卷'"
      :submitting="submitting"
      @update:currentIndex="currentIndex = $event"
      @submit="handleSubmit"
    >
      <!-- Countdown Header -->
      <template #header>
        <div class="countdown-bar">
          <van-icon name="clock-o" />
          <span>剩余时间：</span>
          <van-count-down :time="remainingTime" format="mm:ss" @finish="autoSubmit" />
        </div>
      </template>
    </QuestionViewer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, post } from '../../api'
import QuestionViewer from '../../components/QuestionViewer.vue'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId
const viewerRef = ref(null)
const questions = ref([])
const currentIndex = ref(0)
const submitting = ref(false)
const cheatCount = ref(0)
const duration = ref(60 * 60)
const remainingTime = ref(duration.value * 1000)

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
      viewerRef.value?.initAnswers(questions.value)
    }
  } catch (e) {
    questions.value = [
      { id: 1, content: 'Java中，int类型的默认值是什么？', type: 'SINGLE',
        options: ['0', 'null', 'undefined', 'false'], answer: 0 },
      { id: 2, content: '以下哪些是Java关键字？', type: 'MULTIPLE',
        options: ['class', 'public', 'string', 'interface'], answer: [0, 1, 3] },
      { id: 3, content: 'Java是纯面向对象语言', type: 'JUDGE', answer: false }
    ]
    viewerRef.value?.initAnswers(questions.value)
  }
}

async function autoSubmit() {
  showToast('考试时间已到，自动交卷')
  await doSubmit()
}

function handleSubmit() {
  const unanswered = questions.value.filter(q => !viewerRef.value?.isAnswered(q.id)).length
  const msg = unanswered > 0
    ? `还有${unanswered}题未作答，确定交卷吗？`
    : '确定交卷吗？'
  showConfirmDialog({ title: '交卷确认', message: msg })
    .then(() => doSubmit())
    .catch(() => {})
}

async function doSubmit() {
  submitting.value = true
  const adata = viewerRef.value?.answers || {}
  try {
    const res = await post('/exam/submit', {
      recordId: parseInt(recordId),
      answers: Object.keys(adata).map(key => ({
        questionId: parseInt(key),
        answer: adata[key]
      })),
      cheatCount: cheatCount.value
    })
    if (res.data) {
      const resultData = {
        ...res.data,
        passed: res.data.isPass != null ? res.data.isPass : (res.data.passed || false),  // 统一使用 passed
        duration: res.data.duration || Math.round((duration.value * 1000 - remainingTime.value) / 1000 / 60),
        correctCount: res.data.rightCount != null ? res.data.rightCount : (res.data.correctCount || 0),
        totalCount: res.data.totalCount != null ? res.data.totalCount : questions.value.length,
        questions: questions.value,
        userAnswers: { ...adata },
        cheatCount: cheatCount.value
      }
      localStorage.setItem('exam_result_' + recordId, JSON.stringify(resultData))
      router.replace('/exam/result/' + recordId)
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e.message || '提交失败，请稍后重试'
    showToast(msg)
    // 倒计时仍在进行中（van-count-down 组件未销毁），时间到会自动提交
  }
  submitting.value = false
}

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
  padding-bottom: 70px;
}
</style>
