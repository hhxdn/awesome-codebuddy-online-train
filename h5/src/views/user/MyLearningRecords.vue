<template>
  <div class="my-learning-records-page">
    <van-nav-bar title="学习记录" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-for="course in courseList" :key="course.id" class="learning-card" @click="goDetail(course)">
        <h4>{{ course.courseTitle }}</h4>
        <van-progress :percentage="course.progress || 0" stroke-color="#1989fa" style="margin: 12px 0;" />
        <div class="learning-stats">
          <span>进度 {{ course.progress || 0 }}%</span>
          <span>已学 {{ course.studiedChapters || 0 }}/{{ course.totalChapters || 0 }} 节</span>
          <span>总时长 {{ course.totalDuration || 0 }}分钟</span>
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
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
}

.learning-card h4 {
  font-size: 15px;
  margin-bottom: 4px;
}

.learning-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
