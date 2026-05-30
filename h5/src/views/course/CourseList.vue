<template>
  <div class="course-list-page">
    <van-nav-bar title="全部课程" :border="false" />

    <div class="search-bar">
      <van-search v-model="keyword" shape="round" placeholder="搜索课程" @search="onSearch" />
    </div>

    <!-- Category Bar -->
    <div class="category-bar">
      <div class="category-scroll">
        <div class="cat-chip" :class="{ active: activeCategory === 0 }" @click="selectCategory(0)">全部</div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >{{ cat.name }}</div>
      </div>
      <div v-if="activeSubCategories.length > 0" class="sub-category-bar">
        <div class="sub-cat-chip" :class="{ active: activeSubCategory === 0 }" @click="selectSubCategory(0)">全部</div>
        <div
          v-for="sub in activeSubCategories"
          :key="sub.id"
          class="sub-cat-chip"
          :class="{ active: activeSubCategory === sub.id }"
          @click="selectSubCategory(sub.id)"
        >{{ sub.name }}</div>
      </div>
    </div>

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
const activeSubCategory = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const courseList = ref([])
const categories = ref([])

const activeSubCategories = computed(() => {
  if (activeCategory.value === 0) return []
  const cat = categories.value.find(c => c.id === activeCategory.value)
  return cat?.children || []
})

async function fetchCategories() {
  try {
    const res = await get('/categories/tree')
    if (res.data && res.data.length > 0) categories.value = res.data
  } catch (e) { categories.value = [] }
}

async function fetchCourses(isRefresh) {
  if (isRefresh) { page.value = 1; finished.value = false }
  try {
    const params = { page: page.value, size: 10 }
    const cid = activeSubCategory.value > 0 ? activeSubCategory.value : activeCategory.value
    if (cid > 0) params.categoryId = cid
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
  activeCategory.value = id; activeSubCategory.value = 0; page.value = 1; finished.value = false
  courseList.value = []; loading.value = true; fetchCourses(true)
}
function selectSubCategory(id) {
  activeSubCategory.value = id; page.value = 1; finished.value = false
  courseList.value = []; loading.value = true; fetchCourses(true)
}
function onSearch() {
  page.value = 1; finished.value = false
  courseList.value = []; loading.value = true; fetchCourses(true)
}
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
.category-bar { background: #fff; position: sticky; top: 46px; z-index: 98; }
.category-scroll { display: flex; gap: 4px; padding: 8px 14px; overflow-x: auto; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.cat-chip {
  flex-shrink: 0; padding: 6px 14px; border-radius: 6px; background: var(--bg-color);
  font-size: 13px; color: var(--text-secondary); cursor: pointer; transition: all var(--transition); white-space: nowrap;
}
.cat-chip:active { transform: scale(0.96); }
.cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.sub-category-bar {
  display: flex; gap: 4px; padding: 0 14px 10px; overflow-x: auto; scrollbar-width: none;
  border-top: 1px solid var(--border-light);
}
.sub-category-bar::-webkit-scrollbar { display: none; }
.sub-cat-chip {
  flex-shrink: 0; padding: 4px 12px; border-radius: 14px; background: var(--bg-color);
  font-size: 12px; color: var(--text-secondary); cursor: pointer; white-space: nowrap;
}
.sub-cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 500; }
::deep(.van-list__finished-text) { color: var(--text-muted); font-size: 13px; }
</style>
