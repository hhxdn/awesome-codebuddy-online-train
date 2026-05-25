<template>
  <div class="practice-question-page">
    <van-nav-bar :title="`第${currentIndex + 1}/${totalQuestions}题`" left-text="退出" left-arrow @click-left="confirmExit" />
    <van-progress
      :percentage="Math.round(((currentIndex + 1) / totalQuestions) * 100)"
      stroke-color="#1989fa"
      style="margin: 0; padding: 0;"
    />

    <!-- Question -->
    <div class="question-container" v-if="currentQuestion">
      <van-tag :type="tagType" size="medium" class="question-type">
        {{ typeLabel }}
      </van-tag>

      <div class="question-content">{{ currentQuestion.content }}</div>

      <!-- Single Choice -->
      <van-radio-group v-if="currentQuestion.type === 'SINGLE'" v-model="answers[currentQuestion.id]" class="options">
        <van-cell
          v-for="(opt, idx) in currentQuestion.options"
          :key="idx"
          clickable
          :title="opt"
          @click="answers[currentQuestion.id] = idx"
        >
          <template #right-icon>
            <van-radio :name="idx" />
          </template>
        </van-cell>
      </van-radio-group>

      <!-- Multiple Choice -->
      <van-checkbox-group v-if="currentQuestion.type === 'MULTIPLE'" v-model="answers[currentQuestion.id]" class="options">
        <van-cell
          v-for="(opt, idx) in currentQuestion.options"
          :key="idx"
          clickable
          :title="opt"
          @click="toggleMultiple(currentQuestion.id, idx)"
        >
          <template #right-icon>
            <van-checkbox :name="idx" />
          </template>
        </van-cell>
      </van-checkbox-group>

      <!-- Judge -->
      <div v-if="currentQuestion.type === 'JUDGE'" class="judge-options">
        <van-button
          :type="answers[currentQuestion.id] === true ? 'primary' : 'default'"
          size="large"
          @click="answers[currentQuestion.id] = true"
        >正确</van-button>
        <van-button
          :type="answers[currentQuestion.id] === false ? 'danger' : 'default'"
          size="large"
          @click="answers[currentQuestion.id] = false"
        >错误</van-button>
      </div>

      <!-- Essay -->
      <div v-if="currentQuestion.type === 'ESSAY'" class="essay-option">
        <van-field
          v-model="answers[currentQuestion.id]"
          type="textarea"
          rows="4"
          placeholder="请输入你的答案..."
          autosize
        />
      </div>
    </div>

    <!-- Navigation -->
    <div class="question-nav">
      <van-button :disabled="currentIndex === 0" @click="prevQuestion">上一题</van-button>
      <van-button
        v-if="currentIndex < totalQuestions - 1"
        type="primary"
        @click="nextQuestion"
      >下一题</van-button>
      <van-button
        v-else
        type="danger"
        :loading="submitting"
        @click="submitAnswers"
      >提交练习</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const chapterId = route.params.chapterId
const questions = ref([])
const currentIndex = ref(0)
const answers = reactive({})
const submitting = ref(false)

const totalQuestions = computed(() => questions.value.length)
const currentQuestion = computed(() => questions.value[currentIndex.value])

const tagType = computed(() => {
  const map = { SINGLE: 'primary', MULTIPLE: 'warning', JUDGE: 'success', ESSAY: '' }
  return map[currentQuestion.value?.type] || 'primary'
})

const typeLabel = computed(() => {
  const map = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }
  return map[currentQuestion.value?.type] || '未知'
})

async function fetchQuestions() {
  try {
    const res = await get('/chapters/' + chapterId + '/questions')
    if (res.data) {
      questions.value = res.data
      initAnswers()
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

function toggleMultiple(qId, idx) {
  if (!answers[qId]) answers[qId] = []
  const arr = answers[qId]
  const pos = arr.indexOf(idx)
  if (pos >= 0) {
    arr.splice(pos, 1)
  } else {
    arr.push(idx)
  }
}

function prevQuestion() {
  if (currentIndex.value > 0) currentIndex.value--
}

function nextQuestion() {
  if (currentIndex.value < totalQuestions.value - 1) currentIndex.value++
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
  try {
    const res = await post('/practice/submit', {
      chapterId: parseInt(chapterId),
      answers: Object.keys(answers).map(key => ({
        questionId: parseInt(key),
        answer: answers[key]
      }))
    })
    if (res.data) {
      const resultData = {
        score: res.data.score || 0,
        totalCount: totalQuestions.value,
        correctCount: res.data.correctCount || 0,
        questions: questions.value,
        userAnswers: { ...answers },
        correctAnswers: res.data.correctAnswers || [],
        explanations: res.data.explanations || []
      }
      localStorage.setItem('practice_result_' + chapterId, JSON.stringify(resultData))
      router.push('/practice/' + chapterId + '/result')
    }
  } catch (e) {
    // Mock submit
    let correctCount = 0
    questions.value.forEach(q => {
      const userAns = answers[q.id]
      if (q.type === 'MULTIPLE') {
        if (JSON.stringify([...userAns].sort()) === JSON.stringify([...(q.answer || [])].sort())) correctCount++
      } else if (q.type === 'ESSAY') {
        correctCount++ // Essay always marked correct in mock
      } else {
        if (userAns === q.answer) correctCount++
      }
    })
    const resultData = {
      score: Math.round((correctCount / totalQuestions.value) * 100),
      totalCount: totalQuestions.value,
      correctCount,
      questions: questions.value,
      userAnswers: { ...answers },
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
  padding-bottom: 60px;
}

.question-container {
  background: #fff;
  margin: 12px;
  padding: 20px;
  border-radius: 8px;
}

.question-type {
  margin-bottom: 12px;
}

.question-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.options .van-cell {
  margin-bottom: 4px;
}

.judge-options {
  display: flex;
  gap: 16px;
}

.judge-options .van-button {
  flex: 1;
}

.essay-option {
  margin-top: 12px;
}

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
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.question-nav .van-button {
  flex: 1;
}
</style>
