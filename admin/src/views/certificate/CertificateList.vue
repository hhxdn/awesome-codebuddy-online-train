<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="证书类型">
          <el-select v-model="query.certType" placeholder="全部" clearable style="width: 130px;" @change="fetchData">
            <el-option label="全部" value="" />
            <el-option label="单课程" value="COURSE" />
            <el-option label="全课程" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员">
          <el-input v-model="query.keyword" placeholder="姓名/手机号搜索" clearable @keyup.enter="fetchData" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 颁发证书弹窗 -->
    <el-dialog v-model="dialogVisible" title="颁发结业证书" width="500px">
      <el-form label-width="90px">
        <el-form-item label="证书类型" required>
          <el-radio-group v-model="issueForm.certType" @change="onCertTypeChange">
            <el-radio value="COURSE">单课程结业</el-radio>
            <el-radio value="ALL">全部课程结业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学员" required>
          <el-select v-model="issueForm.userId" placeholder="选择学员" filterable style="width: 100%;">
            <el-option v-for="s in students" :key="s.id" :label="(s.realName || s.nickname) + ' (' + s.phone + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="issueForm.certType === 'COURSE'" label="课程" required>
          <el-select v-model="issueForm.courseId" placeholder="选择课程" filterable style="width: 100%;">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitIssue" :loading="submitting">颁发证书</el-button>
      </template>
    </el-dialog>

    <div class="card-container">
      <div style="margin-bottom: 12px; text-align: right;">
        <el-button type="primary" @click="openIssueDialog">颁发证书</el-button>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="certNo" label="证书编号" width="220" show-overflow-tooltip />
        <el-table-column prop="userName" label="学员" width="120" />
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column prop="title" label="证书标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.certType === 'COURSE'" type="primary" size="small">单课程</el-tag>
            <el-tag v-else type="success" size="small">全部课程</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">有效</el-tag>
            <el-tag v-else type="danger" size="small">已撤销</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueTime" label="颁发时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              <el-button size="small" type="warning" link @click="handleRevoke(row)">撤销</el-button>
            </template>
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
        @current-change="fetchData"
        @size-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const students = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)

const query = reactive({ certType: '', keyword: '' })
const issueForm = reactive({ certType: 'COURSE', userId: null, courseId: null })

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

async function fetchStudents() {
  try {
    const res = await get('/admin/students', { pageSize: 999 })
    students.value = res.data?.records || res.data?.list || []
  } catch { students.value = [] }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (query.certType) params.certType = query.certType
    if (query.keyword) params.keyword = query.keyword
    const res = await get('/admin/certificates', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function resetQuery() {
  query.certType = ''
  query.keyword = ''
  page.value = 1
  fetchData()
}

function openIssueDialog() {
  issueForm.certType = 'COURSE'
  issueForm.userId = null
  issueForm.courseId = null
  dialogVisible.value = true
}

function onCertTypeChange() {
  if (issueForm.certType === 'ALL') {
    issueForm.courseId = null
  }
}

async function submitIssue() {
  if (!issueForm.userId) { ElMessage.warning('请选择学员'); return }
  if (issueForm.certType === 'COURSE' && !issueForm.courseId) { ElMessage.warning('请选择课程'); return }
  submitting.value = true
  try {
    const body = { userId: issueForm.userId, certType: issueForm.certType }
    if (issueForm.certType === 'COURSE') body.courseId = issueForm.courseId
    await post('/admin/certificates', body)
    ElMessage.success('证书颁发成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '颁发失败')
  } finally { submitting.value = false }
}

async function handleRevoke(row) {
  try {
    await ElMessageBox.confirm('确定撤销该证书吗？撤销后学员将无法查看。', '提示', { type: 'warning' })
    await put(`/admin/certificates/${row.id}/revoke`)
    ElMessage.success('证书已撤销')
    fetchData()
  } catch { /* cancel */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该证书吗？', '提示', { type: 'warning' })
    await del(`/admin/certificates/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancel */ }
}

onMounted(() => {
  fetchCourses()
  fetchStudents()
  fetchData()
})
</script>
