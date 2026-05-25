<template>
  <div class="exam-list-page page-fade-in">
    <van-nav-bar title="考试列表" :border="false" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="— 没有更多考试 —"
      >
        <div v-for="exam in examList" :key="exam.id" class="exam-card" @click="goExam(exam)">
          <div class="exam-card-header">
            <div class="exam-icon-wrap">
              <van-icon name="certificate" size="22" color="#fff" />
            </div>
            <div class="exam-name-wrap">
              <h3>{{ exam.name }}</h3>
              <span class="exam-status" :class="'exam-' + getStatusType(exam)">
                {{ getStatusText(exam) }}
              </span>
            </div>
          </div>
          <div class="exam-card-info">
            <div class="exam-info-item">
              <van-icon name="clock-o" size="15" color="var(--primary)" />
              <span>{{ exam.duration || 60 }}分钟</span>
            </div>
            <div class="exam-info-item">
              <van-icon name="gold-coin-o" size="15" color="#f59e0b" />
              <span>满分{{ exam.totalScore || 100 }}分</span>
            </div>
            <div class="exam-info-item">
              <van-icon name="passed" size="15" color="var(--success)" />
              <span>及格{{ exam.passScore || 60 }}分</span>
            </div>
          </div>
          <div class="exam-card-footer" v-if="exam.questionCount">
            <van-icon name="description" size="14" color="var(--text-muted)" />
            <span>共{{ exam.questionCount }}题</span>
            <van-icon name="arrow" size="14" color="var(--text-muted)" style="margin-left: auto;" />
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && !refreshing && examList.length === 0" description="暂无考试" />
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
  if (exam.userScore !== undefined) return 'fail'
  return 'new'
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
  margin: 8px 12px;
  padding: 18px;
  border-radius: var(--radius);
  box-shadow: var(--shadow-xs);
  cursor: pointer;
  transition: all var(--transition);
}

.exam-card:active {
  transform: scale(0.985);
  box-shadow: var(--shadow-sm);
}

.exam-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.exam-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.exam-name-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
}

.exam-name-wrap h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.exam-status {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
  flex-shrink: 0;
  margin-left: 8px;
}

.exam-status.exam-success {
  background: var(--success-light);
  color: var(--success);
}

.exam-status.exam-fail {
  background: var(--danger-light);
  color: var(--danger);
}

.exam-status.exam-new {
  background: var(--primary-bg);
  color: var(--primary);
}

.exam-card-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.exam-info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-secondary);
}

.exam-card-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}
</style>
