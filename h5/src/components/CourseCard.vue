<template>
  <div class="course-card" @click="goDetail">
    <div class="card-cover">
      <img v-if="course.cover" :src="course.cover" alt="" />
      <div v-else class="cover-placeholder" :style="{ background: getGradient() }">
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
      <div class="card-footer">
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

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)'
]

function getGradient() {
  const idx = (props.course.id || 0) % gradients.length
  return gradients[idx]
}

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
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition);
  cursor: pointer;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.course-card:active {
  transform: scale(0.97);
}

.card-cover {
  position: relative;
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.course-card:hover .card-cover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 800;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(34, 197, 94, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.cover-badge.badge-pro {
  background: rgba(239, 68, 68, 0.92);
}

.badge-text {
  font-size: 11px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.card-body {
  padding: 12px 12px 14px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.5;
  margin-bottom: 10px;
  min-height: 42px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-tag {
  font-size: 11px;
  color: var(--primary);
  background: var(--primary-bg);
  padding: 3px 8px;
  border-radius: 6px;
  font-weight: 500;
}

.meta-students {
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
