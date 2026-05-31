<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>试卷管理</h3>
        <div style="display: flex; gap: 10px;">
          <el-select v-model="filterExamType" placeholder="考试类型" clearable style="width: 140px;" @change="fetchData">
            <el-option label="线上考试" value="ONLINE" />
            <el-option label="线下考试" value="OFFLINE" />
          </el-select>
          <el-button type="primary" @click="handleAdd">新增试卷</el-button>
        </div>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="140" show-overflow-tooltip />
        <el-table-column label="考试类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.examType === 'OFFLINE' ? 'warning' : 'primary'" size="small">
              {{ row.examType === 'OFFLINE' ? '线下' : '线上' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="考试时长(分)" width="110" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/exams/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" type="info" link @click="handlePreview(row)">预览</el-button>
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
        @current-change="fetchData"
        @size-change="fetchData"
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
        <el-form-item label="考试类型" prop="examType">
          <el-radio-group v-model="form.examType">
            <el-radio value="ONLINE">线上考试</el-radio>
            <el-radio value="OFFLINE">线下考试</el-radio>
          </el-radio-group>
          <span style="margin-left: 8px; font-size: 12px; color: #909399;">
            {{ form.examType === 'OFFLINE' ? '线下考试无需试题，学员需预约后参加，由管理员录入成绩' : '学员在线参加，系统自动评分' }}
          </span>
        </el-form-item>

        <div v-if="form.courseId && form.examType !== 'OFFLINE'" style="margin-top: 16px;">
          <div class="card-title">选择题目（按章节筛选）</div>
          <el-select v-model="filterChapterId" placeholder="全部章节" clearable style="width: 200px; margin-bottom: 12px;" @change="loadQuestions">
            <el-option v-for="ch in examChapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
          <el-table v-loading="questionLoading" :data="availableQuestions" border stripe max-height="300" @selection-change="onSelectionChange" ref="questionTableRef">
            <el-table-column type="selection" width="50" :selectable="(row) => true" />
            <el-table-column label="题型" width="70">
              <template #default="{ row }">
                <el-tag size="small" :type="tagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
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

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      title="试卷预览"
      width="900px"
      top="30px"
    >
      <template v-if="previewData">
        <div class="preview-header">
          <div class="preview-title">{{ previewData.title }}</div>
          <div class="preview-meta">
            <el-tag :type="previewData.examType === 'OFFLINE' ? 'warning' : 'primary'" size="small">
              {{ previewData.examType === 'OFFLINE' ? '线下考试' : '线上考试' }}
            </el-tag>
            <span>考试时长：<b>{{ previewData.durationMinutes }}</b> 分钟</span>
            <span>总分：<b>{{ previewData.totalScore }}</b> 分</span>
            <span>及格分：<b>{{ previewData.passScore }}</b> 分</span>
            <span>题数：<b>{{ previewData.questionCount }}</b> 道</span>
          </div>
          <el-switch v-model="showAnswers" active-text="显示答案" inactive-text="隐藏答案" style="margin-top: 10px;" />
        </div>
        <div class="preview-questions">
          <div v-for="(q, idx) in previewData.questions" :key="q.id" class="preview-question-item">
            <div class="pq-header">
              <span class="pq-num">{{ idx + 1 }}.</span>
              <el-tag :type="tagType(q.type)" size="small" effect="dark">{{ typeLabel(q.type) }}</el-tag>
              <span class="pq-score">（{{ q.score }} 分）</span>
            </div>
            <div class="pq-content" v-html="q.content"></div>
            <!-- 选项 -->
            <div v-if="q.options && q.options.length" class="pq-options">
              <div
                v-for="opt in q.options"
                :key="opt.label"
                class="pq-option"
                :class="{ 'is-correct': showAnswers && opt.isCorrect }"
              >
                <span class="pq-opt-label">{{ opt.label }}.</span>
                <span>{{ opt.content }}</span>
                <el-icon v-if="showAnswers && opt.isCorrect" class="pq-check"><Select /></el-icon>
              </div>
            </div>
            <!-- 判断题 -->
            <div v-if="q.type === 'JUDGE'" class="pq-options">
              <div class="pq-option" :class="{ 'is-correct': showAnswers && q.answer === 'T' }">
                <span class="pq-opt-label">✓</span><span>正确</span>
              </div>
              <div class="pq-option" :class="{ 'is-correct': showAnswers && q.answer === 'F' }">
                <span class="pq-opt-label">✗</span><span>错误</span>
              </div>
            </div>
            <!-- 答案和解析 -->
            <div v-if="showAnswers" class="pq-answer">
              <div class="pq-answer-label">
                正确答案：
                <b v-if="q.type === 'JUDGE'">{{ q.answer === 'T' ? '正确' : '错误' }}</b>
                <b v-else>{{ q.answer }}</b>
              </div>
              <div v-if="q.analysis" class="pq-analysis">
                <span class="pq-analysis-label">解析：</span>{{ q.analysis }}
              </div>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Select } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/api'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterExamType = ref('')

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

// 预览相关
const previewVisible = ref(false)
const previewData = ref(null)
const showAnswers = ref(false)

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

function statusTag(status) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', ENDED: 'danger' }
  return map[status] || 'info'
}

