<template>
  <div class="exam-list-page">
    <van-nav-bar title="考试列表" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多考试"
      >
        <div v-for="exam in examList" :key="exam.id" class="exam-card" @click="goExam(exam)">
          <div class="exam-card-header">
            <h3>{{ exam.name }}</h3>
            <van-tag :type="getStatusType(exam)" size="medium">
              {{ getStatusText(exam) }}
            </van-tag>
          </div>
          <div class="exam-card-info">
            <span>⏱ {{ exam.duration || 60 }}分钟</span>
            <span>📝 满分{{ exam.totalScore || 100 }}分</span>
            <span>✅ 及格{{ exam.passScore || 60 }}分</span>
          </div>
          <div class="exam-card-desc" v-if="exam.questionCount">
            共{{ exam.questionCount }}题
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && examList.length === 0" description="暂无考试" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId || route.params.id || ''

const examList = ref([])
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(true)

async function fetchExams() {
  loading.value = true
  try {
    let res
    if (courseId) {
      res = await get('/courses/' + courseId + '/exams')
    } else {
      res = await get('/exams')
    }
    if (res.data) examList.value = res.data.records || res.data || []
  } catch (e) {
    examList.value = [
      { id: 1, name: 'Java基础考试', duration: 60, totalScore: 100, passScore: 60, questionCount: 20 },
      { id: 2, name: 'Spring Boot综合测试', duration: 90, totalScore: 100, passScore: 60, questionCount: 30 }
    ]
  }
  loading.value = false
  finished.value = true
}

function getStatusType(exam) {
  if (exam.userScore >= exam.passScore) return 'success'
  if (exam.userScore !== undefined) return 'danger'
  return 'primary'
}

function getStatusText(exam) {
  if (exam.userScore !== undefined && exam.userScore >= exam.passScore) return '已通过'
  if (exam.userScore !== undefined) return '未通过'
  return '未参加'
}

function goExam(exam) {
  router.push('/exam/start/' + exam.id)
}

function onRefresh() {
  refreshing.value = true
  fetchExams().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchExams()
})
</script>

<style scoped>
.exam-list-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.exam-card {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
}

.exam-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.exam-card-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.exam-card-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.exam-card-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
