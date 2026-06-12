<template>
  <div>
    <!-- Header slot for mode-specific header (countdown / progress) -->
    <slot name="header" />

    <!-- Question Indicator (for exam mode) -->
    <div v-if="showIndicator" class="question-indicator-row">
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

    <!-- Question Content -->
    <div class="question-body" v-if="currentQuestion">
      <div class="question-header">
        <van-tag :type="tagType" size="medium">{{ typeLabel }}</van-tag>
        <span class="question-score" v-if="showScore && currentQuestion.score">
          {{ currentQuestion.score }}分
        </span>
      </div>

      <div class="question-content">
        <span class="question-number" v-if="showNumber">{{ currentIndex + 1 }}.</span>
        {{ currentQuestion.content }}
      </div>

      <!-- Single Choice -->
      <van-radio-group v-if="currentQuestion.type === 'SINGLE'" v-model="answers[currentQuestion.id]" class="options-list">
        <div
          v-for="(opt, idx) in currentQuestion.options"
          :key="idx"
          class="option-item"
          :class="{ 'option-selected': answers[currentQuestion.id] === idx }"
          @click="answers[currentQuestion.id] = idx"
        >
          <span class="option-label">{{ optionLabels[idx] }}</span>
          <span class="option-text">{{ opt }}</span>
          <van-radio :name="idx" class="option-radio" />
        </div>
      </van-radio-group>

      <!-- Multiple Choice -->
      <van-checkbox-group v-if="currentQuestion.type === 'MULTIPLE'" v-model="answers[currentQuestion.id]" class="options-list">
        <div
          v-for="(opt, idx) in currentQuestion.options"
          :key="idx"
          class="option-item"
          :class="{ 'option-selected': (answers[currentQuestion.id] || []).includes(idx) }"
          @click="toggleMultiple(currentQuestion.id, idx)"
        >
          <span class="option-label">{{ optionLabels[idx] }}</span>
          <span class="option-text">{{ opt }}</span>
          <van-checkbox :name="idx" class="option-radio" @click.stop />
        </div>
      </van-checkbox-group>

      <!-- Judge -->
      <div v-if="currentQuestion.type === 'JUDGE'" class="judge-options">
        <div
          class="judge-btn judge-true"
          :class="{ active: answers[currentQuestion.id] === '0' }"
          @click="answers[currentQuestion.id] = '0'"
        >
          <van-icon name="success" size="28" />
          <span>正确</span>
        </div>
        <div
          class="judge-btn judge-false"
          :class="{ active: answers[currentQuestion.id] === '1' }"
          @click="answers[currentQuestion.id] = '1'"
        >
          <van-icon name="cross" size="28" />
          <span>错误</span>
        </div>
      </div>

      <!-- Essay -->
      <div v-if="currentQuestion.type === 'ESSAY'" class="essay-option">
        <van-field
          v-model="answers[currentQuestion.id]"
          type="textarea"
          rows="5"
          placeholder="请输入你的答案..."
          autosize
          maxlength="2000"
          show-word-limit
        />
      </div>
    </div>

    <!-- Navigation -->
    <div class="question-nav safe-bottom">
      <van-button :disabled="currentIndex === 0" @click="prevQuestion" class="nav-btn-prev">
        <van-icon name="arrow-left" />
        上一题
      </van-button>
      <div class="nav-progress" v-if="!showIndicator">
        {{ currentIndex + 1 }} / {{ questions.length }}
      </div>
      <van-button
        v-if="currentIndex < questions.length - 1"
        type="primary"
        @click="nextQuestion"
        class="nav-btn-next"
      >
        下一题
        <van-icon name="arrow" />
      </van-button>
      <van-button
        v-else
        type="danger"
        :loading="submitting"
        @click="$emit('submit')"
        class="nav-btn-submit"
      >
        {{ submitText }}
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'