function tagType(type) {
  const map = { SINGLE: '', MULTIPLE: 'success', JUDGE: 'warning', ESSAY: 'danger' }
  return map[type] || 'info'
}

function typeLabel(type) {
  const map = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
  return map[type] || type
}

function resetForm() {
  formRef.value?.resetFields()
  form.title = ''
  form.courseId = null
  form.durationMinutes = 60
  form.totalScore = 100
  form.passScore = 60
  form.maxAttempts = 1
  form.examType = 'ONLINE'
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
    const params = { page: page.value, pageSize: pageSize.value }
    if (filterExamType.value) params.examType = filterExamType.value
    const res = await get('/admin/exams', params)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  // 跳转到独立的编辑页面（统一使用 ExamEdit.vue）
  router.push('/exams/edit/new')
}

// 预览
async function handlePreview(row) {
  showAnswers.value = false
  previewData.value = null
  previewVisible.value = true
  try {
    const res = await get(`/admin/exams/${row.id}/preview`)
    previewData.value = res.data
  } catch {
    ElMessage.error('加载试卷预览失败')
    previewVisible.value = false
  }
}

async function handleEdit(row) {
  // edit via full page, not dialog
  router.push(`/exams/edit/${row.id}`)
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm('确定发布该试卷吗？发布后学员可以参加考试。', '提示', { type: 'info' })
    await put(`/admin/exams/${row.id}/publish`)
    row.status = 'PUBLISHED'
    ElMessage.success('发布成功')
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleEnd(row) {
  try {
    await ElMessageBox.confirm('确定结束该考试吗？', '提示', { type: 'warning' })
    await put(`/admin/exams/${row.id}/end`)
    row.status = 'ENDED'
    ElMessage.success('已结束')
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该试卷吗？', '提示', { type: 'warning' })
    await del(`/admin/exams/${row.id}`)
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
</script>

<style scoped>
.preview-header {
  border-bottom: 2px solid #ebeef5;
  padding-bottom: 16px;
  margin-bottom: 20px;
}
.preview-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.preview-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #606266;
}
.preview-meta b {
  color: #303133;
}
.preview-questions {
  max-height: 55vh;
  overflow-y: auto;
  padding-right: 4px;
}
.preview-question-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 14px;
  border: 1px solid #e4e7ed;
  transition: border-color 0.2s;
}
.preview-question-item:hover {
  border-color: #c0c4cc;
}
.pq-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.pq-num {
  font-weight: 700;
  font-size: 15px;
  color: #409eff;
  min-width: 28px;
}
.pq-score {
  font-size: 12px;
  color: #909399;
}
.pq-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  margin-bottom: 10px;
}
.pq-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}
.pq-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  min-width: 120px;
}
.pq-option.is-correct {
  border-color: #67c23a;
  background: #f0f9eb;
  color: #67c23a;
  font-weight: 600;
}
.pq-opt-label {
  font-weight: 700;
  color: #409eff;
}
.pq-option.is-correct .pq-opt-label {
  color: #67c23a;
}
.pq-check {
  color: #67c23a;
  margin-left: auto;
}
.pq-answer {
  margin-top: 10px;
  padding: 10px 12px;
  background: #ecf5ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}
.pq-answer-label {
  font-size: 13px;
  color: #303133;
}
.pq-answer-label b {
  color: #f56c6c;
}
.pq-analysis {
  margin-top: 6px;
  font-size: 13px;
  color: #e6a23c;
  line-height: 1.6;
}
.pq-analysis-label {
  font-weight: 600;
  color: #e6a23c;
}
</style>
