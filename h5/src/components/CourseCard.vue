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
      <div v-if="course.courseType === 'OFFLINE'" class="cover-type-badge">线下</div>
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
  aspect-ratio: 3 / 2;
  overflow: hidden;
  border-radius: 10px 10px 0 0;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-card:hover .card-cover img {
  transform: scale(1.06);
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

.cover-type-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(237, 123, 47, 0.88);
  color: #fff;
  backdrop-filter: blur(8px);
}

.card-body {
  padding-top: 10px;
  padding-left: 12px;
  padding-right: 12px;
  padding-bottom: 12px;
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

@media (min-width: 768px) {
  .course-card {
    border-radius: 12px;
  }
  .card-cover {
    border-radius: 12px 12px 0 0;
  }
  .card-body {
    padding: 14px 16px 16px;
  }
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
    min-height: 48px;
  }
  .meta-category {
    font-size: 12px;
    padding: 3px 10px;
  }
  .meta-students {
    font-size: 12px;
  }
  .cover-badge {
    font-size: 13px;
    padding: 3px 10px;
    top: 12px;
    right: 12px;
  }
  .cover-type-badge {
    font-size: 13px;
    padding: 3px 10px;
    top: 12px;
    left: 12px;
  }
  .cover-placeholder .cover-text {
    font-size: 56px;
  }
}

@media (min-width: 1200px) {
  .course-card {
    border-radius: 14px;
  }
  .card-cover {
    border-radius: 14px 14px 0 0;
  }
  .card-body {
    padding: 16px 18px 18px;
  }
  .card-title {
    font-size: 18px;
    min-height: 54px;
  }
  .meta-category {
    font-size: 13px;
  }
  .meta-students {
    font-size: 13px;
  }
  .cover-placeholder .cover-text {
    font-size: 64px;
  }
}

@media (min-width: 1600px) {
  .card-title {
    font-size: 19px;
    min-height: 56px;
  }
  .meta-category {
    font-size: 14px;
  }
  .meta-students {
    font-size: 14px;
  }
  .cover-placeholder .cover-text {
    font-size: 72px;
  }
}
</style>
