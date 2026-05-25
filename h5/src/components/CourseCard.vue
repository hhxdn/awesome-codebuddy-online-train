<template>
  <div class="course-card" @click="goDetail">
    <div class="card-cover">
      <img v-if="course.cover" :src="course.cover" alt="" />
      <div v-else class="cover-placeholder">
        <span class="cover-icon">{{ course.title?.charAt(0) || '课' }}</span>
      </div>
      <div class="cover-badge" v-if="(course.price || 0) === 0">
        <span class="badge-text">免费</span>
      </div>
      <div class="cover-badge badge-pro" v-else>
        <span class="badge-text">¥{{ course.price }}</span>
      </div>
    </div>
    <div class="card-body">
      <h3 class="card-title text-ellipsis-2">{{ course.title }}</h3>
      <div class="card-meta">
        <span class="meta-tag">{{ course.categoryName || '通用' }}</span>
        <span class="meta-students">
          <van-icon name="friends-o" size="12" />
          {{ formatCount(course.studentCount || 0) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  course: { type: Object, required: true }
})

const router = useRouter()

function formatCount(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function goDetail() {
  router.push('/course/' + props.course.id)
}
</script>

<style scoped>
.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.course-card:active {
  transform: scale(0.97);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-cover {
  position: relative;
  width: 100%;
  height: 110px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 32px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 700;
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(34, 197, 94, 0.9);
  backdrop-filter: blur(4px);
}

.cover-badge.badge-pro {
  background: rgba(239, 68, 68, 0.9);
}

.badge-text {
  font-size: 11px;
  color: #fff;
  font-weight: 600;
}

.card-body {
  padding: 10px 10px 12px;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.4;
  margin-bottom: 8px;
  min-height: 36px;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-tag {
  font-size: 11px;
  color: var(--primary);
  background: #e8f4ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.meta-students {
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
