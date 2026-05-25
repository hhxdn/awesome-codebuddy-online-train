<template>
  <div class="page-container" v-loading="loading">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="试卷">
          <el-select v-model="query.examId" placeholder="全部试卷" clearable @change="fetchData" style="width: 250px;">
            <el-option v-for="e in exams" :key="e.id" :label="e.title" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="examTitle" label="试卷名称" min-width="180" show-overflow-tooltip />
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
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
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
      <div v-if="answers.length === 0" style="text-align: center; color: #909399; padding: 40px;">
        暂无答题详情
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get } from '@/api'

const loading = ref(false)
const tableData = ref([])
const exams = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const query = reactive({ examId: null })

const detailVisible = ref(false)
const answers = ref([])

async function fetchExams() {
  try {
    const res = await get('/admin/exams', { pageSize: 999 })
    exams.value = res.data?.records || res.data?.list || []
  } catch { exams.value = [] }
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
  try {
    const res = await get(`/admin/exams/records/${row.id}`)
    answers.value = res.data?.answers || res.data?.details || []
  } catch { answers.value = [] }
}

onMounted(() => {
  fetchExams()
  fetchData()
})
</script>
