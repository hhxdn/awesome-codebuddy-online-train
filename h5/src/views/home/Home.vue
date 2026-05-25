<template>
  <div class="home-page page-fade-in">
    <!-- Header -->
    <div class="home-header">
      <div class="header-top">
        <div class="header-brand">
          <span class="brand-icon">
            <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
              <rect width="32" height="32" rx="8" fill="#0052D9"/>
              <path d="M10 14L16 9L22 14V22C22 22.6 21.6 23 21 23H11C10.4 23 10 22.6 10 22V14Z" stroke="white" stroke-width="1.5" fill="none"/>
              <path d="M14 23V17H18V23" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="brand-name">在线学习</span>
        </div>
      </div>
      <div class="header-search">
        <div class="search-box" @click="$router.push('/courses')">
          <van-icon name="search" size="16" color="#86909C" />
          <span class="search-placeholder">搜索课程、知识点</span>
        </div>
      </div>
    </div>

    <!-- Category Chips -->
    <div class="category-bar">
      <div class="category-scroll">
        <div
          v-for="cat in visibleCategories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </div>
        <div
          v-if="hasMoreCategories"
          class="cat-chip cat-more"
          @click="showAllCategories = true"
        >
          更多
          <van-icon name="arrow-down" size="12" />
        </div>
      </div>
    </div>

    <!-- Category Popup -->
    <van-popup
      v-model:show="showAllCategories"
      position="bottom"
      round
      :style="{ maxHeight: '50vh' }"
    >
      <div class="category-popup">
        <h3 class="popup-title">全部分类</h3>
        <div class="popup-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="popup-cat-item"
            :class="{ active: activeCategory === cat.id }"
            @click="selectCategoryFromPopup(cat.id)"
          >
            <span>{{ cat.name }}</span>
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

      <div v-if="!refreshing && !loading && courseList.length === 0" class="empty-wrap">
        <EmptyState description="暂无课程" subText="换个分类试试" />
      </div>
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
const categories = ref([{ id: 0, name: '全部' }])
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
        { id: 0, name: '全部' },
        ...res.data.map(c => ({ id: c.id, name: c.name }))
      ]
    }
  } catch (e) {
    categories.value = [
      { id: 0, name: '全部' },
      { id: 1, name: 'Java开发' },
      { id: 2, name: '前端开发' },
      { id: 3, name: 'Python' },
      { id: 4, name: 'AI与大模型' },
      { id: 5, name: '数据库' },
      { id: 6, name: '云计算' }
    ]
  }
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

/* Header */
.home-header {
  background: linear-gradient(180deg, #0052D9 0%, #0052D9 60%, #366EF4 100%);
  padding-top: 12px; padding-left: 16px; padding-right: 16px; padding-bottom: 20px;
  position: relative;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  display: flex;
  align-items: center;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.header-search {
  padding: 0 4px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
}

.search-placeholder {
  font-size: 14px;
  color: var(--text-placeholder);
}

/* Category Bar */
.category-bar {
  background: #fff;
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 99;
}

.category-scroll {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.category-scroll::-webkit-scrollbar {
  display: none;
}

.cat-chip {
  flex-shrink: 0;
  padding: 7px 15px;
  border-radius: 6px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
  user-select: none;
  font-weight: 400;
}
.cat-chip:active {
  transform: scale(0.96);
}
.cat-chip.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
}
.cat-more {
  gap: 3px;
  display: flex;
  align-items: center;
  color: var(--text-muted);
}

/* Category Popup */
.category-popup {
  padding-top: 16px; padding-left: 16px; padding-right: 16px; padding-bottom: 30px;
}
.popup-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 16px;
  text-align: center;
}
.popup-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.popup-cat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 16px;
  border-radius: 8px;
  background: var(--bg-color);
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: all var(--transition);
  flex: 0 0 calc(50% - 5px);
  box-sizing: border-box;
}
.popup-cat-item:active {
  transform: scale(0.97);
}
.popup-cat-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
}

/* Pull Refresh */
.pull-refresh-wrap {
  min-height: calc(100vh - 140px);
}
</style>
