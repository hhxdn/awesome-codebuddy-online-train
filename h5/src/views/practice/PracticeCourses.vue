<template>
  <div class="practice-courses-page page-fade-in">
    <div class="page-header">
      <h2 class="header-title">我的练习</h2>
      <p class="header-desc">选择课程开始练习</p>
    </div>

    <!-- 加载中 -->
    <van-loading v-if="loading" class="loading-center" size="32" type="spinner" color="var(--primary)" />

    <!-- 无练习权限提示 -->
    <div v-else-if="courses.length === 0" class="empty-state">
      <van-icon name="notes-o" size="64" color="#C9CDD4" />
      <p class="empty-title">暂无练习权限</p>
      <p class="empty-desc">您还没有开通任何课程的练习题权限，请联系管理员为您开通</p>
      <van-button round plain type="primary" size="small" @click="$router.push('/courses')">
        去选课
      </van-button>
    </div>

    <!-- 课程列表 -->
    <div v-else class="course-list">
      <div
        v-for="item in courses"
        :key="item.courseId"
        class="course-card"
        @click="goCoursePractice(item)"
      >
        <div class="card-cover">
          <van-image
            :src="item.courseCover || 'https://picsum.photos/seed/practice/480/270'"
            fit="cover"
            width="100%"
            height="100%"
            radius="10"
          />
          <div class="cover-badge" v-if="item.price && item.price > 0">
            <van-icon name="gold-coin-o" size="12" /> 付费
          </div>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ item.courseTitle }}</h3>
          <div class="card-stats">
            <span class="stat-item">
              <van-icon name="records" size="14" color="var(--primary)" />
              {{ item.chapterCount }}个章节
            </span>
            <span class="stat-item">
              <van-icon name="description" size="14" color="var(--success)" />
              {{ item.questionCount }}道题目
            </span>
          </div>
        </div>
        <div class="card-arrow">
          <van-icon name="arrow" size="16" color="#C9CDD4" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../../api'

const router = useRouter()
const courses = ref([])
const loading = ref(true)

async function fetchCourses() {
  loading.value = true
  try {
    const res = await get('/courses/exercise/my-courses')
    if (res.data) courses.value = res.data
  } catch (e) {
    courses.value = []
  } finally {
    loading.value = false
  }
}

function goCoursePractice(item) {
  // 跳转到课程详情页的章节练习
  router.push('/course/' + item.courseId)
}

onMounted(() => fetchCourses())
</script>

<style scoped>
.practice-courses-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 60px;
}

.page-header {
  background: linear-gradient(160deg, #0052D9, #366EF4);
  padding: 20px 20px 24px;
  color: #fff;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
}

.header-desc {
  font-size: 13px;
  opacity: 0.75;
  margin: 0;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 120px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 30px 60px;
  text-align: center;
}

.empty-title {
  font-size: 17px;
  font-weight: 600;
  color: #1D2129;
  margin: 18px 0 8px;
}

.empty-desc {
  font-size: 14px;
  color: #86909C;
  margin: 0 0 28px;
  line-height: 1.6;
  max-width: 280px;
}

.course-list {
  padding: 12px 14px;
}

.course-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.course-card:active {
  transform: scale(0.98);
}

.card-cover {
  width: 90px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
}

.cover-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
  backdrop-filter: blur(4px);
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-arrow {
  flex-shrink: 0;
  padding: 4px;
}
</style>
