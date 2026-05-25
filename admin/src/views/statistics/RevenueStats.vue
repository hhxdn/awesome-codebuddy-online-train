<template>
  <div class="page-container" v-loading="loading">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">¥{{ formatMoney(stats.totalRevenue || 0) }}</div>
          <div class="stat-label">总营收</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">¥{{ formatMoney(stats.avgOrderAmount || 0) }}</div>
          <div class="stat-label">平均订单金额</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">近30天日营收趋势</div>
          <div class="chart-container">
            <v-chart :option="dailyRevenueOption" autoresize />
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">近12个月月营收趋势</div>
          <div class="chart-container">
            <v-chart :option="monthlyRevenueOption" autoresize />
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">支付方式分布</div>
          <div class="chart-container" style="height: 350px;">
            <v-chart :option="payMethodOption" autoresize />
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-container">
          <div class="card-title">课程营收排行</div>
          <el-table :data="courseRanking" border stripe max-height="350">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip />
            <el-table-column label="营收" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.revenue || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="80" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/api'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const loading = ref(false)
const stats = ref({})
const courseRanking = ref([])

const dailyRevenueOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{
    data: [], type: 'line', smooth: true,
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.05)' }] } },
    itemStyle: { color: '#409eff' }
  }]
})

const monthlyRevenueOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{
    data: [], type: 'line', smooth: true,
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.05)' }] } },
    itemStyle: { color: '#67c23a' }
  }]
})

const payMethodOption = ref({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: true, formatter: '{b}: {c} ({d}%)' },
    data: []
  }]
})

function formatMoney(value) {
  return Number(value).toFixed(2)
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/statistics/revenue')
    const data = res.data || {}
    stats.value = {
      totalRevenue: data.totalRevenue || 0,
      totalOrders: data.totalOrders || 0,
      avgOrderAmount: data.avgOrderAmount || 0
    }

    courseRanking.value = data.courseRanking || []

    if (data.dailyRevenue) {
      dailyRevenueOption.value.xAxis.data = data.dailyRevenue.map(i => i.date)
      dailyRevenueOption.value.series[0].data = data.dailyRevenue.map(i => i.amount)
    }
    if (data.monthlyRevenue) {
      monthlyRevenueOption.value.xAxis.data = data.monthlyRevenue.map(i => i.month)
      monthlyRevenueOption.value.series[0].data = data.monthlyRevenue.map(i => i.amount)
    }
    if (data.payMethodDistribution) {
      payMethodOption.value.series[0].data = data.payMethodDistribution.map(i => ({
        name: i.method, value: i.count
      }))
    }
  } catch { } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>
