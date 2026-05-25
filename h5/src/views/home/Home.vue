<template>
  <div class="home-page page-fade-in">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-bg-decor">
        <div class="decor-circle c1"></div>
        <div class="decor-circle c2"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">在线学习平台</h1>
        <p class="hero-subtitle">随时随地，提升自我</p>
      </div>
      <div class="search-bar">
        <van-search
          v-model="keyword"
          shape="round"
          placeholder="搜索你感兴趣的课程"
          @search="onSearch"
          background="transparent"
        >
          <template #left-icon>
            <van-icon name="search" size="18" color="#9ca3af" />
          </template>
        </van-search>
      </div>
    </div>

    <!-- Category Chips -->
    <div class="category-bar">
      <div class="category-scroll" ref="scrollRef">
        <div
          v-for="cat in visibleCategories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <span class="cat-icon" v-if="cat.icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
        <div
          v-if="hasMoreCategories"
          class="cat-chip cat-more"
          @click="showAllCategories = true"
        >
          <van-icon name="ellipsis" size="16" />
          <span class="cat-name">更多</span>
        </div>
      </div>
    </div>

    <!-- Category Popup (全部分类) -->
    <van-popup
      v-model:show="showAllCategories"
      position="bottom"
      round
      :style="{ maxHeight: '60vh' }"
    >
      <div class="category-popup">
        <div class="popup-handle"></div>
        <h3 class="popup-title">全部分类</h3>
        <div class="popup-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="popup-cat-item"
            :class="{ active: activeCategory === cat.id }"
            @click="selectCategoryFromPopup(cat.id)"
          >
            <span class="popup-cat-icon" v-if="cat.icon">{{ cat.icon }}</span>
            <span class="popup-cat-name">{{ cat.name }}</span>
            <van-icon v-if="activeCategory === cat.id" name="success" color="var(--primary)" size="16" />
          </div>
        </div>
      </div>
    </van-popup>

    <!-- Course Grid -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="pull-refresh-wrap">
      <!-- Skeleton Loading -->
      <div class="course-grid" v-if="loading">
        <div v-for="i in 4" :key="'s'+i" class="skeleton-card">
          <div class="skeleton-img"></div>
          <div class="skeleton-text">
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>
      </div>

      <!-- Course Grid -->
      <div class="course-grid" v-else-if="courseList.length > 0">
        <CourseCard
          v-for="course in courseList"
          :key="course.id"
          :course="course"
          @click="goDetail(course.id)"
        />
      </div>

      <!-- Empty -->
      <EmptyState v-if="!refreshing && !loading && courseList.length === 0" />
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../../api'
import CourseCard from '../../components/CourseCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const keyword = ref('')
const activeCategory = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const categories = ref([{ id: 0, name: '全部', icon: '📚' }])
const courseList = ref([])
const showAllCategories = ref(false)

const MAX_VISIBLE = 5
const hasMoreCategories = computed(() => categories.value.length > MAX_VISIBLE)
const visibleCategories = computed(() => categories.value.slice(0, MAX_VISIBLE))

async function fetchCategories() {
  try {
    const res = await get('/categories')
    if (res.data && res.data.length > 0) {
      categories.value = [
        { id: 0, name: '全部', icon: '📚' },
        ...res.data.map(c => ({ ...c, icon: getCategoryIcon(c.name) }))
      ]
    }
  } catch (e) {
    categories.value = [
      { id: 0, name: '全部', icon: '📚' },
      { id: 1, name: 'Java开发', icon: '☕' },
      { id: 2, name: '前端开发', icon: '🎨' },
      { id: 3, name: 'Python', icon: '🐍' },
      { id: 4, name: 'AI与大模型', icon: '🤖' },
      { id: 5, name: '数据库', icon: '🗄️' },
      { id: 6, name: '云计算', icon: '☁️' }
    ]
  }
}

function getCategoryIcon(name) {
  const icons = {
    'Java': '☕', '前端': '🎨', 'Python': '🐍', 'AI': '🤖',
    '数据库': '🗄️', '云计算': '☁️', 'DevOps': '🔧', '大模型': '🤖',
    '测试': '🧪', '安全': '🔒', '架构': '🏗️', '大数据': '📊'
  }
  for (const [key, icon] of Object.entries(icons)) {
    if (name.includes(key)) return icon
  }
  return '📖'
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {}
    if (activeCategory.value > 0) params.categoryId = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await get('/courses', params)
    if (res.data) {
      courseList.value = res.data.records || res.data || []
    }
  } catch (e) {
    courseList.value = []
  }
  loading.value = false
}

function selectCategory(id) {
  activeCategory.value = id
  fetchCourses()
}

function selectCategoryFromPopup(id) {
  activeCategory.value = id
  showAllCategories.value = false
  fetchCourses()
}

function onSearch() {
  fetchCourses()
}

function onRefresh() {
  refreshing.value = true
  fetchCourses().finally(() => { refreshing.value = false })
}

function goDetail(id) {
  router.push('/course/' + id)
}

onMounted(() => {
  fetchCategories()
  fetchCourses()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-color);
}

/* Hero Banner */
.hero-banner {
  background: linear-gradient(160deg, #3a54d4 0%, var(--primary) 30%, var(--primary-light) 70%, #a5b4fc 100%);
  padding: 0 0 16px;
  position: relative;
  overflow: hidden;
}

.hero-bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.decor-circle.c1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
}

.decor-circle.c2 {
  width: 120px;
  height: 120px;
  bottom: -30px;
  left: -20px;
}

.hero-content {
  padding: 32px 20px 8px;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1px;
  margin-bottom: 4px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

/* Search Bar */
.search-bar {
  position: relative;
  z-index: 1;
  padding: 0 12px;
}

.search-bar :deep(.van-search) {
  padding: 8px 0;
}

.search-bar :deep(.van-search__content) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.search-bar :deep(.van-field__control) {
  font-size: 14px;
}

/* Category Bar */
.category-bar {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 99;
}

.category-scroll {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.category-scroll::-webkit-scrollbar {
  display: none;
}

.cat-chip {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 22px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
  user-select: none;
  border: 1.5px solid transparent;
}
.cat-chip:active {
  transform: scale(0.95);
}
.cat-chip.active {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(79, 110, 247, 0.35);
  border-color: transparent;
}
.cat-more {
  background: var(--bg-color);
  gap: 4px;
}
.cat-icon {
  font-size: 15px;
  line-height: 1;
}
.cat-name {
  line-height: 1;
}

/* Category Popup */
.category-popup {
  padding: 12px 16px 30px;
}
.popup-handle {
  width: 36px;
  height: 4px;
  background: #ddd;
  border-radius: 2px;
  margin: 0 auto 16px;
}
.popup-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 18px;
  text-align: center;
}
.popup-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.popup-cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: var(--radius);
  background: var(--bg-color);
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: all var(--transition);
  flex: 0 0 calc(50% - 6px);
  box-sizing: border-box;
  border: 1.5px solid transparent;
}
.popup-cat-item:active {
  transform: scale(0.97);
}
.popup-cat-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.12);
}
.popup-cat-icon {
  font-size: 20px;
}
.popup-cat-name {
  flex: 1;
}

/* Pull Refresh */
.pull-refresh-wrap {
  min-height: calc(100vh - 180px);
}
</style>
