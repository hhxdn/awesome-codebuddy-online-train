<template>
  <div class="page-container" v-loading="loading">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.totalExams || 0 }}</div>
          <div class="stat-label">总考试次数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ formatScore(stats.avgScore || 0) }}</div>
          <div class="stat-label">平均分</div>
        </el-card>
      </el-col>
      <el-col :span="8">
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

    <div class="card-container">
      <div class="card-title">试卷统计</div>
      <el-table :data="examPaperStats" border stripe>
        <el-table-column prop="examTitle" label="试卷名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="totalParticipants" label="参与人数" width="100" />
        <el-table-column prop="absentCount" label="缺考人数" width="100" />
        <el-table-column label="平均分" width="90">
          <template #default="{ row }">
            {{ formatScore(row.avgScore) }}
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.passRate || 0)" :stroke-width="12" :color="passColor(row.passRate)" />
          </template>
        </el-table-column>
      </el-table>
      <div v-if="examPaperStats.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        暂无数据
      </div>
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

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/statistics/exam')
    const data = res.data || {}
    stats.value = {
      totalExams: data.totalExams || 0,
      avgScore: data.avgScore || 0,
      passRate: data.passRate || 0
    }
    examPaperStats.value = data.examPaperStats || []

    if (data.scoreDistribution) {
      scoreDistOption.value.series[0].data = data.scoreDistribution
    }
  } catch { } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>
