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

    <!-- Banner Carousel -->
    <div class="banner-section" v-if="banners.length > 0">
      <van-swipe :autoplay="3000" indicator-color="#0052D9" lazy-render>
        <van-swipe-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" @click="goBannerLink(banner)">
            <img :src="banner.imageUrl" :alt="banner.title" class="banner-image" />
            <div class="banner-title" v-if="banner.title">{{ banner.title }}</div>
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- News List -->
    <div class="news-section">
      <div class="news-header">
        <span class="news-header-title">最新资讯</span>
        <span class="news-header-more" @click="$router.push('/news')">更多 <van-icon name="arrow" /></span>
      </div>
      <!-- News Module Tabs -->
      <div class="news-tabs" v-if="newsModules.length > 0">
        <div 
          class="news-tab" 
          :class="{ active: activeNewsModule === 0 }" 
          @click="switchNewsTab(0)"
        >全部</div>
        <div 
          v-for="mod in newsModules" 
          :key="mod.id" 
          class="news-tab" 
          :class="{ active: activeNewsModule === mod.id }" 
          @click="switchNewsTab(mod.id)"
        >{{ mod.name }}</div>
      </div>
      <div class="news-list" v-if="newsList.length > 0">
        <div
          v-for="item in newsList"
          :key="item.id"
          class="news-item"
          @click="goNewsDetail(item.id)"
        >
          <img v-if="item.cover" :src="item.cover" class="news-cover" />
          <div class="news-info" :class="{ 'has-cover': item.cover }">
            <div class="news-title">{{ item.title }}</div>
            <div class="news-meta">
              <span v-if="item.source">{{ item.source }}</span>
              <span class="news-views" v-if="item.viewCount">{{ item.viewCount }}阅读</span>
              <span class="news-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="news-empty" v-else>
        <span>暂无资讯</span>
      </div>
    </div>

    <!-- Category Bar: 多级分类 -->
    <div class="category-bar">
      <!-- 一级 -->
      <div class="category-scroll">
        <div class="cat-chip" :class="{ active: activeL1 === 0 }" @click="selectL1(0)">全部</div>
        <div v-for="cat in categories" :key="cat.id" class="cat-chip" :class="{ active: activeL1 === cat.id }" @click="selectL1(cat.id)">{{ cat.name }}</div>
      </div>
      <!-- 二级 -->
      <div v-if="activeL2List.length > 0" class="sub-category-bar">
        <div class="sub-cat-chip" :class="{ active: activeL2 === 0 }" @click="selectL2(0)">全部</div>
        <div v-for="sub in activeL2List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL2 === sub.id }" @click="selectL2(sub.id)">
          {{ sub.name }}
          <span v-if="sub.isFree === 1" class="free-badge">免费</span>
          <span v-else-if="sub.price > 0" class="price-badge">¥{{ sub.price }}</span>
        </div>
      </div>
      <!-- 三级 -->
      <div v-if="activeL3List.length > 0" class="sub-category-bar level3-bar">
        <div class="sub-cat-chip" :class="{ active: activeL3 === 0 }" @click="selectL3(0)">全部</div>
        <div v-for="sub in activeL3List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL3 === sub.id }" @click="selectL3(sub.id)">
          {{ sub.name }}
          <span v-if="sub.isFree === 1" class="free-badge">免费</span>
          <span v-else-if="sub.price > 0" class="price-badge">¥{{ sub.price }}</span>
        </div>
      </div>
    </div>

    <!-- Category Popup: 展示多级分类 -->
    <van-popup v-model:show="showAllCategories" position="bottom" round :style="{ maxHeight: '65vh' }">
      <div class="category-popup">
        <h3 class="popup-title">全部分类</h3>
        <div v-for="cat in categories" :key="cat.id" class="popup-cat-group">
          <div class="popup-cat-parent" :class="{ active: activeL1 === cat.id }" @click="selectL1FromPopup(cat.id)">
            <span>{{ cat.name }}</span>
            <van-icon v-if="activeL1 === cat.id" name="success" color="var(--primary)" size="16" />
          </div>
          <div v-if="cat.children && cat.children.length" class="popup-cat-children">
            <div v-for="sub in cat.children" :key="sub.id" class="popup-sub-item-wrap">
              <div class="popup-sub-item" :class="{ active: activeL2 === sub.id }" @click="selectL2FromPopup(cat.id, sub.id)">
                <span>{{ sub.name }}</span>
                <span class="sub-price">{{ sub.isFree === 1 ? '免费' : '¥' + (sub.price || 0) }}</span>
              </div>
              <div v-if="sub.children && sub.children.length" class="popup-l3-children">
                <div v-for="l3 in sub.children" :key="l3.id" class="popup-l3-item" :class="{ active: activeL3 === l3.id }" @click.stop="selectL3FromPopup(cat.id, sub.id, l3.id)">
                  {{ l3.name }}
                  <span class="sub-price">{{ l3.isFree === 1 ? '免费' : '¥' + (l3.price || 0) }}</span>
                </div>
              </div>
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
const activeL1 = ref(0)      // 一级分类ID
const activeL2 = ref(0)      // 二级分类ID
const activeL3 = ref(0)      // 三级分类ID
const refreshing = ref(false)
const loading = ref(false)
const categories = ref([])
const courseList = ref([])
const banners = ref([])
const newsList = ref([])
const newsModules = ref([])
const activeNewsModule = ref(0)
const showAllCategories = ref(false)

