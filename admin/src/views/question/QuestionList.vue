<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="课程">
          <el-select v-model="query.courseId" placeholder="全部课程" clearable @change="onCourseChange" style="width: 180px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="query.chapterId" placeholder="全部章节" clearable :disabled="!query.courseId" style="width: 180px;">
            <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 120px;">
            <el-option label="单选" value="SINGLE" />
            <el-option label="多选" value="MULTIPLE" />
            <el-option label="判断" value="JUDGE" />
            <el-option label="简答" value="ESSAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索题目内容" clearable @keyup.enter="fetchData" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        <el-button @click="handleAdd">新增题目</el-button>
        <el-button type="primary" @click="$router.push('/questions/import')">导入题目</el-button>
      </div>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="courseName" label="课程" width="140" show-overflow-tooltip />
        <el-table-column prop="chapterName" label="章节" width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="tagType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="handleToggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%;" @change="onFormCourseChange">
                <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="章节" prop="chapterId">
              <el-select v-model="form.chapterId" placeholder="请选择章节" style="width: 100%;" :disabled="!form.courseId">
                <el-option v-for="ch in formChapters" :key="ch.id" :label="ch.title" :value="ch.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio value="SINGLE">单选</el-radio>
            <el-radio value="MULTIPLE">多选</el-radio>
            <el-radio value="JUDGE">判断</el-radio>
            <el-radio value="ESSAY">简答</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="0" :step="1" />
        </el-form-item>

        <!-- 单选/多选选项 -->
        <template v-if="form.type === 'SINGLE' || form.type === 'MULTIPLE'">
          <el-form-item label="选项列表">
            <div v-for="(opt, idx) in form.options" :key="idx" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center;">
              <span>{{ optionLabels[idx] }}.</span>
              <el-input v-model="opt.content" placeholder="选项内容" style="flex: 1;" size="small" />
              <el-checkbox v-if="form.type === 'MULTIPLE'" v-model="opt.isCorrect" size="small">正确</el-checkbox>
              <el-radio v-else v-model="form.correctIndex" :value="idx" size="small" style="margin-left: 8px;">正确</el-radio>
              <el-button size="small" type="danger" :icon="Delete" circle @click="removeOption(idx)" :disabled="form.options.length <= 2" />
            </div>
            <el-button size="small" type="primary" text @click="addOption" :disabled="form.options.length >= 8">+ 添加选项</el-button>
          </el-form-item>
        </template>

        <!-- 判断题 -->
        <el-form-item v-if="form.type === 'JUDGE'" label="正确答案">
          <el-radio-group v-model="form.judgeAnswer">
            <el-radio :value="true">正确</el-radio>
            <el-radio :value="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 简答题 -->
        <el-form-item v-if="form.type === 'ESSAY'" label="参考答案">
          <el-input v-model="form.essayAnswer" type="textarea" :rows="3" placeholder="请输入参考答案" />
        </el-form-item>

        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入题目解析" />
        </el-form-item>
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
import { Delete } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const chapters = ref([])
const formChapters = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const query = reactive({ courseId: null, chapterId: null, type: '', keyword: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

const form = reactive({
  courseId: null,
  chapterId: null,
  type: 'SINGLE',
  content: '',
  score: 5,
  options: [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ],
  correctIndex: 0,
  judgeAnswer: true,
  essayAnswer: '',
  analysis: ''
})

const formRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  chapterId: [{ required: true, message: '请选择章节', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
}

function tagType(type) {
  const map = { SINGLE: '', MULTIPLE: 'success', JUDGE: 'warning', ESSAY: 'danger' }
  return map[type] || 'info'
}

function onTypeChange() {
  form.options = [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ]
  form.correctIndex = 0
  form.judgeAnswer = true
  form.essayAnswer = ''
}

function addOption() {
  form.options.push({ content: '', isCorrect: false })
}

function removeOption(idx) {
  form.options.splice(idx, 1)
  if (form.correctIndex >= form.options.length) {
    form.correctIndex = form.options.length - 1
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.courseId = null
  form.chapterId = null
  form.type = 'SINGLE'
  form.content = ''
  form.score = 5
  form.options = [{ content: '', isCorrect: false }, { content: '', isCorrect: false }]
  form.correctIndex = 0
  form.judgeAnswer = true
  form.essayAnswer = ''
  form.analysis = ''
  isEdit.value = false
  editId.value = null
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

async function onCourseChange(courseId) {
  query.chapterId = null
  chapters.value = []
  if (courseId) {
    try {
      const res = await get(`/admin/courses/${courseId}`)
      chapters.value = res.data?.chapters || []
    } catch { chapters.value = [] }
  }
  fetchData()
}

async function onFormCourseChange(courseId) {
  form.chapterId = null
  formChapters.value = []
  if (courseId) {
    try {
      const res = await get(`/admin/courses/${courseId}`)
      formChapters.value = res.data?.chapters || []
    } catch { formChapters.value = [] }
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/questions', {
      page: page.value,
      pageSize: pageSize.value,
      ...query
    })
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function resetQuery() {
  query.courseId = null
  query.chapterId = null
  query.type = ''
  query.keyword = ''
  chapters.value = []
  page.value = 1
  fetchData()
}

function handleAdd() {
  dialogTitle.value = '新增题目'
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑题目'
  try {
    const res = await get(`/admin/questions/${row.id}`)
    const data = res.data
    form.courseId = data.courseId
    form.chapterId = data.chapterId
    form.type = data.type
    form.content = data.content
    form.score = data.score
    form.analysis = data.analysis || ''

    if (data.options) {
      form.options = data.options.map(o => ({
        content: o.content || o.optionContent || '',
        isCorrect: o.isCorrect === 1 || o.isCorrect === true
      }))
      form.correctIndex = data.options.findIndex(o => o.isCorrect === 1 || o.isCorrect === true)
      if (form.correctIndex < 0) form.correctIndex = 0
    }
    if (data.type === 'JUDGE') {
      form.judgeAnswer = data.answer === true || data.answer === 'true' || data.answer === 1
    }
    if (data.type === 'ESSAY') {
      form.essayAnswer = data.answer || ''
    }

    if (form.courseId) await onFormCourseChange(form.courseId)
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取题目信息失败')
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await put(`/admin/questions/${row.id}/status`, { status: newStatus })
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
    await del(`/admin/questions/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const data = {
    courseId: form.courseId,
    chapterId: form.chapterId,
    type: form.type,
    content: form.content,
    score: form.score,
    analysis: form.analysis
  }

  if (form.type === 'SINGLE' || form.type === 'MULTIPLE') {
    data.options = form.options.map((o, i) => ({
      optionLabel: optionLabels[i],
      content: o.content,
      isCorrect: form.type === 'SINGLE' ? (i === form.correctIndex) : o.isCorrect
    }))
  } else if (form.type === 'JUDGE') {
    data.answer = form.judgeAnswer
  } else if (form.type === 'ESSAY') {
    data.answer = form.essayAnswer
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await put(`/admin/questions/${editId.value}`, data)
    } else {
      await post('/admin/questions', data)
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
</script>
