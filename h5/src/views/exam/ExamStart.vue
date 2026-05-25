<template>
  <div class="exam-start-page">
    <van-nav-bar title="考试确认" left-text="返回" left-arrow @click-left="$router.back()" />

    <div class="exam-info-card">
      <h2>{{ exam.name }}</h2>
      <div class="exam-meta">
        <van-tag type="warning" size="medium">⏱ {{ exam.duration }}分钟</van-tag>
        <van-tag type="primary" size="medium">📝 满分{{ exam.totalScore }}分</van-tag>
        <van-tag type="success" size="medium">✅ 及格{{ exam.passScore }}分</van-tag>
        <van-tag size="medium">📋 共{{ exam.questionCount }}题</van-tag>
      </div>
    </div>

    <van-cell-group inset title="考试须知">
      <van-cell title="1. 考试开始后计时开始，时间到自动交卷" />
      <van-cell title="2. 切换窗口或后台3次以上将记作作弊" />
      <van-cell title="3. 确认答案后无法修改" />
      <van-cell title="4. 考试期间请保持网络畅通" />
    </van-cell-group>

    <div class="exam-actions">
      <van-button type="primary" block round size="large" @click="startExam">
        开始考试
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const paperId = route.params.paperId

const exam = ref({})

async function fetchExamInfo() {
  try {
    const res = await get('/exams/' + paperId)
    if (res.data) exam.value = res.data
  } catch (e) {
    exam.value = {
      id: paperId,
      name: 'Java基础考试',
      duration: 60,
      totalScore: 100,
      passScore: 60,
      questionCount: 20
    }
  }
}

async function startExam() {
  try {
    const res = await post('/exam/start', { paperId: parseInt(paperId) })
    if (res.data?.recordId) {
      router.push('/exam/do/' + res.data.recordId)
    }
  } catch (e) {
    // Mock start
    showToast('考试开始')
    router.push('/exam/do/' + Date.now())
  }
}

onMounted(() => {
  fetchExamInfo()
})
</script>

<style scoped>
.exam-start-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.exam-info-card {
  background: #fff;
  margin: 12px;
  padding: 20px;
  border-radius: 8px;
}

.exam-info-card h2 {
  font-size: 18px;
  margin-bottom: 16px;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.exam-actions {
  padding: 24px 16px;
}
</style>
