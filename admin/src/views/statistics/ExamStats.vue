<template>
  <div class="page-container" v-loading="loading">
    <!-- 考试选择器 -->
    <div class="card-container" style="margin-bottom: 20px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 16px;">
          <h3 style="margin: 0;">考试统计</h3>
          <el-select
            v-model="selectedExamId"
            placeholder="请选择考试"
            clearable
            filterable
            style="width: 280px;"
            @change="onExamChange"
          >
            <el-option
              v-for="exam in examList"
              :key="exam.id"
              :label="exam.title"
              :value="exam.id"
            />
          </el-select>
        </div>
      </div>
      <div v-if="selectedExamTitle" style="margin-top: 12px; color: #606266;">
        当前统计：<el-tag type="primary">{{ selectedExamTitle }}</el-tag>
        <span v-if="selectedExamPassScore" style="margin-left: 12px; color: #909399;">
          及格线 {{ selectedExamPassScore }} 分 / 满分 {{ selectedExamTotalScore }} 分
        </span>
      </div>
    </div>

    <!-- 选择考试后才显示统计卡片 -->
    <template v-if="selectedExamId">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.totalParticipants || 0 }}</div>
            <div class="stat-label">参加人数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ formatScore(stats.avgScore || 0) }}</div>
            <div class="stat-label">平均分</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.passCount || 0 }}</div>
            <div class="stat-label">通过人数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ formatPercent(stats.passRate || 0) }}</div>
            <div class="stat-label">通过率</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="card-container">
        <div class="card-title">成绩分布</div>
        <div class="chart-container">
          <v-chart :option="scoreDistOption" autoresize />
        </div>
      </div>
    </template>

    <!-- 未选择考试时的提示 -->
    <el-empty v-else description="请先选择一场考试，查看详细统计数据" />

    <!-- 所有试卷统计概览表 -->
    <div class="card-container" style="margin-top: 20px;">
      <div class="card-title">全部试卷概览</div>
      <el-table :data="examPaperStats" border stripe>
        <el-table-column prop="examTitle" label="试卷名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="totalParticipants" label="参与人数" width="100" />
        <el-table-column label="平均分" width="90">
          <template #default="{ row }">
            {{ formatScore(row.avgScore) }}
          </template>
        </el-table-column>
        <el-table-column label="通过/总人数" width="110">
          <template #default="{ row }">
            {{ row.passCount || 0 }}/{{ row.totalParticipants || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.passRate || 0)" :stroke-width="12" :color="passColor(row.passRate)" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/api'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'

use([CanvasRenderer, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const loading = ref(false)
const selectedExamId = ref(null)
const examList = ref([])
const selectedExamTitle = ref('')
const selectedExamTotalScore = ref(0)
const selectedExamPassScore = ref(0)
const stats = ref({})
const examPaperStats = ref([])

const scoreDistOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['0-59', '60-69', '70-79', '80-89', '90-100'] },
  yAxis: { type: 'value' },
  series: [{
    data: [0, 0, 0, 0, 0],
    type: 'bar',
    itemStyle: {
      color: (params) => {
        const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#95d475']
        return colors[params.dataIndex]
      },
      borderRadius: [4, 4, 0, 0]
    },
    barWidth: '50%'
  }]
})

function formatScore(val) {
  return Number(val).toFixed(1)
}

function formatPercent(val) {
  return Number(val).toFixed(1) + '%'
}

function passColor(rate) {
  if (!rate) return '#909399'
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

async function fetchData(examId) {
  loading.value = true
  try {
    const params = examId ? { examPaperId: examId } : {}
    const res = await get('/admin/statistics/exam', params)
    const data = res.data || {}

    // 更新下拉列表（首次或刷新时）
    if (data.examPapers && data.examPapers.length > 0) {
      examList.value = data.examPapers
    }

    // 选中考试的信息
    selectedExamTitle.value = data.selectedExamTitle || ''
    selectedExamTotalScore.value = data.selectedExamTotalScore || 0
    selectedExamPassScore.value = data.selectedExamPassScore || 0

    // 统计指标
    stats.value = {
      totalParticipants: data.totalParticipants || 0,
      avgScore: data.avgScore || 0,
      passCount: data.passCount || 0,
      passRate: data.passRate || 0
    }

    // 全部试卷概览
    examPaperStats.value = data.examPaperStats || []

    // 成绩分布
    if (data.scoreDistribution) {
      scoreDistOption.value.series[0].data = data.scoreDistribution
    }
  } catch {
    stats.value = { totalParticipants: 0, avgScore: 0, passCount: 0, passRate: 0 }
    examPaperStats.value = []
  } finally {
    loading.value = false
  }
}

function onExamChange(val) {
  if (val) {
    fetchData(val)
  } else {
    // 清空选中状态，只保留概览表
    stats.value = { totalParticipants: 0, avgScore: 0, passCount: 0, passRate: 0 }
    selectedExamTitle.value = ''
    scoreDistOption.value.series[0].data = [0, 0, 0, 0, 0]
    // 重新加载概览
    fetchData(null)
  }
}

onMounted(() => {
  fetchData(null)
})
</script>
