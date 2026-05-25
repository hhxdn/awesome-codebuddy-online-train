<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div class="card-title">课程学习统计</div>
      <el-table :data="courseStats" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentCount" label="学习人数" width="100" />
        <el-table-column prop="completionCount" label="完成人数" width="100" />
        <el-table-column label="完成率" width="100">
          <template #default="{ row }">
            {{ row.studentCount > 0 ? Math.round(row.completionCount / row.studentCount * 100) : 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="平均观看时长" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.avgWatchDuration || 0) }}
          </template>
        </el-table-column>
        <el-table-column label="平均完成率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.avgCompletionRate || 0" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card-container">
      <div class="card-title">章节练习统计</div>
      <el-table :data="chapterStats" border stripe>
        <el-table-column prop="chapterName" label="章节名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="practiceCount" label="练习人次" width="100" />
        <el-table-column label="正确率" width="200">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.correctRate || 0) * 100)" :stroke-width="12" :color="progressColor(row.correctRate)" />
          </template>
        </el-table-column>
      </el-table>
      <div v-if="chapterStats.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        暂无数据
      </div>
    </div>

    <div class="card-container">
      <div class="card-title">错题排行 TOP 10</div>
      <el-table :data="wrongQuestions" border stripe>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="140" />
        <el-table-column prop="wrongCount" label="错误次数" width="100" />
        <el-table-column label="错误率" width="100">
          <template #default="{ row }">
            {{ Math.round((row.wrongRate || 0) * 100) }}%
          </template>
        </el-table-column>
      </el-table>
      <div v-if="wrongQuestions.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        暂无数据
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/api'

const loading = ref(false)
const courseStats = ref([])
const chapterStats = ref([])
const wrongQuestions = ref([])

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}

function progressColor(rate) {
  if (!rate) return '#909399'
  if (rate >= 0.8) return '#67c23a'
  if (rate >= 0.6) return '#e6a23c'
  return '#f56c6c'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/statistics/learning')
    const data = res.data || {}
    courseStats.value = data.courseStats || []
    chapterStats.value = data.chapterStats || []
    wrongQuestions.value = data.wrongQuestions || data.topWrongQuestions || []
  } catch { } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>
