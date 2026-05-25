<template>
  <van-card
    :price="course.price || 0"
    :desc="course.categoryName || ''"
    :title="course.title"
    :thumb="course.cover || defaultCover"
    :origin-price="course.originPrice || undefined"
    currency="¥"
    @click="goDetail"
  >
    <template #price>
      <span v-if="(course.price || 0) === 0" class="price-free">免费</span>
      <span v-else class="price">¥{{ course.price }}</span>
    </template>
    <template #desc>
      <span>{{ course.categoryName || '通用' }}</span>
      <span style="margin-left: 8px;">{{ course.studentCount || 0 }}人学习</span>
    </template>
  </van-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  course: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const defaultCover = computed(() => {
  return 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="150"><rect fill="#e5e5e5" width="200" height="150"/><text x="100" y="80" text-anchor="middle" fill="#999" font-size="14">暂无封面</text></svg>'
  )
})

function goDetail() {
  router.push('/course/' + props.course.id)
}
</script>

<style scoped>
.van-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 0;
}

.price {
  color: var(--danger);
  font-weight: 700;
  font-size: 16px;
}

.price-free {
  color: var(--success);
  font-weight: 700;
  font-size: 16px;
}
</style>
