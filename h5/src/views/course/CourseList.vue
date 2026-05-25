<template>
  <div class="course-list-page">
    <van-nav-bar title="全部课程" :border="false" />

    <div class="search-bar">
      <van-search v-model="keyword" shape="round" placeholder="搜索课程" @search="onSearch" />
    </div>

    <div class="category-bar">
      <div class="category-scroll">
        <div
          v-for="cat in visibleCategories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >{{ cat.name }}</div>
        <div v-if="hasMoreCategories" class="cat-chip cat-more" @click="showAllCategories = true">
          更多 <van-icon name="arrow-down" size="12" />
        </div>
      </div>
    </div>

    <van-popup v-model:show="showAllCategories" position="bottom" round :style="{ maxHeight: '50vh' }">
      <div class="category-popup">
        <h3 class="popup-title">全部分类</h3>
        <div class="popup-grid">
          <div
            v-for="cat in categories" :key="cat.id"
            class="popup-cat-item" :class="{ active: activeCategory === cat.id }"
            @click="selectCategoryFromPopup(cat.id)"
          >
            <span>{{ cat.name }}</span>
            <van-icon v-if="activeCategory === cat.id" name="success" color="var(--primary)" size="16" />
          </div>
        </div>
      </div>
    </van-popup>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多了 —" @load="fetchCourses">
        <div class="course-grid">
          <CourseCard v-for="course in courseList" :key="course.id" :course="course" />
        </div>
      </van-list>
    </van-pull-refresh>

    <div v-if="!loading && !refreshing && courseList.length === 0" class="empty-wrap">
      <EmptyState description="暂无课程" />
    </div>
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
const categories = ref([{ id: 0, name: '全部' }])
const showAllCategories = ref(false)

const MAX_VISIBLE = 5
const hasMoreCategories = computed(() => categories.value.length > MAX_VISIBLE)
const visibleCategories = computed(() => categories.value.slice(0, MAX_VISIBLE))

async function fetchCategories() {
  try {
    const res = await get('/categories')
    if (res.data && res.data.length > 0) {
      categories.value = [{ id: 0, name: '全部' }, ...res.data.map(c => ({ id: c.id, name: c.name }))]
    }
  } catch (e) {
    categories.value = [
      { id: 0, name: '全部' }, { id: 1, name: 'Java开发' }, { id: 2, name: '前端开发' },
      { id: 3, name: 'Python' }, { id: 4, name: 'AI与大模型' }, { id: 5, name: '数据库' }, { id: 6, name: '云计算' }
    ]
  }
}

async function fetchCourses(isRefresh) {
  if (isRefresh) { page.value = 1; finished.value = false }
  try {
    const params = { page: page.value, pageSize: 10 }
    if (activeCategory.value > 0) params.categoryId = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await get('/courses', params)
    const data = res.data?.records || res.data || []
    if (page.value === 1) courseList.value = data
    else courseList.value.push(...data)
    if (!data.length || data.length < 10) finished.value = true
    page.value++
  } catch (e) { finished.value = true }
  loading.value = false
}

function selectCategory(id) {
  activeCategory.value = id; page.value = 1; finished.value = false
  courseList.value = []; loading.value = true; fetchCourses(true)
}
function selectCategoryFromPopup(id) { showAllCategories.value = false; selectCategory(id) }
function onSearch() { selectCategory(activeCategory.value) }
function onRefresh() {
  refreshing.value = true
  page.value = 1; finished.value = false; loading.value = false
  fetchCourses(true).finally(() => { refreshing.value = false })
}

onMounted(() => { fetchCategories(); loading.value = true })
</script>

<style scoped>
.course-list-page { background: var(--bg-color); min-height: 100vh; }
.search-bar { background: #fff; }
.category-bar { background: #fff; border-bottom: 1px solid var(--border-light); position: sticky; top: 46px; z-index: 98; }
.category-scroll { display: flex; gap: 4px; padding: 8px 14px; overflow-x: auto; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.cat-chip {
  flex-shrink: 0; padding: 6px 14px; border-radius: 6px; background: var(--bg-color);
  font-size: 13px; color: var(--text-secondary); cursor: pointer; transition: all var(--transition); white-space: nowrap;
}
.cat-chip:active { transform: scale(0.96); }
.cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.cat-more { display: flex; align-items: center; gap: 3px; color: var(--text-muted); }
.category-popup { padding: 16px 16px 30px; }
.popup-title { font-size: 16px; font-weight: 600; text-align: center; margin-bottom: 16px; }
.popup-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.popup-cat-item {
  display: flex; align-items: center; justify-content: space-between; padding: 11px 16px;
  border-radius: 8px; background: var(--bg-color); font-size: 14px; cursor: pointer;
  flex: 0 0 calc(50% - 5px); box-sizing: border-box;
}
.popup-cat-item:active { transform: scale(0.97); }
.popup-cat-item.active { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
:deep(.van-list__finished-text) { color: var(--text-muted); font-size: 13px; }
</style>
