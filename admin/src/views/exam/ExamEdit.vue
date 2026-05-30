<template>
  <div class="page-container" v-loading="loading">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ isEdit ? '编辑试卷' : '新增试卷' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

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
          <el-col :span="6">
            <el-form-item label="考试时长(分)" prop="durationMinutes">
              <el-input-number v-model="form.durationMinutes" :min="1" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="1" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="form.passScore" :min="1" :max="form.totalScore" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="考试类型">
              <el-radio-group v-model="form.examType">
                <el-radio value="ONLINE">线上考试</el-radio>
                <el-radio value="OFFLINE">线下考试</el-radio>
              </el-radio-group>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                {{ form.examType === 'OFFLINE' ? '学员不能在线参加，需管理员录入成绩' : '学员在线参加，系统自动评分' }}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="最大次数" prop="maxAttempts">
              <el-input-number v-model="form.maxAttempts" :min="1" :max="99" />
            </el-form-item>
          </el-col>
        </el-row>

        <div v-if="form.courseId" class="card-container" style="margin-top: 16px;">
          <div class="card-title">
            选择题目
            <span style="font-weight: normal; font-size: 13px; color: #909399; margin-left: 10px;">
              已选择 {{ form.questionIds.length }} 道题目
            </span>
          </div>
          <el-select v-model="filterChapterId" placeholder="按章节筛选" clearable style="width: 200px; margin-bottom: 12px;" @change="loadQuestions">
            <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
          <el-table v-loading="questionLoading" :data="availableQuestions" border stripe max-height="500" @selection-change="onSelectionChange" ref="questionTableRef">
            <el-table-column type="selection" width="50" />
            <el-table-column label="题型" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="tagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="chapterName" label="章节" width="120" />
            <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
            <el-table-column prop="score" label="分值" width="70" />
          </el-table>
        </div>

        <div style="text-align: center; margin-top: 30px;">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post, put } from '@/api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const questionTableRef = ref(null)
const courses = ref([])
const chapters = ref([])
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
  examType: 'ONLINE',
  questionIds: []
})

const formRules = {
  title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
}

function tagType(type) {
  const map = { SINGLE: '', MULTIPLE: 'success', JUDGE: 'warning', ESSAY: 'danger' }
  return map[type] || 'info'
}

function typeLabel(type) {
  const map = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
  return map[type] || type
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
  chapters.value = []
  availableQuestions.value = []
  filterChapterId.value = null
  if (courseId) {
    try {
      const res = await get(`/admin/courses/${courseId}`)
      chapters.value = res.data?.chapters || []
    } catch { chapters.value = [] }
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

async function fetchExam() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await get(`/admin/exams/${route.params.id}`)
    const data = res.data
    form.title = data.title
    form.courseId = data.courseId
    form.durationMinutes = data.durationMinutes
    form.totalScore = data.totalScore
    form.passScore = data.passScore
    form.maxAttempts = data.maxAttempts || 1
    form.examType = data.examType || 'ONLINE'
    form.questionIds = (data.questionIds || data.questions || []).map(q => q.id || q.questionId || q)

    if (form.courseId) {
      await onCourseChange(form.courseId)
      await nextTick()
      await loadQuestions()
      await nextTick()
      availableQuestions.value.forEach(row => {
        if (form.questionIds.includes(row.id)) {
          questionTableRef.value?.toggleRowSelection(row, true)
        }
      })
    }
  } catch { ElMessage.error('获取试卷信息失败') } finally { loading.value = false }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await put(`/admin/exams/${route.params.id}`, data)
    } else {
      await post('/admin/exams', data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    router.push('/exams')
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

onMounted(() => {
  fetchCourses()
  fetchExam()
})
</script>
