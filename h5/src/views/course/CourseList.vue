<template>
  <div class="course-list-page">
    <van-nav-bar title="全部课程" :border="false" />

    <div class="search-bar">
      <van-search v-model="keyword" shape="round" placeholder="搜索课程" @search="onSearch" />
    </div>

    <!-- Category Bar: 多级分类 -->
    <div class="category-bar">
      <div class="category-scroll">
        <div class="cat-chip" :class="{ active: activeL1 === 0 }" @click="selectL1(0)">全部</div>
        <div v-for="cat in categories" :key="cat.id" class="cat-chip" :class="{ active: activeL1 === cat.id }" @click="selectL1(cat.id)">{{ cat.name }}</div>
      </div>
      <div v-if="activeL2List.length > 0" class="sub-category-bar">
        <div class="sub-cat-chip" :class="{ active: activeL2 === 0 }" @click="selectL2(0)">全部</div>
        <div v-for="sub in activeL2List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL2 === sub.id }" @click="selectL2(sub.id)">{{ sub.name }}</div>
      </div>
      <div v-if="activeL3List.length > 0" class="sub-category-bar level3-bar">
        <div class="sub-cat-chip" :class="{ active: activeL3 === 0 }" @click="selectL3(0)">全部</div>
        <div v-for="sub in activeL3List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL3 === sub.id }" @click="selectL3(sub.id)">{{ sub.name }}</div>
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
const activeL1 = ref(0)
const activeL2 = ref(0)
const activeL3 = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const courseList = ref([])
const categories = ref([])

const activeL2List = computed(() => {
  if (activeL1.value === 0) return []
  const cat = categories.value.find(c => c.id === activeL1.value)
  return cat?.children || []
})

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

async function fetchCourses(isRefresh) {
  if (isRefresh) { page.value = 1; finished.value = false }
  try {
    const params = { page: page.value, size: 10 }
    const cid = activeL3.value > 0 ? activeL3.value : (activeL2.value > 0 ? activeL2.value : activeL1.value)
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

function selectL1(id) { activeL1.value = id; activeL2.value = 0; activeL3.value = 0; resetAndLoad() }
function selectL2(id) { activeL2.value = id; activeL3.value = 0; resetAndLoad() }
function selectL3(id) { activeL3.value = id; resetAndLoad() }
function resetAndLoad() { page.value = 1; finished.value = false; courseList.value = []; loading.value = true; fetchCourses(true) }
function onSearch() { page.value = 1; finished.value = false; courseList.value = []; loading.value = true; fetchCourses(true) }
function onRefresh() { refreshing.value = true; page.value = 1; finished.value = false; loading.value = false; fetchCourses(true).finally(() => { refreshing.value = false }) }

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
.level3-bar { background: #FAFBFC; }
.sub-category-bar::-webkit-scrollbar { display: none; }
.sub-cat-chip {
  flex-shrink: 0; padding: 4px 12px; border-radius: 14px; background: var(--bg-color);
  font-size: 12px; color: var(--text-secondary); cursor: pointer; white-space: nowrap;
}
.sub-cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 500; }
::deep(.van-list__finished-text) { color: var(--text-muted); font-size: 13px; }
</style>
