<template>
  <div class="exam-list-page page-fade-in">
    <van-nav-bar title="考试列表" :border="false" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="examList.length > 0">
        <div v-for="exam in examList" :key="exam.id" class="exam-card" @click="goExam(exam)">
          <div class="exam-top">
            <div class="exam-icon">
              <van-icon name="certificate" size="20" color="#fff" />
            </div>
            <div class="exam-main">
              <h3 class="exam-name">{{ exam.name }}</h3>
              <span class="exam-status" :class="'s-' + getStatusType(exam)">{{ getStatusText(exam) }}</span>
            </div>
          </div>
          <div class="exam-meta">
            <div class="meta-item"><van-icon name="clock-o" size="14" /> {{ exam.duration || 60 }}分钟</div>
            <div class="meta-item"><van-icon name="gold-coin-o" size="14" /> 满分{{ exam.totalScore || 100 }}分</div>
            <div class="meta-item"><van-icon name="passed" size="14" /> 及格{{ exam.passScore || 60 }}分</div>
          </div>
          <div class="exam-bottom" v-if="exam.questionCount">
            <span>共{{ exam.questionCount }}题</span>
            <van-icon name="arrow" size="14" />
          </div>
        </div>
      </div>
      <div v-else-if="!loading && !refreshing" class="empty-wrap">
        <EmptyState description="暂无考试" />
      </div>
    </van-pull-refresh>
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

async function fetchExams() {
  loading.value = true
  try {
    const res = courseId ? await get('/courses/' + courseId + '/exams') : await get('/exams')
    if (res.data) examList.value = res.data.records || res.data || []
  } catch (e) { examList.value = [] }
  loading.value = false
}

function getStatusType(exam) {
  if (exam.userScore >= exam.passScore) return 'pass'
  if (exam.userScore !== undefined) return 'fail'
  return 'new'
}
function getStatusText(exam) {
  if (exam.userScore !== undefined && exam.userScore >= exam.passScore) return '已通过'
  if (exam.userScore !== undefined) return '未通过'
  return '未参加'
}
function goExam(exam) { router.push('/exam/start/' + exam.id) }
function onRefresh() { refreshing.value = true; fetchExams().finally(() => { refreshing.value = false }) }

onMounted(() => fetchExams())
</script>

<style scoped>
.exam-list-page { background: var(--bg-color); min-height: 100vh; }

.exam-card {
  background: #fff; margin: 8px 12px; padding: 18px;
  border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  cursor: pointer; transition: all var(--transition);
}
.exam-card:active { transform: scale(0.985); }

.exam-top { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.exam-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #0052D9, #366EF4);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.exam-main { flex: 1; display: flex; align-items: center; justify-content: space-between; min-width: 0; }
.exam-name { font-size: 16px; font-weight: 600; color: var(--text-color); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.exam-status { font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; margin-left: 8px; }
.s-pass { background: var(--success-light); color: #00A870; }
.s-fail { background: var(--danger-light); color: #E34D59; }
.s-new { background: var(--primary-bg); color: var(--primary); }

.exam-meta { display: flex; gap: 20px; margin-bottom: 12px; }
.meta-item { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; }

.exam-bottom {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 12px; border-top: 1px solid var(--border-light);
  font-size: 12px; color: var(--text-muted);
}
</style>
