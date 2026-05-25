<template>
  <div class="course-list-page">
    <van-nav-bar title="全部课程" :border="false" />

    <div class="search-wrapper">
      <van-search v-model="keyword" shape="round" placeholder="搜索你感兴趣的课程" @search="onSearch" />
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

    <!-- Category Popup -->
    <van-popup
      v-model:show="showAllCategories"
      position="bottom"
      round
      :style="{ maxHeight: '60vh' }"
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
            <span class="popup-cat-icon" v-if="cat.icon">{{ cat.icon }}</span>
            <span class="popup-cat-name">{{ cat.name }}</span>
            <van-icon v-if="activeCategory === cat.id" name="success" color="var(--primary)" size="16" />
          </div>
        </div>
      </div>
    </van-popup>

    <!-- Course List -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="— 没有更多了 —"
        @load="fetchCourses"
      >
        <div class="course-list-wrapper">
          <CourseCard
            v-for="course in courseList"
            :key="course.id"
            :course="course"
          />
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && !refreshing && courseList.length === 0" description="暂无课程" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get } from '../../api'
import CourseCard from '../../components/CourseCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const keyword = ref('')
const activeCategory = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const courseList = ref([])
const categories = ref([{ id: 0, name: '全部', icon: '📚' }])
const showAllCategories = ref(false)

const MAX_VISIBLE = 5
const hasMoreCategories = computed(() => categories.value.length > MAX_VISIBLE)
const visibleCategories = computed(() => categories.value.slice(0, MAX_VISIBLE))

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

async function fetchCourses(isRefresh) {
  if (isRefresh) {
    page.value = 1
    finished.value = false
  }
  try {
    const params = { page: page.value, pageSize: 10 }
    if (activeCategory.value > 0) params.categoryId = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await get('/courses', params)
    const data = res.data?.records || res.data || []
    if (page.value === 1) {
      courseList.value = data
    } else {
      courseList.value.push(...data)
    }
    if (!data.length || data.length < 10) finished.value = true
    page.value++
  } catch (e) {
    finished.value = true
  }
  loading.value = false
}

function selectCategory(id) {
  activeCategory.value = id
  page.value = 1
  finished.value = false
  courseList.value = []
  loading.value = true
  fetchCourses(true)
}

function selectCategoryFromPopup(id) {
  showAllCategories.value = false
  selectCategory(id)
}

function onSearch() {
  page.value = 1
  finished.value = false
  courseList.value = []
  loading.value = true
  fetchCourses(true)
}

function onRefresh() {
  refreshing.value = true
  page.value = 1
  finished.value = false
  loading.value = false
  fetchCourses(true).finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchCategories()
  loading.value = true
})
</script>

<style scoped>
.course-list-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.search-wrapper {
  background: #fff;
  padding: 0 4px;
}

/* Category Bar */
.category-bar {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 46px;
  z-index: 98;
}

.category-scroll {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
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
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}
.cat-chip:active {
  transform: scale(0.95);
}
.cat-chip.active {
  background: var(--primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.3);
}
.cat-more {
  background: var(--bg-color);
  gap: 3px;
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
  padding: 20px 16px 30px;
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
  gap: 6px;
  padding: 10px 16px;
  border-radius: 10px;
  background: var(--bg-color);
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 0 0 calc(50% - 5px);
  box-sizing: border-box;
}
.popup-cat-item:active {
  transform: scale(0.97);
}
.popup-cat-item.active {
  background: #e8eeff;
  color: var(--primary);
  font-weight: 600;
  border: 1px solid var(--primary);
}
.popup-cat-icon {
  font-size: 18px;
}
.popup-cat-name {
  flex: 1;
}

.course-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
}

:deep(.van-list__finished-text) {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
