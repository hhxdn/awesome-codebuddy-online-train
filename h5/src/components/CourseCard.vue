<template>
  <div class="course-card" @click="goDetail">
    <div class="card-cover">
      <img v-if="course.cover" :src="course.cover" alt="" />
      <div v-else class="cover-placeholder" :style="{ background: getGradient() }">
        <span class="cover-text">{{ course.title?.charAt(0) || '课' }}</span>
      </div>
      <div class="cover-badge" :class="(course.price || 0) === 0 ? 'free' : 'paid'">
        {{ (course.price || 0) === 0 ? '免费' : '¥' + course.price }}
      </div>
    </div>
    <div class="card-body">
      <h3 class="card-title text-ellipsis-2">{{ course.title }}</h3>
      <div class="card-meta">
        <span class="meta-category">{{ course.categoryName || '通用' }}</span>
        <span class="meta-students">{{ course.studentCount || 0 }}人学习</span>
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
  'linear-gradient(135deg, #0052D9 0%, #366EF4 100%)',
  'linear-gradient(135deg, #00A870 0%, #2BA471 100%)',
  'linear-gradient(135deg, #ED7B2F 0%, #E37318 100%)',
  'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)',
  'linear-gradient(135deg, #E34D59 0%, #C9353F 100%)',
  'linear-gradient(135deg, #0594FA 0%, #0052D9 100%)'
]

function getGradient() {
  const idx = (props.course.id || 0) % gradients.length
  return gradients[idx]
}

function goDetail() {
  router.push('/course/' + props.course.id)
}
</script>

<style scoped>
.course-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
  cursor: pointer;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.course-card:active {
  transform: scale(0.97);
}

.card-cover {
  position: relative;
  width: 100%;
  height: 136px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.course-card:hover .card-cover img {
  transform: scale(1.06);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder .cover-text {
  font-size: 42px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.25);
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
  backdrop-filter: blur(8px);
}

.cover-badge.free {
  background: rgba(0, 168, 112, 0.88);
  color: #fff;
}

.cover-badge.paid {
  background: rgba(227, 77, 89, 0.88);
  color: #fff;
}

.card-body {
  padding: 10px 12px 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
  line-height: 1.5;
  margin-bottom: 8px;
  min-height: 42px;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-category {
  font-size: 11px;
  color: var(--primary);
  background: var(--primary-bg);
  padding: 2px 8px;
  border-radius: 4px;
}

.meta-students {
  font-size: 11px;
  color: var(--text-muted);
}
</style>