// 当前选中的一级分类下的二级列表
const activeL2List = computed(() => {
  if (activeL1.value === 0) return []
  const cat = categories.value.find(c => c.id === activeL1.value)
  return cat?.children || []
})

// 当前选中的二级分类下的三级列表
const activeL3List = computed(() => {
  if (activeL2.value === 0) return []
  for (const cat of categories.value) {
    const l2 = cat.children?.find(c => c.id === activeL2.value)
    if (l2) return l2.children || []
  }
  return []
})

async function fetchCategories() {
  try {
    const res = await get('/categories/tree')
    if (res.data && res.data.length > 0) categories.value = res.data
  } catch (e) { categories.value = [] }
}

async function fetchCourses() {
  loading.value = true
  try {
    // 首页不分页，一次查全量课程
    const params = { size: 100 }
    // 优先用最深级分类筛选
    const cid = activeL3.value > 0 ? activeL3.value : (activeL2.value > 0 ? activeL2.value : activeL1.value)
    if (cid > 0) params.categoryId = cid
    if (keyword.value) params.keyword = keyword.value
    const res = await get('/courses', params)
    if (res.data) courseList.value = res.data.records || res.data || []
  } catch (e) { courseList.value = [] }
  loading.value = false
}

function selectL1(id) {
  activeL1.value = id; activeL2.value = 0; activeL3.value = 0
  fetchCourses()
}
function selectL2(id) {
  activeL2.value = id; activeL3.value = 0
  fetchCourses()
}
function selectL3(id) {
  activeL3.value = id
  fetchCourses()
}

function selectL1FromPopup(id) {
  activeL1.value = id; activeL2.value = 0; activeL3.value = 0
  showAllCategories.value = false
  fetchCourses()
}
function selectL2FromPopup(l1Id, l2Id) {
  activeL1.value = l1Id; activeL2.value = l2Id; activeL3.value = 0
  showAllCategories.value = false
  fetchCourses()
}
function selectL3FromPopup(l1Id, l2Id, l3Id) {
  activeL1.value = l1Id; activeL2.value = l2Id; activeL3.value = l3Id
  showAllCategories.value = false
  fetchCourses()
}

function onRefresh() {
  refreshing.value = true
  fetchCourses().finally(() => { refreshing.value = false })
}

function goDetail(id) { router.push('/course/' + id) }

async function fetchBanners() {
  try {
    const res = await get('/banners')
    banners.value = res.data || []
  } catch (e) { banners.value = [] }
}

async function fetchNewsModules() {
  try {
    const res = await get('/config/news-modules')
    if (res.data) newsModules.value = res.data
  } catch (e) { newsModules.value = [] }
}

async function fetchNews({ moduleId } = {}) {
  try {
    const params = {}
    if (moduleId && moduleId > 0) params.moduleId = moduleId
    const res = await get('/news', params)
    newsList.value = (res.data || []).slice(0, 4)
  } catch (e) { newsList.value = [] }
}

function switchNewsTab(moduleId) {
  activeNewsModule.value = moduleId
  fetchNews({ moduleId })
}

function goBannerLink(banner) {
  if (!banner.linkUrl) return
  if (banner.linkUrl.startsWith('http')) {
    window.location.href = banner.linkUrl
  } else if (banner.linkUrl.startsWith('/')) {
    router.push(banner.linkUrl)
  }
}

function goNewsDetail(id) {
  router.push('/news/' + id)
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

onMounted(() => {
  fetchCategories()
  fetchCourses()
  fetchBanners()
  fetchNewsModules()
  fetchNews()
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

/* Banner Section */
.banner-section {
  margin: 0 14px 10px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.banner-item {
  position: relative;
  width: 100%;
  height: 200px;
  cursor: pointer;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px 14px;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

/* News Section */
.news-section {
  background: #fff;
  margin: 0 14px 10px;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.news-header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
  position: relative;
  padding-left: 10px;
}

.news-header-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: var(--primary);
  border-radius: 2px;
}

.news-header-more {
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  display: flex;
  gap: 10px;
  cursor: pointer;
  align-items: flex-start;
}

.news-cover {
  width: 120px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.news-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 60px;
}

.news-info.has-cover {
  min-height: 80px;
}

.news-title {
  font-size: 16px;
  font-weight: 500;
  color: #1D2129;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.news-views {
  color: var(--text-placeholder);
}

.news-time {
  color: var(--text-placeholder);
}

/* News Tabs */
.news-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 0 0 10px 0;
  scrollbar-width: none;
}
.news-tabs::-webkit-scrollbar { display: none; }
.news-tab {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 14px;
  background: var(--bg-color);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  white-space: nowrap;
}
.news-tab.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 500;
}
.news-empty {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: var(--text-muted);
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
.level3-bar {
  background: #FAFBFC;
}
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
.popup-sub-item-wrap {
  flex: 1 1 100%;
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
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 4px;
}
.popup-sub-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 500;
}
.popup-l3-children {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 2px 0 6px 18px;
}
.popup-l3-item {
  padding: 5px 10px;
  border-radius: 12px;
  background: #f0f2f5;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.popup-l3-item.active {
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
