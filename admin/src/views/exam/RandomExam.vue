<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <el-form ref="formRef" :model="form" label-width="110px" :rules="rules">
        <el-form-item label="选择课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="选择课程" filterable style="width: 400px;">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="form.title" placeholder="留空自动生成" style="width: 400px;" />
        </el-form-item>

        <el-divider content-position="left">题目配置</el-divider>

        <el-form-item label="单选题数量">
          <el-input-number v-model="form.singleCount" :min="0" :max="200" />
          <span style="margin-left: 8px; color: #909399;">
            题库中：{{ questionCounts.SINGLE || 0 }}道
          </span>
        </el-form-item>

        <el-form-item label="多选题数量">
          <el-input-number v-model="form.multipleCount" :min="0" :max="200" />
          <span style="margin-left: 8px; color: #909399;">
            题库中：{{ questionCounts.MULTIPLE || 0 }}道
          </span>
        </el-form-item>

        <el-form-item label="判断题数量">
          <el-input-number v-model="form.judgeCount" :min="0" :max="200" />
          <span style="margin-left: 8px; color: #909399;">
            题库中：{{ questionCounts.JUDGE || 0 }}道
          </span>
        </el-form-item>

        <el-divider content-position="left">考试设置</el-divider>

        <el-form-item label="考试时长(分钟)" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" :max="300" />
        </el-form-item>

        <el-form-item label="及格分数" prop="passScore">
          <el-input-number v-model="form.passScore" :min="1" :max="200" />
        </el-form-item>

        <el-form-item label="最大考试次数">
          <el-input-number v-model="form.maxAttempts" :min="1" :max="10" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="handleGenerate" :loading="generating">
            随机生成试卷
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 生成结果弹窗 -->
    <el-dialog v-model="resultVisible" title="组卷成功" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="试卷标题">{{ result.title }}</el-descriptions-item>
        <el-descriptions-item label="单选题">{{ result.singleCount }}道</el-descriptions-item>
        <el-descriptions-item label="多选题">{{ result.multipleCount }}道</el-descriptions-item>
        <el-descriptions-item label="判断题">{{ result.judgeCount }}道</el-descriptions-item>
        <el-descriptions-item label="总题数">{{ result.totalQuestions }}道</el-descriptions-item>
        <el-descriptions-item label="总分">{{ result.totalScore }}分</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="resultVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToExamList">查看试卷列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post } from '@/api'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const resultVisible = ref(false)
const courses = ref([])
const questionCounts = ref({})

const form = reactive({
  courseId: null,
  title: '',
  singleCount: 60,
  multipleCount: 20,
  judgeCount: 20,
  durationMinutes: 120,
  passScore: 60,
  maxAttempts: 1
})

const result = reactive({
  title: '',
  singleCount: 0,
  multipleCount: 0,
  judgeCount: 0,
  totalQuestions: 0,
  totalScore: 0
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  durationMinutes: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
  passScore: [{ required: true, message: '请输入及格分数', trigger: 'blur' }]
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

watch(() => form.courseId, async (newVal) => {
  if (!newVal) { questionCounts.value = {}; return }
  try {
    const res = await get('/admin/questions', { courseId: newVal, pageSize: 9999 })
    const questions = res.data?.records || res.data?.list || []
    questionCounts.value = {}
    questions.forEach(q => {
      questionCounts.value[q.type] = (questionCounts.value[q.type] || 0) + 1
    })
  } catch { questionCounts.value = {} }
})

async function handleGenerate() {
  if (!form.courseId) { ElMessage.warning('请选择课程'); return }

  const counts = questionCounts.value
  if ((counts.SINGLE || 0) < form.singleCount) { ElMessage.warning(`题库中单选题不足${form.singleCount}道`); return }
  if ((counts.MULTIPLE || 0) < form.multipleCount) { ElMessage.warning(`题库中多选题不足${form.multipleCount}道`); return }
  if ((counts.JUDGE || 0) < form.judgeCount) { ElMessage.warning(`题库中判断题不足${form.judgeCount}道`); return }

  generating.value = true
  try {
    const res = await post('/admin/exams/random', {
      courseId: form.courseId,
      title: form.title || undefined,
      singleCount: form.singleCount,
      multipleCount: form.multipleCount,
      judgeCount: form.judgeCount,
      durationMinutes: form.durationMinutes,
      passScore: form.passScore,
      maxAttempts: form.maxAttempts
    })
    Object.assign(result, res.data)
    resultVisible.value = true
    ElMessage.success(res.data?.message || '组卷成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '组卷失败')
  } finally { generating.value = false }
}

function goToExamList() {
  resultVisible.value = false
  router.push('/exams')
}

onMounted(() => fetchCourses())
</script>
