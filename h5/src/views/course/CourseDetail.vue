<template>
  <div class="course-detail-page">
    <!-- Course Banner -->
    <div class="course-banner">
      <van-nav-bar
        title="课程详情"
        left-text="返回"
        left-arrow
        @click-left="$router.back()"
        :style="{ backgroundColor: 'transparent' }"
      >
        <template #title>
          <span style="color: #fff; font-weight: 600;">课程详情</span>
        </template>
        <template #left>
          <van-icon name="arrow-left" size="20" color="#fff" @click="$router.back()" />
        </template>
      </van-nav-bar>
      <div class="banner-content">
        <h1 class="banner-title">{{ course.title }}</h1>
        <div class="banner-meta">
          <span class="banner-tag" :class="(course.price || 0) === 0 ? 'free' : 'paid'">
            {{ (course.price || 0) === 0 ? '免费' : '¥' + course.price }}
          </span>
          <span class="banner-cat">{{ course.categoryName }}</span>
          <span class="banner-count">
            <van-icon name="friends-o" size="14" />
            {{ course.studentCount || 0 }}人已学
          </span>
        </div>
      </div>
    </div>

    <!-- Course Description -->
    <div class="section-card">
      <div class="section-title">课程介绍</div>
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
        {{ descExpanded ? '收起' : '展开全部' }}
      </van-button>
    </div>

    <!-- Chapter List -->
    <div class="section-card">
      <div class="section-title">课程目录 ({{ chapters.length }}节)</div>
      <div class="chapter-list" v-if="chapters.length > 0">
        <div
          v-for="(chapter, index) in chapters"
          :key="chapter.id"
          class="chapter-item"
          @click="goChapter(chapter)"
        >
          <div class="chapter-index">{{ index + 1 }}</div>
          <div class="chapter-info">
            <div class="chapter-title text-ellipsis-2">{{ chapter.title }}</div>
            <div class="chapter-meta">
              <span class="chapter-duration">
                <van-icon name="clock-o" size="12" /> {{ chapter.duration || '视频' }}
              </span>
              <span v-if="chapter.completed" class="chapter-done">
                <van-icon name="success" size="12" color="var(--success)" /> 已完成
              </span>
            </div>
          </div>
          <div class="chapter-actions">
            <van-button
              v-if="!isPaid || purchased"
              size="small"
              round
              plain
              type="primary"
              @click.stop="goPractice(chapter)"
            >
              练习
            </van-button>
            <van-icon v-else name="lock" color="#c8c9cc" />
          </div>
        </div>
      </div>
      <EmptyState v-else description="暂无章节" />
    </div>

    <!-- Bottom Bar -->
    <div class="bottom-bar safe-bottom">
      <template v-if="(course.price || 0) === 0">
        <van-button type="primary" block round @click="startLearn" size="large">
          开始学习
        </van-button>
      </template>
      <template v-else-if="!purchased">
        <div class="bottom-info">
          <span class="bottom-price">¥{{ course.price }}</span>
        </div>
        <van-button type="primary" round size="large" @click="buyNow">
          立即购买
        </van-button>
      </template>
      <template v-else>
        <van-button type="primary" block round @click="startLearn" size="large">
          继续学习
        </van-button>
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
    course.value = {
      id: courseId,
      title: 'Spring Boot 实战教程',
      cover: '',
      price: 0,
      categoryName: 'Java开发',
      studentCount: 1234,
      description: '本课程从零开始，系统讲解Spring Boot框架的核心特性和实战应用。'
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

.course-banner {
  background: linear-gradient(160deg, var(--primary-dark), var(--primary), var(--primary-light));
  padding-bottom: 24px;
  position: relative;
}

.banner-content {
  padding: 8px 20px 0;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
  margin-bottom: 12px;
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.banner-tag {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  padding: 3px 12px;
  border-radius: 12px;
}

.banner-tag.free {
  background: rgba(34, 197, 94, 0.3);
}

.banner-tag.paid {
  background: rgba(239, 68, 68, 0.3);
}

.banner-cat {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.15);
  padding: 3px 10px;
  border-radius: 10px;
}

.banner-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  gap: 4px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  margin: 10px 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}

.desc-content {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  overflow: hidden;
  max-height: 88px;
  transition: max-height 0.3s;
}

.desc-content.expanded {
  max-height: none;
}

.toggle-desc {
  margin-top: 10px;
  border-radius: 20px;
}

.chapter-list {
  display: flex;
  flex-direction: column;
}

.chapter-item {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
  cursor: pointer;
  transition: background 0.15s;
}

.chapter-item:last-child {
  border-bottom: none;
}

.chapter-item:active {
  background: var(--bg-color);
  margin: 0 -16px;
  padding: 14px 16px;
  border-radius: 8px;
}

.chapter-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e8f4ff;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.chapter-info {
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
  display: flex;
  align-items: center;
  gap: 12px;
}

.chapter-duration {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 3px;
}

.chapter-done {
  font-size: 12px;
  color: var(--success);
  display: flex;
  align-items: center;
  gap: 3px;
}

.chapter-actions {
  flex-shrink: 0;
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
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-info {
  flex: 1;
}

.bottom-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--danger);
}

:deep(.van-nav-bar) {
  background: transparent !important;
}

:deep(.van-nav-bar .van-icon) {
  color: #fff !important;
}
</style>
