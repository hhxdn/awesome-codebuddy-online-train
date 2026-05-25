<template>
  <div class="course-list-page">
    <van-nav-bar title="全部课程" :border="false" />
    <van-search v-model="keyword" shape="round" placeholder="搜索课程" @search="onSearch" />
    <van-tabs v-model:active="activeCategory" @change="onTabChange">
      <van-tab v-for="cat in categories" :key="cat.id || 0" :title="cat.name || '全部'" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="fetchCourses"
      >
        <CourseCard
          v-for="course in courseList"
          :key="course.id"
          :course="course"
        />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../../api'
import CourseCard from '../../components/CourseCard.vue'

const keyword = ref('')
const activeCategory = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const courseList = ref([])
const categories = ref([{ id: 0, name: '全部' }])

async function fetchCategories() {
  try {
    const res = await get('/categories')
    if (res.data) {
      categories.value = [{ id: 0, name: '全部' }, ...res.data]
    }
  } catch (e) {
    categories.value = [
      { id: 0, name: '全部' },
      { id: 1, name: 'Java开发' },
      { id: 2, name: '前端开发' }
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
    if (data.length < 10) finished.value = true
    page.value++
  } catch (e) {
    finished.value = true
  }
  loading.value = false
}

function onSearch() {
  page.value = 1
  finished.value = false
  courseList.value = []
  loading.value = true
  fetchCourses(true)
}

function onTabChange() {
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
