<template>
  <div class="course-detail-page">
    <van-nav-bar title="课程详情" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Course Banner -->
    <van-image :src="course.cover || ''" width="100%" height="200" fit="cover">
      <template #error>
        <div class="cover-placeholder">课程封面</div>
      </template>
    </van-image>

    <!-- Course Info -->
    <div class="course-info">
      <h2 class="course-title">{{ course.title }}</h2>
      <div class="course-meta">
        <span class="price" v-if="(course.price || 0) === 0">免费</span>
        <span class="price" v-else>¥{{ course.price }}</span>
        <van-tag type="primary" size="medium">{{ course.categoryName }}</van-tag>
        <span class="student-count">{{ course.studentCount || 0 }}人已学</span>
      </div>
    </div>

    <van-divider />

    <!-- Course Description -->
    <div class="course-desc">
      <h3>课程介绍</h3>
      <div class="desc-content" :class="{ expanded: descExpanded }">
        {{ course.description || '暂无介绍' }}
      </div>
      <van-button
        v-if="(course.description || '').length > 150"
        size="small"
        plain
        type="primary"
        @click="descExpanded = !descExpanded"
        class="toggle-desc"
      >
        {{ descExpanded ? '收起' : '展开' }}
      </van-button>
    </div>

    <!-- Chapter List -->
    <van-divider>课程目录</van-divider>
    <div class="chapter-list" v-if="chapters.length > 0">
      <van-cell
        v-for="chapter in chapters"
        :key="chapter.id"
        :title="chapter.title"
        :label="chapter.duration || '视频'"
        @click="goChapter(chapter)"
      >
        <template #icon>
          <van-icon
            v-if="isPaid && !purchased"
            name="lock"
            color="#c8c9cc"
            style="margin-right: 8px;"
          />
          <van-icon
            v-else-if="chapter.completed"
            name="success"
            color="#07c160"
            style="margin-right: 8px;"
          />
          <van-icon
            v-else
            name="video-o"
            color="#1989fa"
            style="margin-right: 8px;"
          />
        </template>
        <template #right-icon>
          <van-button size="small" type="primary" plain @click.stop="goPractice(chapter)">
            练习
          </van-button>
        </template>
      </van-cell>
    </div>
    <EmptyState v-else description="暂无章节" />

    <!-- Bottom Bar -->
    <div class="bottom-bar safe-bottom">
      <template v-if="(course.price || 0) === 0">
        <van-button type="primary" block round @click="startLearn">开始学习</van-button>
      </template>
      <template v-else-if="!purchased">
        <div class="bottom-info">
          <span class="price">¥{{ course.price }}</span>
        </div>
        <van-button type="primary" round @click="buyNow">立即购买</van-button>
      </template>
      <template v-else>
        <van-button type="primary" block round @click="startLearn">继续学习</van-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id
const course = ref({})
const chapters = ref([])
const purchased = ref(false)
const isPaid = ref(false)
const descExpanded = ref(false)

async function fetchDetail() {
  try {
    const res = await get('/courses/' + courseId)
    if (res.data) course.value = res.data
    isPaid.value = (course.value.price || 0) > 0
  } catch (e) {
    // Mock
    course.value = {
      id: courseId,
      title: 'Spring Boot 实战教程',
      cover: '',
      price: 0,
      categoryName: 'Java开发',
      studentCount: 1234,
      description: '本课程从零开始，系统讲解Spring Boot框架的核心特性和实战应用，包括自动配置、起步依赖、Actuator监控、配置文件、日志管理、数据访问、安全控制、缓存、消息队列、定时任务等核心模块。'
    }
  }

  try {
    const res = await get('/courses/' + courseId + '/chapters')
    if (res.data) chapters.value = res.data
  } catch (e) {
    chapters.value = [
      { id: 1, title: '第一章：Spring Boot入门', duration: '12:30' },
      { id: 2, title: '第二章：配置文件详解', duration: '18:45' },
      { id: 3, title: '第三章：数据访问层', duration: '25:20' }
    ]
  }

  try {
    const res = await get('/courses/' + courseId + '/access')
    if (res.data) purchased.value = res.data.purchased
  } catch (e) {
    purchased.value = false
  }
}

function goChapter(chapter) {
  if (isPaid.value && !purchased.value) {
    showToast('请先购买课程')
    return
  }
  router.push('/video/' + chapter.id)
}

function goPractice(chapter) {
  if (isPaid.value && !purchased.value) {
    showToast('请先购买课程')
    return
  }
  router.push('/practice/' + chapter.id)
}

function startLearn() {
  if (chapters.value.length > 0) {
    router.push('/video/' + chapters.value[0].id)
  } else {
    showToast('暂无章节内容')
  }
}

function buyNow() {
  router.push('/order/confirm/' + courseId)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.course-detail-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 70px;
}

.cover-placeholder {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.course-info {
  background: #fff;
  padding: 16px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: var(--danger);
}

.student-count {
  color: var(--text-secondary);
  font-size: 13px;
}

.course-desc {
  background: #fff;
  padding: 16px;
}

.course-desc h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.desc-content {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  overflow: hidden;
  max-height: 100px;
  transition: max-height 0.3s;
}

.desc-content.expanded {
  max-height: none;
}

.toggle-desc {
  margin-top: 8px;
}

.chapter-list {
  background: #fff;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.bottom-info {
  flex: 1;
}
</style>
