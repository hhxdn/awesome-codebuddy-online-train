<template>
  <div class="exam-list-page">
    <van-nav-bar title="考试列表" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="— 没有更多考试 —"
      >
        <div v-for="exam in examList" :key="exam.id" class="exam-card" @click="goExam(exam)">
          <div class="exam-card-header">
            <div class="exam-name-wrap">
              <van-icon name="certificate" size="20" color="var(--primary)" />
              <h3>{{ exam.name }}</h3>
            </div>
            <van-tag :type="getStatusType(exam)" size="medium" round>
              {{ getStatusText(exam) }}
            </van-tag>
          </div>
          <div class="exam-card-tags">
            <span class="exam-tag"><van-icon name="clock-o" size="13" /> {{ exam.duration || 60 }}分钟</span>
            <span class="exam-tag"><van-icon name="gold-coin-o" size="13" /> 满分{{ exam.totalScore || 100 }}分</span>
            <span class="exam-tag"><van-icon name="passed" size="13" /> 及格{{ exam.passScore || 60 }}分</span>
          </div>
          <div class="exam-card-footer" v-if="exam.questionCount">
            <van-icon name="description" size="13" color="var(--text-muted)" />
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
  margin: 10px 12px;
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s;
}

.exam-card:active {
  transform: scale(0.98);
}

.exam-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.exam-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.exam-name-wrap h3 {
  font-size: 16px;
  font-weight: 600;
}

.exam-card-tags {
  display: flex;
  gap: 14px;
  margin-bottom: 10px;
}

.exam-tag {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 3px;
}

.exam-card-footer {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
