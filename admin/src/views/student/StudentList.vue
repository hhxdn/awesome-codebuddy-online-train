<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="审核状态">
          <el-select v-model="query.approvalStatus" placeholder="全部" clearable style="width: 130px;" @change="fetchData">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="手机号/昵称搜索" clearable @keyup.enter="fetchData" style="width: 250px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.approvalStatus === 'PENDING'" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.approvalStatus === 'APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="row.approvalStatus === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            <el-tag v-else type="info" size="small">-</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="170" />
        <el-table-column label="总学习时长" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.totalStudyDuration || 0) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/students/${row.id}`)">详情</el-button>
            <template v-if="row.approvalStatus === 'PENDING'">
              <el-button size="small" type="success" link @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
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
import { ref, reactive, onMounted } from 'vue'
import { get, put } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const query = reactive({ keyword: '', approvalStatus: '' })

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      keyword: query.keyword
    }
    if (query.approvalStatus) {
      params.approvalStatus = query.approvalStatus
    }
    const res = await get('/admin/students', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过学员「${row.nickname || row.phone}」的审核吗？`, '审核通过', { type: 'info' })
    await put(`/admin/students/${row.id}/approve`)
    ElMessage.success('已审核通过')
    fetchData()
  } catch { /* 取消操作 */ }
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm(`确认拒绝学员「${row.nickname || row.phone}」的申请吗？`, '审核拒绝', { type: 'warning' })
    await put(`/admin/students/${row.id}/reject`)
    ElMessage.success('已拒绝')
    fetchData()
  } catch { /* 取消操作 */ }
}

function resetQuery() {
  query.keyword = ''
  query.approvalStatus = ''
  page.value = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>
