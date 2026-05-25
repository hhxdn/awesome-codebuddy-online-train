<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="课程名称" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 180px;">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px;">
            <el-option label="上架" value="UP" />
            <el-option label="下架" value="DOWN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        <el-button type="primary" @click="$router.push('/courses/edit/new')">新增课程</el-button>
      </div>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              style="width: 50px; height: 35px; border-radius: 4px;"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isFree === 1" type="success" size="small">免费</el-tag>
            <span v-else>¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学员数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UP' ? 'success' : 'info'" size="small">
              {{ row.status === 'UP' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommend === 1" type="warning" size="small">推荐</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/courses/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 'UP' ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" :type="row.isRecommend === 1 ? 'info' : 'warning'" link @click="handleToggleRecommend(row)">
              {{ row.isRecommend === 1 ? '取消推荐' : '推荐' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const query = reactive({
  keyword: '',
  categoryId: null,
  status: ''
})

async function fetchCategories() {
  try {
    const res = await get('/admin/categories', { pageSize: 999 })
    categories.value = res.data?.records || res.data?.list || []
  } catch {
    categories.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      ...query
    }
    const res = await get('/admin/courses', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.categoryId = null
  query.status = ''
  page.value = 1
  fetchData()
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 'UP' ? 'DOWN' : 'UP'
  try {
    await put(`/admin/courses/${row.id}/status`, { status: newStatus })
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleToggleRecommend(row) {
  const newVal = row.isRecommend === 1 ? 0 : 1
  try {
    await put(`/admin/courses/${row.id}`, { isRecommend: newVal })
    row.isRecommend = newVal
    ElMessage.success('操作成功')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该课程吗？', '提示', { type: 'warning' })
  try {
    await del(`/admin/courses/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>
