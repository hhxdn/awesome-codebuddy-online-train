<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>试卷管理</h3>
        <el-button type="primary" @click="handleAdd">新增试卷</el-button>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="140" show-overflow-tooltip />
        <el-table-column prop="durationMinutes" label="考试时长(分)" width="110" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="passScore" label="及格分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/exams/edit/${row.id}`)">编辑</el-button>
            <el-button
              v-if="row.status === 'DRAFT'"
              size="small" type="success" link @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-if="row.status === 'PUBLISHED'"
              size="small" type="warning" link @click="handleEnd(row)"
            >结束</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="试卷名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%;" @change="onCourseChange">
                <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="考试时长(分)" prop="durationMinutes">
              <el-input-number v-model="form.durationMinutes" :min="1" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="1" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="form.passScore" :min="1" :max="form.totalScore" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="最大考试次数" prop="maxAttempts">
          <el-input-number v-model="form.maxAttempts" :min="1" :max="99" />
        </el-form-item>

        <div v-if="form.courseId" style="margin-top: 16px;">
          <div class="card-title">选择题目（按章节筛选）</div>
          <el-select v-model="filterChapterId" placeholder="全部章节" clearable style="width: 200px; margin-bottom: 12px;" @change="loadQuestions">
            <el-option v-for="ch in examChapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
          <el-table v-loading="questionLoading" :data="availableQuestions" border stripe max-height="300" @selection-change="onSelectionChange" ref="questionTableRef">
            <el-table-column type="selection" width="50" :selectable="(row) => true" />
            <el-table-column label="题型" width="70">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="chapterName" label="章节" width="100" />
            <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="score" label="分值" width="60" />
          </el-table>
          <div style="margin-top: 8px; color: #909399;">
            已选择 <strong>{{ form.questionIds.length }}</strong> 道题目
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增试卷')
const submitting = ref(false)
const formRef = ref(null)
const questionTableRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const examChapters = ref([])
const availableQuestions = ref([])
const questionLoading = ref(false)
const filterChapterId = ref(null)

const form = reactive({
  title: '',
  courseId: null,
  durationMinutes: 60,
  totalScore: 100,
  passScore: 60,
  maxAttempts: 1,
  questionIds: []
})

const formRules = {
  title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
}

function statusTag(status) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', ENDED: 'danger' }
  return map[status] || 'info'
}

function resetForm() {
  formRef.value?.resetFields()
  form.title = ''
  form.courseId = null
  form.durationMinutes = 60
  form.totalScore = 100
  form.passScore = 60
  form.maxAttempts = 1
  form.questionIds = []
  examChapters.value = []
  availableQuestions.value = []
  filterChapterId.value = null
  isEdit.value = false
  editId.value = null
}

function onSelectionChange(selection) {
  form.questionIds = selection.map(s => s.id)
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

async function onCourseChange(courseId) {
  examChapters.value = []
  availableQuestions.value = []
  filterChapterId.value = null
  if (courseId) {
    try {
      const res = await get(`/admin/courses/${courseId}`)
      examChapters.value = res.data?.chapters || []
    } catch { examChapters.value = [] }
    loadQuestions()
  }
}

async function loadQuestions() {
  if (!form.courseId) return
  questionLoading.value = true
  try {
    const params = { courseId: form.courseId, pageSize: 999 }
    if (filterChapterId.value) params.chapterId = filterChapterId.value
    const res = await get('/admin/questions', params)
    availableQuestions.value = res.data?.records || res.data?.list || []
  } catch { availableQuestions.value = [] } finally { questionLoading.value = false }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/exams', { page: page.value, pageSize: pageSize.value })
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增试卷'
  dialogVisible.value = true
}

async function handleEdit(row) {
  // edit via full page, not dialog
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑试卷'
  try {
    const res = await get(`/admin/exams/${row.id}`)
    const data = res.data
    form.title = data.title
    form.courseId = data.courseId
    form.durationMinutes = data.durationMinutes
    form.totalScore = data.totalScore
    form.passScore = data.passScore
    form.maxAttempts = data.maxAttempts || 1
    form.questionIds = (data.questionIds || data.questions || []).map(q => q.id || q.questionId || q)

    if (form.courseId) {
      await onCourseChange(form.courseId)
      // wait for questions load then set selection
      await new Promise(resolve => setTimeout(resolve, 300))
      nextTick(() => {
        availableQuestions.value.forEach(row => {
          if (form.questionIds.includes(row.id)) {
            questionTableRef.value?.toggleRowSelection(row, true)
          }
        })
      })
    }
    dialogVisible.value = true
  } catch { ElMessage.error('获取试卷信息失败') }
}

async function handlePublish(row) {
  await ElMessageBox.confirm('确定发布该试卷吗？发布后学员可以参加考试。', '提示', { type: 'info' })
  try {
    await put(`/admin/exams/${row.id}/publish`)
    row.status = 'PUBLISHED'
    ElMessage.success('发布成功')
  } catch { ElMessage.error('操作失败') }
}

async function handleEnd(row) {
  await ElMessageBox.confirm('确定结束该考试吗？', '提示', { type: 'warning' })
  try {
    await put(`/admin/exams/${row.id}/end`)
    row.status = 'ENDED'
    ElMessage.success('已结束')
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该试卷吗？', '提示', { type: 'warning' })
  try {
    await del(`/admin/exams/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { ElMessage.error('删除失败') }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await put(`/admin/exams/${editId.value}`, data)
    } else {
      await post('/admin/exams', data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

onMounted(() => {
  fetchCourses()
  fetchData()
})

import { nextTick } from 'vue'
</script>
