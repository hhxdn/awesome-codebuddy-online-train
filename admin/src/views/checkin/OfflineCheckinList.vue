<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="打卡方式">
          <el-select v-model="query.checkinType" placeholder="全部" clearable filterable style="width: 130px;" @change="fetchData">
            <el-option label="全部" value="" />
            <el-option label="自主打卡" value="SELF" />
            <el-option label="后台代打卡" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="query.courseId" placeholder="全部课程" clearable filterable style="width: 220px;" @change="fetchData">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 代打卡弹窗 -->
    <el-dialog v-model="dialogVisible" title="替学员打卡" width="450px">
      <el-form label-width="90px">
        <el-form-item label="学员">
          <el-select v-model="checkinForm.userId" placeholder="选择学员" filterable style="width: 100%;">
            <el-option v-for="s in students" :key="s.id" :label="(s.realName || s.nickname) + ' (' + s.phone + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="线下课程">
          <el-select v-model="checkinForm.courseId" placeholder="选择线下课程" filterable style="width: 100%;">
            <el-option v-for="c in offlineCourses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdminCheckin" :loading="submitting">确认打卡</el-button>
      </template>
    </el-dialog>

    <div class="card-container">
      <div style="margin-bottom: 12px; text-align: right;">
        <el-button type="primary" @click="openAdminCheckinDialog">替学员打卡</el-button>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="userName" label="学员" width="120" />
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column prop="courseTitle" label="课程" min-width="180" show-overflow-tooltip />
        <el-table-column label="打卡方式" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.checkinType === 'SELF'" type="success" size="small">自主打卡</el-tag>
            <el-tag v-else type="warning" size="small">后台代打卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="打卡距离" width="100">
          <template #default="{ row }">
            {{ row.distance != null ? row.distance + '米' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="打卡时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
import { get, post, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const offlineCourses = ref([])
const students = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)

const query = reactive({ courseId: null, checkinType: '' })
const checkinForm = reactive({ userId: null, courseId: null })

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { size: 999 })
    const all = res.data?.records || res.data?.list || []
    courses.value = all
    offlineCourses.value = all.filter(c => c.courseType === 'OFFLINE')
  } catch { courses.value = []; offlineCourses.value = [] }
}

async function fetchStudents() {
  try {
    const res = await get('/admin/students', { size: 999 })
    students.value = res.data?.records || res.data?.list || []
  } catch { students.value = [] }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (query.courseId) params.courseId = query.courseId
    if (query.checkinType) params.checkinType = query.checkinType
    const res = await get('/admin/checkins', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function resetQuery() {
  query.courseId = null
  query.checkinType = ''
  page.value = 1
  fetchData()
}

function openAdminCheckinDialog() {
  checkinForm.userId = null
  checkinForm.courseId = null
  dialogVisible.value = true
}

async function submitAdminCheckin() {
  if (!checkinForm.userId || !checkinForm.courseId) {
    ElMessage.warning('请选择学员和课程')
    return
  }
  submitting.value = true
  try {
    await post('/admin/checkins/admin-checkin', {
      userId: checkinForm.userId,
      courseId: checkinForm.courseId
    })
    ElMessage.success('打卡成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '打卡失败')
  } finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该打卡记录吗？', '提示', { type: 'warning' })
    await del(`/admin/checkins/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchCourses()
  fetchStudents()
  fetchData()
})
</script>
