<template>
  <div class="chapter-list-page">
    <van-nav-bar title="课程目录" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Course Progress -->
    <div class="course-progress">
      <div class="progress-header">
        <span class="progress-title">学习进度</span>
        <span class="progress-text">{{ finishedCount }} / {{ totalCount }}节 · {{ progressPercent }}%</span>
      </div>
      <van-progress :percentage="progressPercent" stroke-color="var(--primary)" :show-pivot="false" />
    </div>

    <!-- Chapter List -->
    <div class="chapters" v-if="chapters.length > 0">
      <div
        v-for="(chapter, index) in chapters"
        :key="chapter.id"
        class="chapter-item"
      >
        <div class="chapter-status">
          <van-tag
            v-if="chapter.status === 'completed'"
            type="success"
            size="small"
            round
          >已完成</van-tag>
          <van-tag
            v-else-if="chapter.status === 'learning'"
            type="primary"
            size="small"
            round
          >学习中</van-tag>
          <van-tag
            v-else
            type="default"
            size="small"
            round
          >未学习</van-tag>
        </div>
        <div class="chapter-content">
          <div class="chapter-title">{{ index + 1 }}. {{ chapter.title }}</div>
          <div class="chapter-meta">
            <van-icon name="clock-o" size="12" color="var(--text-muted)" />
            {{ chapter.duration || '视频' }}
          </div>
        </div>
        <div class="chapter-actions">
          <van-button size="mini" round type="primary" plain @click.stop="goVideo(chapter)">
            视频
          </van-button>
          <van-button size="mini" round type="warning" plain @click.stop="goPractice(chapter)">
            练习
          </van-button>
        </div>
      </div>
    </div>

    <EmptyState v-if="chapters.length === 0" description="暂无章节" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id
const chapters = ref([])

const totalCount = computed(() => chapters.value.length)
const finishedCount = computed(() => chapters.value.filter(c => c.status === 'completed').length)
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((finishedCount.value / totalCount.value) * 100)
})

async function fetchChapters() {
  try {
    const res = await get('/courses/' + courseId + '/chapters')
    if (res.data) chapters.value = res.data
  } catch (e) {
    chapters.value = [
      { id: 1, title: '第一章：Spring Boot入门', duration: '12:30', status: 'completed' },
      { id: 2, title: '第二章：配置文件详解', duration: '18:45', status: 'learning' },
      { id: 3, title: '第三章：数据访问层', duration: '25:20', status: '' }
    ]
  }
}

function goVideo(chapter) {
  router.push('/video/' + chapter.id)
}

function goPractice(chapter) {
  router.push('/practice/' + chapter.id)
}

onMounted(() => {
  fetchChapters()
})
</script>

<style scoped>
.chapter-list-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.course-progress {
  background: #fff;
  padding: 16px 18px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.progress-title {
  font-size: 15px;
  font-weight: 600;
}

.progress-text {
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
}

.chapters {
  background: #fff;
  margin: 0 12px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.chapter-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
}

.chapter-item:last-child {
  border-bottom: none;
}

.chapter-status {
  flex-shrink: 0;
}

.chapter-content {
  flex: 1;
  min-width: 0;
}

.chapter-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 4px;
}

.chapter-meta {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}

.chapter-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
</style>