const props = defineProps({
  questions: { type: Array, required: true },
  currentIndex: { type: Number, default: 0 },
  showIndicator: { type: Boolean, default: false },
  showScore: { type: Boolean, default: true },
  showNumber: { type: Boolean, default: true },
  submitText: { type: String, default: '提交' },
  submitting: { type: Boolean, default: false }
})

const emit = defineEmits(['update:currentIndex', 'update:answers', 'submit', 'prev', 'next'])

const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
const answers = reactive({})

const currentQuestion = computed(() => props.questions[props.currentIndex] || null)

const tagType = computed(() => {
  const map = { SINGLE: 'primary', MULTIPLE: 'warning', JUDGE: 'success', ESSAY: '' }
  return map[currentQuestion.value?.type] || 'primary'
})

const typeLabel = computed(() => {
  const map = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }
  return map[currentQuestion.value?.type] || ''
})

function isAnswered(qId) {
  const ans = answers[qId]
  if (ans === undefined || ans === null || ans === '') return false
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

function prevQuestion() {
  if (props.currentIndex > 0) {
    emit('update:currentIndex', props.currentIndex - 1)
    emit('prev')
  }
}

function nextQuestion() {
  if (props.currentIndex < props.questions.length - 1) {
    emit('update:currentIndex', props.currentIndex + 1)
    emit('next')
  }
}

// Watch questions to init answers
watch(() => props.questions, (qs) => {
  if (!qs) return
  qs.forEach(q => {
    if (q.type === 'MULTIPLE') {
      if (!answers[q.id]) answers[q.id] = []
    } else if (q.type === 'ESSAY') {
      if (!answers[q.id]) answers[q.id] = ''
    } else {
      if (answers[q.id] === undefined) answers[q.id] = null
    }
  })
}, { immediate: true })

// Expose answers and helpers for parent
defineExpose({ answers, isAnswered, initAnswers: (qs) => {
  qs.forEach(q => {
    if (q.type === 'MULTIPLE') {
      if (!answers[q.id]) answers[q.id] = []
    } else if (q.type === 'ESSAY') {
      if (!answers[q.id]) answers[q.id] = ''
    } else {
      if (answers[q.id] === undefined) answers[q.id] = null
    }
  })
}})
</script>

<style scoped>
.question-body {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.question-score {
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
}

.question-content {
  font-size: 16px;
  line-height: 1.7;
  margin-bottom: 20px;
  color: var(--text-color);
  font-weight: 500;
}

.question-number {
  color: var(--primary);
  margin-right: 4px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1.5px solid var(--border-color);
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-item:active {
  transform: scale(0.98);
}

.option-selected {
  border-color: var(--primary);
  background: #e8f4ff;
}

.option-label {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e5e5e5;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.option-selected .option-label {
  background: var(--primary);
  color: #fff;
}

.option-text {
  flex: 1;
  font-size: 15px;
  color: var(--text-color);
}

.option-radio {
  flex-shrink: 0;
  margin-left: 8px;
}

.judge-options {
  display: flex;
  gap: 16px;
}

.judge-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid var(--border-color);
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 15px;
  font-weight: 500;
}

.judge-btn:active {
  transform: scale(0.96);
}

.judge-true.active {
  border-color: var(--success);
  background: #e6f9ee;
  color: var(--success);
}

.judge-false.active {
  border-color: var(--danger);
  background: #fde8ec;
  color: var(--danger);
}

.essay-option {
  margin-top: 4px;
  border-radius: 10px;
  overflow: hidden;
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
  align-items: center;
  gap: 10px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.nav-btn-prev,
.nav-btn-next,
.nav-btn-submit {
  flex: 1;
  border-radius: 24px;
}

.nav-progress {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.question-indicator-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 16px;
  background: var(--white);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
}

.question-indicator-dot {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--border-color);
  background: var(--white);
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.question-indicator-dot.current {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.question-indicator-dot.answered {
  background: #e8f4ff;
  border-color: var(--primary);
  color: var(--primary);
}
</style>
