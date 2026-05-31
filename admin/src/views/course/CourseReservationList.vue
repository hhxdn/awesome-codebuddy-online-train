<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>线下课程预约管理</h3>
        <div style="display: flex; gap: 10px;">
          <el-select v-model="filterCourseId" placeholder="选择线下课程" clearable @change="fetchData" style="width: 220px;">
            <el-option v-for="c in offlineCourses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="预约状态" clearable @change="fetchData" style="width: 140px;">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </div>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="courseTitle" label="课程名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userName" label="学员" width="120" />
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column label="预约状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reservationTime" label="预约时间" width="170">
          <template #default="{ row }">
            {{ row.reservationTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small" type="success" link @click="handleConfirm(row)"
            >确认</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small" type="warning" link @click="handleCancel(row)"
            >取消</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              size="small" type="primary" link @click="handleComplete(row)"
            >标记完成</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put } from '@/api'

const loading = ref(false)
const tableData = ref([])
const offlineCourses = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterCourseId = ref(null)
const filterStatus = ref('')

function statusTag(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', COMPLETED: '' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
  return map[status] || status
}

async function fetchOfflineCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999, courseType: 'OFFLINE' })
    offlineCourses.value = res.data?.records || res.data?.list || []
  } catch { offlineCourses.value = [] }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (filterCourseId.value) params.courseId = filterCourseId.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await get('/admin/course-reservations', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确认该学员的课程预约吗？', '确认预约', { type: 'info' })
    await put(`/admin/course-reservations/${row.id}/confirm`)
    ElMessage.success('预约已确认')
    row.status = 'CONFIRMED'
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('操作失败')
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该预约吗？', '取消预约', { type: 'warning' })
    await put(`/admin/course-reservations/${row.id}/cancel`)
    ElMessage.success('预约已取消')
    row.status = 'CANCELLED'
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('操作失败')
  }
}

async function handleComplete(row) {
  try {
    await ElMessageBox.confirm('标记该课程为已完成？', '标记完成', { type: 'info' })
    await put(`/admin/course-reservations/${row.id}/complete`)
    ElMessage.success('已标记为完成')
    row.status = 'COMPLETED'
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchOfflineCourses()
  fetchData()
})
</script>
