<template>
  <div class="my-learning-records-page">
    <van-nav-bar title="学习记录" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-for="course in courseList" :key="course.id" class="learning-card" @click="goDetail(course)">
        <div class="learning-header">
          <h4>{{ course.courseTitle }}</h4>
          <span class="progress-badge">{{ course.progress || 0 }}%</span>
        </div>
        <van-progress :percentage="course.progress || 0" stroke-color="var(--primary)" :show-pivot="false" />
        <div class="learning-stats">
          <span><van-icon name="bookmark-o" size="12" /> 已学 {{ course.studiedChapters || 0 }}/{{ course.totalChapters || 0 }} 节</span>
          <span><van-icon name="clock-o" size="12" /> {{ course.totalDuration || 0 }}分钟</span>
        </div>
      </div>
    </van-pull-refresh>

    <EmptyState v-if="!refreshing && courseList.length === 0" description="暂无学习记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const courseList = ref([])
const refreshing = ref(false)

async function fetchLearningRecords() {
  try {
    const res = await get('/user/learning-records')
    if (res.data) courseList.value = res.data.records || res.data || []
  } catch (e) {
    courseList.value = [
      { id: 1, courseTitle: 'Spring Boot实战教程', progress: 60, studiedChapters: 3, totalChapters: 5, totalDuration: 120 },
      { id: 2, courseTitle: 'Vue3从入门到精通', progress: 20, studiedChapters: 1, totalChapters: 5, totalDuration: 30 }
    ]
  }
}

function goDetail(course) {
  router.push('/course/' + course.id)
}

function onRefresh() {
  refreshing.value = true
  fetchLearningRecords().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchLearningRecords()
})
</script>

<style scoped>
.my-learning-records-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.learning-card {
  background: #fff;
  margin: 10px 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s;
}

.learning-card:active {
  transform: scale(0.98);
}

.learning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.learning-header h4 {
  font-size: 15px;
  font-weight: 600;
}

.progress-badge {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary);
  background: #e8f4ff;
  padding: 4px 10px;
  border-radius: 10px;
}

.learning-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 10px;
}

.learning-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (min-width: 768px) {
  .record-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  }
}
</style>
