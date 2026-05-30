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

    <!-- Category Bar: 一级分类横向滚动 -->
    <div class="category-bar">
      <div class="category-scroll">
        <div
          class="cat-chip"
          :class="{ active: activeCategory === 0 }"
          @click="selectCategory(0)"
        >全部</div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </div>
      </div>
      <!-- 二级分类（点击一级后展开） -->
      <div v-if="activeSubCategories.length > 0" class="sub-category-bar">
        <div
          class="sub-cat-chip"
          :class="{ active: activeSubCategory === 0 }"
          @click="selectSubCategory(0)"
        >全部</div>
        <div
          v-for="sub in activeSubCategories"
          :key="sub.id"
          class="sub-cat-chip"
          :class="{ active: activeSubCategory === sub.id }"
          @click="selectSubCategory(sub.id)"
        >
          {{ sub.name }}
          <span v-if="sub.isFree === 1" class="free-badge">免费</span>
          <span v-else-if="sub.price > 0" class="price-badge">¥{{ sub.price }}</span>
        </div>
      </div>
    </div>

    <!-- Category Popup: 展示所有一级和二级 -->
    <van-popup
      v-model:show="showAllCategories"
      position="bottom"
      round
      :style="{ maxHeight: '60vh' }"
    >
      <div class="category-popup">
        <h3 class="popup-title">全部分类</h3>
        <div v-for="cat in categories" :key="cat.id" class="popup-cat-group">
          <div class="popup-cat-parent" :class="{ active: activeCategory === cat.id }" @click="selectCategoryFromPopup(cat.id)">
            <span>{{ cat.name }}</span>
            <van-icon v-if="activeCategory === cat.id" name="success" color="var(--primary)" size="16" />
          </div>
          <div v-if="cat.children && cat.children.length" class="popup-cat-children">
            <div
              v-for="sub in cat.children"
              :key="sub.id"
              class="popup-sub-item"
              :class="{ active: activeSubCategory === sub.id }"
              @click="selectSubFromPopup(cat.id, sub.id)"
            >
              <span>{{ sub.name }}</span>
              <span class="sub-price">{{ sub.isFree === 1 ? '免费' : '¥' + (sub.price || 0) }}</span>
            </div>
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
const activeSubCategory = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const categories = ref([])
const courseList = ref([])
const showAllCategories = ref(false)

const activeSubCategories = computed(() => {
  if (activeCategory.value === 0) return []
  const cat = categories.value.find(c => c.id === activeCategory.value)
  return cat?.children || []
})

async function fetchCategories() {
  try {
    const res = await get('/categories/tree')
    if (res.data && res.data.length > 0) {
      categories.value = res.data
    }
  } catch (e) {
    categories.value = []
  }
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {}
    // 优先用二级分类筛选
    const cid = activeSubCategory.value > 0 ? activeSubCategory.value : activeCategory.value
    if (cid > 0) params.categoryId = cid
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
  activeSubCategory.value = 0
  fetchCourses()
}

function selectSubCategory(id) {
  activeSubCategory.value = id
  fetchCourses()
}

function selectCategoryFromPopup(id) {
  activeCategory.value = id
  activeSubCategory.value = 0
  showAllCategories.value = false
  fetchCourses()
}

function selectSubFromPopup(parentId, subId) {
  activeCategory.value = parentId
  activeSubCategory.value = subId
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

/* Sub Category Bar */
.sub-category-bar {
  display: flex;
  gap: 4px;
  padding: 6px 14px 10px;
  overflow-x: auto;
  scrollbar-width: none;
  background: #fff;
  border-top: 1px solid var(--border-light);
}
.sub-category-bar::-webkit-scrollbar { display: none; }
.sub-cat-chip {
  flex-shrink: 0;
  padding: 5px 12px;
  border-radius: 14px;
  background: var(--bg-color);
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}
.sub-cat-chip.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 500;
}
.free-badge {
  font-size: 10px;
  background: #00A870;
  color: #fff;
  padding: 0 5px;
  border-radius: 3px;
}
.price-badge {
  font-size: 10px;
  color: #E34D59;
}

/* Category Popup */
.category-popup {
  padding: 16px;
  padding-bottom: 30px;
}
.popup-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 16px;
  text-align: center;
}
.popup-cat-group {
  margin-bottom: 14px;
}
.popup-cat-parent {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--primary-bg);
  font-size: 15px;
  font-weight: 600;
  color: var(--primary);
  cursor: pointer;
  margin-bottom: 6px;
}
.popup-cat-parent.active {
  background: var(--primary);
  color: #fff;
}
.popup-cat-children {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding-left: 8px;
}
.popup-sub-item {
  padding: 8px 14px;
  border-radius: 6px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-color);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}
.popup-sub-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 500;
}
.sub-price {
  font-size: 11px;
  color: var(--text-muted);
}
.popup-sub-item.active .sub-price {
  color: var(--primary);
}

/* Pull Refresh */
.pull-refresh-wrap {
  min-height: calc(100vh - 140px);
}
</style>
