<template>
  <div class="my-courses-page">
    <van-nav-bar title="我的课程" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多课程 —">
        <div v-for="course in courseList" :key="course.id" class="course-item" @click="goDetail(course)">
          <van-image :src="course.cover" width="80" height="110" fit="cover" radius="8">
            <template #error>
              <div class="cover-placeholder">{{ course.title?.charAt(0) }}</div>
            </template>
          </van-image>
          <div class="course-info">
            <h4 class="text-ellipsis-2">{{ course.title }}</h4>
            <van-progress :percentage="course.progress || 0" stroke-color="var(--primary)" :show-pivot="false" />
            <span class="progress-text">已学{{ course.progress || 0 }}% · {{ course.studiedChapters || 0 }}/{{ course.totalChapters || 0 }}节</span>
          </div>
          <van-icon name="arrow" size="16" color="var(--text-muted)" />
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && courseList.length === 0" description="还没有购买课程，去逛逛吧" />
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
  margin: 10px 12px;
  padding: 14px;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.15s;
}

.course-item:active {
  transform: scale(0.98);
}

.cover-placeholder {
  width: 100px;
  height: 66px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  border-radius: 8px;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--text-color);
}

.progress-text {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
  display: block;
}

@media (min-width: 768px) {
  .course-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  }
}
</style>
