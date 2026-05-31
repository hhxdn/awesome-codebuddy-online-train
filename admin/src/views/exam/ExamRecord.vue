<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="试卷">
          <el-select v-model="query.examId" placeholder="全部试卷" clearable @change="fetchData" style="width: 250px;">
            <el-option v-for="e in exams" :key="e.id" :label="e.title + (e.examType === 'OFFLINE' ? ' [线下]' : ' [线上]')" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="openOfflineScoreDialog">录入线下成绩</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="examTitle" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="考试类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.examType === 'OFFLINE' ? 'warning' : 'primary'" size="small">
              {{ row.examType === 'OFFLINE' ? '线下' : '线上' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="100">
          <template #default="{ row }">
            {{ row.score }} / {{ row.totalScore }}
          </template>
        </el-table-column>
        <el-table-column label="是否通过" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isPass === 1 || row.isPass === true ? 'success' : 'danger'" size="small">
              {{ row.isPass === 1 || row.isPass === true ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证书" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.hasCertificate" type="success" size="small">已颁发</el-tag>
            <span v-else style="color: #C0C4CC;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.examType === 'OFFLINE' && (row.isPass === 1 || row.isPass === true) && !row.hasCertificate"
              size="small" type="success" link @click="handleIssueCert(row)"
            >颁发证书</el-button>
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
      v-model="detailVisible"
      title="答题详情"
      width="800px"
    >
      <div v-if="answers.length === 0" style="text-align: center; color: #909399; padding: 40px;">
        {{ detailExamType === 'OFFLINE' ? '该考试为线下考试，无在线答题记录' : '暂无答题记录' }}
      </div>
      <div v-for="(item, idx) in answers" :key="idx" style="margin-bottom: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
        <div style="font-weight: bold; margin-bottom: 8px;">
          {{ idx + 1 }}. {{ item.questionContent }}
          <el-tag size="small" :type="item.isCorrect ? 'success' : 'danger'" style="margin-left: 8px;">
            {{ item.isCorrect ? '正确' : '错误' }}
          </el-tag>
          <span style="font-size: 12px; color: #909399; margin-left: 8px;">({{ item.score }}分)</span>
        </div>
        <div v-if="item.userAnswer !== undefined && item.userAnswer !== null" style="color: #606266;">
          学员答案：{{ item.userAnswer }}
        </div>
        <div v-if="item.correctAnswer !== undefined && item.correctAnswer !== null" style="color: #67c23a;">
          正确答案：{{ item.correctAnswer }}
        </div>
        <div v-if="item.analysis" style="color: #909399; margin-top: 4px; font-size: 12px;">
          解析：{{ item.analysis }}
        </div>
      </div>
    </el-dialog>

    <!-- 录入线下成绩弹窗 -->
    <el-dialog
      v-model="scoreDialogVisible"
      title="录入线下考试成绩"
      width="500px"
    >
      <el-form ref="scoreFormRef" :model="scoreForm" :rules="scoreRules" label-width="90px">
        <el-form-item label="线下试卷" prop="examPaperId">
          <el-select v-model="scoreForm.examPaperId" placeholder="选择线下考试试卷" filterable style="width: 100%;">
            <el-option v-for="e in offlineExams" :key="e.id" :label="e.title" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员" prop="userId">
          <el-select v-model="scoreForm.userId" placeholder="选择学员" filterable style="width: 100%;">
            <el-option v-for="s in students" :key="s.id" :label="(s.realName || s.nickname) + ' (' + s.phone + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试成绩" prop="score">
          <el-input-number v-model="scoreForm.score" :min="0" :max="200" :precision="1" />
          <span style="margin-left: 8px; font-size: 12px; color: #909399;">分</span>
        </el-form-item>
        <el-form-item label="是否通过" prop="isPass">
          <el-switch v-model="scoreForm.isPass" active-text="通过" inactive-text="未通过" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scoreSubmitting" @click="submitOfflineScore">确认录入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '@/api'

const loading = ref(false)
const tableData = ref([])
const exams = ref([])
const students = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const query = reactive({ examId: null })

const detailVisible = ref(false)
const answers = ref([])
const detailExamType = ref('ONLINE')

// 录入线下成绩
const scoreDialogVisible = ref(false)
const scoreSubmitting = ref(false)
const scoreFormRef = ref(null)
const scoreForm = reactive({
  examPaperId: null,
  userId: null,
  score: 0,
  isPass: false
})
const scoreRules = {
  examPaperId: [{ required: true, message: '请选择试卷', trigger: 'change' }],
  userId: [{ required: true, message: '请选择学员', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }]
}

const offlineExams = computed(() => {
  return exams.value.filter(e => e.examType === 'OFFLINE')
})

async function fetchExams() {
  try {
    const res = await get('/admin/exams', { pageSize: 999 })
    exams.value = res.data?.records || res.data?.list || []
  } catch { exams.value = [] }
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
    const res = await get('/admin/exams/records', {
      page: page.value,
      pageSize: pageSize.value,
      examId: query.examId
    })
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

async function handleViewDetail(row) {
  detailVisible.value = true
  answers.value = []
  detailExamType.value = row.examType || 'ONLINE'
  try {
    const res = await get(`/admin/exams/records/${row.id}`)
    answers.value = res.data?.answers || res.data?.details || []
    detailExamType.value = res.data?.paper?.examType || row.examType || 'ONLINE'
  } catch { answers.value = [] }
}

function openOfflineScoreDialog() {
  scoreForm.examPaperId = null
  scoreForm.userId = null
  scoreForm.score = 0
  scoreForm.isPass = false
  scoreDialogVisible.value = true
}

async function submitOfflineScore() {
  const valid = await scoreFormRef.value?.validate().catch(() => false)
  if (!valid) return
  scoreSubmitting.value = true
  try {
    await post('/admin/exams/records/offline-score', {
      userId: scoreForm.userId,
      examPaperId: scoreForm.examPaperId,
      score: scoreForm.score,
      isPass: scoreForm.isPass ? 1 : 0
    })
    ElMessage.success('线下考试成绩录入成功')
    scoreDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '录入失败')
  } finally { scoreSubmitting.value = false }
}

async function handleIssueCert(row) {
  try {
    await ElMessageBox.confirm('确定为该学员颁发结业证书吗？', '颁发证书', {
      type: 'info',
      confirmButtonText: '确定颁发'
    })
    await post(`/admin/exams/records/${row.id}/issue-certificate`)
    ElMessage.success('证书颁发成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.response?.data?.message || '颁发失败')
    }
  }
}

onMounted(() => {
  fetchExams()
  fetchStudents()
  fetchData()
})
</script>
