<template>
  <div class="my-courses-page">
    <van-nav-bar title="我的课程" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多课程">
        <div v-for="course in courseList" :key="course.id" class="course-item" @click="goDetail(course)">
          <van-image :src="course.cover" width="120" height="80" fit="cover" round />
          <div class="course-info">
            <h4>{{ course.title }}</h4>
            <van-progress :percentage="course.progress || 0" stroke-color="#1989fa" />
            <span class="progress-text">已学{{ course.progress || 0 }}% · {{ course.studiedChapters || 0 }}/{{ course.totalChapters || 0 }}节</span>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && courseList.length === 0" description="还没有购买课程" />
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
const loading = ref(false)
const finished = ref(true)

async function fetchCourses() {
  loading.value = true
  try {
    const res = await get('/user/courses')
    if (res.data) courseList.value = res.data.records || res.data || []
  } catch (e) {
    courseList.value = [
      { id: 1, title: 'Spring Boot实战教程', cover: '', progress: 60, studiedChapters: 3, totalChapters: 5 },
      { id: 2, title: 'Vue3从入门到精通', cover: '', progress: 20, studiedChapters: 1, totalChapters: 5 }
    ]
  }
  loading.value = false
  finished.value = true
}

function goDetail(course) {
  router.push('/course/' + course.id)
}

function onRefresh() {
  refreshing.value = true
  fetchCourses().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.my-courses-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.course-item {
  background: #fff;
  margin: 12px;
  padding: 12px;
  border-radius: 8px;
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.course-info h4 {
  font-size: 15px;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
