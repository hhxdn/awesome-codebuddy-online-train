<template>
  <div class="course-detail-page page-fade-in">
    <!-- Course Banner -->
    <div class="course-banner">
      <div class="banner-bg-decor">
        <div class="b-decor d1"></div>
        <div class="b-decor d2"></div>
      </div>
      <van-nav-bar
        left-arrow
        @click-left="$router.back()"
        :style="{ backgroundColor: 'transparent' }"
      >
        <template #title>
          <span style="color: #fff; font-weight: 600; font-size: 17px;">课程详情</span>
        </template>
      </van-nav-bar>
      <div class="banner-content">
        <h1 class="banner-title">{{ course.title }}</h1>
        <div class="banner-meta">
          <span class="banner-tag" :class="(course.price || 0) === 0 ? 'free' : 'paid'">
            {{ (course.price || 0) === 0 ? '免费' : '¥' + course.price }}
          </span>
          <span class="banner-cat">
            <span class="cat-dot"></span>
            {{ course.categoryName }}
          </span>
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
      <div
        v-if="(course.description || '').length > 150"
        class="desc-toggle"
        @click="descExpanded = !descExpanded"
      >
        <span>{{ descExpanded ? '收起' : '展开全部' }}</span>
        <van-icon :name="descExpanded ? 'arrow-up' : 'arrow-down'" size="14" />
      </div>
    </div>

    <!-- Chapter List -->
    <div class="section-card">
      <div class="section-title">
        课程目录
        <span class="chapter-total">共{{ chapters.length }}节</span>
      </div>
      <div class="chapter-list" v-if="chapters.length > 0">
        <div
          v-for="(chapter, index) in chapters"
          :key="chapter.id"
          class="chapter-item"
          @click="goChapter(chapter)"
        >
          <div class="chapter-index">{{ String(index + 1).padStart(2, '0') }}</div>
          <div class="chapter-info">
            <div class="chapter-title text-ellipsis-2">{{ chapter.title }}</div>
            <div class="chapter-meta">
              <span class="chapter-duration">
                <van-icon name="play-circle-o" size="13" /> {{ chapter.duration || '视频' }}
              </span>
              <span v-if="chapter.completed" class="chapter-done">
                <van-icon name="success" size="13" color="var(--success)" /> 已完成
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
              class="practice-btn"
            >
              练习
            </van-button>
            <van-icon v-else name="lock" color="#c8c9cc" size="16" />
          </div>
        </div>
      </div>
      <EmptyState v-else description="暂无章节" />
    </div>

    <!-- Bottom Bar -->
    <div class="bottom-bar safe-bottom">
      <div class="bottom-bg"></div>
      <template v-if="(course.price || 0) === 0">
        <van-button type="primary" block round @click="startLearn" size="large" class="learn-btn">
          <van-icon name="play-circle-o" size="18" />
          开始学习
        </van-button>
      </template>
      <template v-else-if="!purchased">
        <div class="bottom-info">
          <span class="bottom-price-label">课程价格</span>
          <span class="bottom-price">¥{{ course.price }}</span>
        </div>
        <van-button type="primary" round size="large" @click="buyNow" class="buy-btn">
          立即购买
        </van-button>
      </template>
      <template v-else>
        <van-button type="primary" block round @click="startLearn" size="large" class="learn-btn">
          <van-icon name="play-circle-o" size="18" />
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
  padding-bottom: 80px;
}

/* Banner */
.course-banner {
  background: linear-gradient(160deg, #3a54d4, var(--primary), var(--primary-light));
  padding-bottom: 28px;
  position: relative;
  overflow: hidden;
}

.banner-bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.b-decor {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.b-decor.d1 {
  width: 180px;
  height: 180px;
  top: -40px;
  right: -30px;
}

.b-decor.d2 {
  width: 100px;
  height: 100px;
  bottom: -20px;
  left: 10%;
}

.banner-content {
  padding: 8px 20px 0;
  position: relative;
}

.banner-title {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  line-height: 1.4;
  margin-bottom: 14px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.banner-tag {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  padding: 4px 14px;
  border-radius: 14px;
}

.banner-tag.free {
  background: rgba(34, 197, 94, 0.35);
  backdrop-filter: blur(4px);
}

.banner-tag.paid {
  background: rgba(239, 68, 68, 0.35);
  backdrop-filter: blur(4px);
}

.banner-cat {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.12);
  padding: 4px 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(4px);
}

.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
}

.banner-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Section Card */
.section-card {
  background: #fff;
  border-radius: var(--radius);
  margin: 10px 12px;
  padding: 18px;
  box-shadow: var(--shadow-xs);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.chapter-total {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 400;
  background: var(--bg-color);
  padding: 3px 10px;
  border-radius: 10px;
}

/* Description */
.desc-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  overflow: hidden;
  max-height: 92px;
  transition: max-height 0.4s ease;
}

.desc-content.expanded {
  max-height: none;
}

.desc-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 12px;
  padding: 8px;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  border-radius: 8px;
  transition: background var(--transition);
}

.desc-toggle:active {
  background: var(--primary-bg);
}

/* Chapter List */
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
  transition: all var(--transition);
}

.chapter-item:last-child {
  border-bottom: none;
}

.chapter-item:active {
  background: var(--bg-color);
  margin: 0 -18px;
  padding: 14px 18px;
  border-radius: 8px;
}

.chapter-index {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--primary-bg);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
}

.chapter-info {
  flex: 1;
  min-width: 0;
}

.chapter-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 5px;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chapter-duration {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}

.chapter-done {
  font-size: 12px;
  color: var(--success);
  display: flex;
  align-items: center;
  gap: 3px;
  font-weight: 500;
}

.chapter-actions {
  flex-shrink: 0;
}

.practice-btn {
  font-size: 12px !important;
  height: 28px !important;
}

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: #fff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-info {
  flex: 1;
}

.bottom-price-label {
  font-size: 12px;
  color: var(--text-muted);
  display: block;
  margin-bottom: 2px;
}

.bottom-price {
  font-size: 24px;
  font-weight: 800;
  color: var(--danger);
}

.learn-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important;
  box-shadow: 0 4px 14px rgba(79, 110, 247, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.buy-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important;
  box-shadow: 0 4px 14px rgba(79, 110, 247, 0.35);
}

::deep(.van-nav-bar) {
  background: transparent !important;
}

::deep(.van-nav-bar .van-icon) {
  color: #fff !important;
}
</style>
