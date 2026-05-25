<template>
  <div class="practice-question-page">
    <van-nav-bar :title="`第${currentIndex + 1}/${totalQuestions}题`" left-text="退出" left-arrow @click-left="confirmExit" />

    <van-progress
      :percentage="Math.round(((currentIndex + 1) / totalQuestions) * 100)"
      stroke-color="var(--primary)"
      :show-pivot="false"
      style="height: 3px;"
    />

    <QuestionViewer
      ref="viewerRef"
      :questions="questions"
      :currentIndex="currentIndex"
      :showIndicator="false"
      :showScore="false"
      :submitText="'提交练习'"
      :submitting="submitting"
      @update:currentIndex="currentIndex = $event"
      @submit="submitAnswers"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, post } from '../../api'
import QuestionViewer from '../../components/QuestionViewer.vue'

const route = useRoute()
const router = useRouter()
const chapterId = route.params.chapterId
const viewerRef = ref(null)
const questions = ref([])
const currentIndex = ref(0)
const submitting = ref(false)

const totalQuestions = computed(() => questions.value.length)

async function fetchQuestions() {
  try {
    const res = await get('/chapters/' + chapterId + '/questions')
    if (res.data) {
      questions.value = res.data
      viewerRef.value?.initAnswers(questions.value)
    }
  } catch (e) {
    questions.value = [
      { id: 1, content: 'Spring Boot的默认配置文件是什么？', type: 'SINGLE',
        options: ['application.xml', 'application.properties', 'config.yml', 'settings.xml'], answer: 1 },
      { id: 2, content: '以下哪些是Spring Boot的特性？', type: 'MULTIPLE',
        options: ['自动配置', '起步依赖', 'Actuator监控', 'XML配置'], answer: [0, 1, 2] },
      { id: 3, content: 'Spring Boot只能用于Web开发', type: 'JUDGE', answer: false },
      { id: 4, content: '请简述Spring Boot的自动配置原理', type: 'ESSAY', answer: '' }
    ]
    viewerRef.value?.initAnswers(questions.value)
  }
}

function confirmExit() {
  showConfirmDialog({
    title: '退出练习',
    message: '确定要退出吗？已作答的题目将不会保存。',
  }).then(() => {
    router.back()
  }).catch(() => {})
}

async function submitAnswers() {
  submitting.value = true
  const adata = viewerRef.value?.answers || {}
  try {
    const res = await post('/practice/submit', {
      chapterId: parseInt(chapterId),
      answers: Object.keys(adata).map(key => ({
        questionId: parseInt(key),
        answer: adata[key]
      }))
    })
    if (res.data) {
      const resultData = {
        score: res.data.score || 0,
        totalCount: totalQuestions.value,
        correctCount: res.data.correctCount || 0,
        questions: questions.value,
        userAnswers: { ...adata },
        correctAnswers: res.data.correctAnswers || [],
        explanations: res.data.explanations || []
      }
      localStorage.setItem('practice_result_' + chapterId, JSON.stringify(resultData))
      router.push('/practice/' + chapterId + '/result')
    }
  } catch (e) {
    let correctCount = 0
    questions.value.forEach(q => {
      const userAns = adata[q.id]
      if (q.type === 'MULTIPLE') {
        if (JSON.stringify([...(userAns || [])].sort()) === JSON.stringify([...(q.answer || [])].sort())) correctCount++
      } else if (q.type === 'ESSAY') {
        correctCount++
      } else {
        if (userAns === q.answer) correctCount++
      }
    })
    const resultData = {
      score: Math.round((correctCount / totalQuestions.value) * 100),
      totalCount: totalQuestions.value,
      correctCount,
      questions: questions.value,
      userAnswers: { ...adata },
      correctAnswers: questions.value.map(q => q.answer),
      explanations: questions.value.map(() => '暂无解析')
    }
    localStorage.setItem('practice_result_' + chapterId, JSON.stringify(resultData))
    router.push('/practice/' + chapterId + '/result')
  }
  submitting.value = false
}

onMounted(() => {
  fetchQuestions()
})
</script>

<style scoped>
.practice-question-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 70px;
}
</style>
