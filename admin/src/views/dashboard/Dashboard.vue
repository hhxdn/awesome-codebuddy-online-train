<template>
  <div class="page-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #e6f7ff;">
            <el-icon :size="32" color="#1890ff"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalStudents || 0 }}</div>
            <div class="stat-label">总学员数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #f6ffed;">
            <el-icon :size="32" color="#52c41a"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCourses || 0 }}</div>
            <div class="stat-label">总课程数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #fff7e6;">
            <el-icon :size="32" color="#faad14"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayRevenue || 0) }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #fff0f6;">
            <el-icon :size="32" color="#eb2f96"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.monthlyNewStudents || 0 }}</div>
            <div class="stat-label">本月新增学员</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">近30天营收趋势</div>
          <div class="chart-container">
            <v-chart :option="revenueChartOption" autoresize />
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">近7天新增学员趋势</div>
          <div class="chart-container">
            <v-chart :option="studentChartOption" autoresize />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get } from '@/api'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const loading = ref(false)
const stats = reactive({
  totalStudents: 0,
  totalCourses: 0,
  todayRevenue: 0,
  monthlyNewStudents: 0
})
const revenueChartOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [], boundaryGap: false },
  yAxis: { type: 'value' },
  series: [{
    data: [],
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.05)' }]
      }
    },
    itemStyle: { color: '#409eff' }
  }]
})

const studentChartOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{
    data: [],
    type: 'bar',
    itemStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: '#eb2f96' }, { offset: 1, color: 'rgba(235,47,150,0.3)' }]
      },
      borderRadius: [4, 4, 0, 0]
    }
  }]
})

function formatMoney(value) {
  return Number(value).toFixed(2)
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/statistics/dashboard')
    if (res.data) {
      stats.totalStudents = res.data.totalStudents || 0
      stats.totalCourses = res.data.totalCourses || 0
      stats.todayRevenue = res.data.todayRevenue || 0
      stats.monthlyNewStudents = res.data.monthlyNewStudents || 0

      if (res.data.revenueTrend) {
        revenueChartOption.value.xAxis.data = res.data.revenueTrend.map(i => i.date)
        revenueChartOption.value.series[0].data = res.data.revenueTrend.map(i => i.amount)
      }

      if (res.data.studentTrend) {
        studentChartOption.value.xAxis.data = res.data.studentTrend.map(i => i.date)
        studentChartOption.value.series[0].data = res.data.studentTrend.map(i => i.count)
      }
    }
  } catch {
    // dashboard may not be available yet
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
