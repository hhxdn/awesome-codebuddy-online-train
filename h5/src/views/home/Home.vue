<template>
  <div class="home-page">
    <van-search
      v-model="keyword"
      shape="round"
      placeholder="搜索课程"
      @search="onSearch"
    />

    <van-tabs
      v-model:active="activeCategory"
      :before-change="onTabChange"
      sticky
      offset-top="0"
    >
      <van-tab v-for="cat in categories" :key="cat.id || 0" :title="cat.name || '全部'">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <div class="course-grid">
            <CourseCard
              v-for="course in courseList"
              :key="course.id"
              :course="course"
              @click="goDetail(course.id)"
            />
          </div>
          <EmptyState v-if="!refreshing && !loading && courseList.length === 0" />
          <van-loading v-if="loading" class="loading-center" />
        </van-pull-refresh>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
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

async function fetchCategories() {
  try {
    const res = await get('/categories')
    if (res.data) {
      categories.value = [{ id: 0, name: '全部' }, ...res.data]
    }
  } catch (e) {
    // Use mock categories
    categories.value = [
      { id: 0, name: '全部' },
      { id: 1, name: 'Java开发' },
      { id: 2, name: '前端开发' },
      { id: 3, name: 'Python' },
      { id: 4, name: '人工智能' }
    ]
  }
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {}
    if (activeCategory.value > 0) {
      params.categoryId = activeCategory.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    const res = await get('/courses', params)
    if (res.data) {
      courseList.value = res.data.records || res.data || []
    }
  } catch (e) {
    // Mock data
    courseList.value = [
      { id: 1, title: 'Spring Boot 实战教程', cover: '', categoryName: 'Java开发', price: 0, studentCount: 1234 },
      { id: 2, title: 'Vue3 全家桶从入门到精通', cover: '', categoryName: '前端开发', price: 99, studentCount: 2100 },
      { id: 3, title: 'Python 数据分析实战', cover: '', categoryName: 'Python', price: 0, studentCount: 856 },
      { id: 4, title: '深度学习入门', cover: '', categoryName: '人工智能', price: 199, studentCount: 567 }
    ]
  }
  loading.value = false
}

function onSearch() {
  fetchCourses()
}

function onTabChange() {
  fetchCourses()
  return true
}

function onRefresh() {
  refreshing.value = true
  fetchCourses().finally(() => {
    refreshing.value = false
  })
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

.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
