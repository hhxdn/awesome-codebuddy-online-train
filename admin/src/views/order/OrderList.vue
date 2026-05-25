<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px;">
            <el-option label="已支付" value="PAID" />
            <el-option label="未支付" value="UNPAID" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="query.courseId" placeholder="全部课程" clearable style="width: 200px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学员" width="120" />
        <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.payMethod || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <el-descriptions v-if="currentOrder.orderNo" :column="1" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="学员">{{ currentOrder.studentName }}</el-descriptions-item>
        <el-descriptions-item label="课程">{{ currentOrder.courseName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentOrder.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get } from '@/api'

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const query = reactive({ dateRange: [], status: '', courseId: null })

const detailVisible = ref(false)
const currentOrder = ref({})

function statusTag(status) {
  const map = { PAID: 'success', UNPAID: 'warning', REFUNDED: 'info' }
  return map[status] || 'info'
}

function statusText(status) {
  const map = { PAID: '已支付', UNPAID: '未支付', REFUNDED: '已退款' }
  return map[status] || status
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value, status: query.status, courseId: query.courseId }
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = query.dateRange[0]
      params.endDate = query.dateRange[1]
    }
    const res = await get('/admin/orders', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function resetQuery() {
  query.dateRange = []
  query.status = ''
  query.courseId = null
  page.value = 1
  fetchData()
}

function handleViewDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchCourses()
  fetchData()
})
</script>
