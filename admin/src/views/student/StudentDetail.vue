<template>
  <div class="page-container" v-loading="loading">
    <div style="display: flex; align-items: center; margin-bottom: 16px;">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
      <span style="font-size: 16px; font-weight: bold; margin-left: 12px;">学员详情</span>
    </div>

    <el-card class="student-info-card" v-if="student.nickname">
      <el-row :gutter="20">
        <el-col :span="4" style="text-align: center;">
          <el-avatar :size="80" :src="student.avatar" />
          <div style="margin-top: 8px; font-weight: bold; font-size: 16px;">{{ student.nickname }}</div>
        </el-col>
        <el-col :span="20">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="手机号">{{ student.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ student.registerTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="总学习时长">{{ formatDuration(student.totalStudyDuration || 0) }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 16px;">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="学习记录" name="learning">
          <el-table :data="learningRecords" border stripe v-loading="tabLoading">
            <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="学习进度" width="200">
              <template #default="{ row }">
                <el-progress :percentage="row.progress || 0" :stroke-width="12" />
              </template>
            </el-table-column>
            <el-table-column label="学习时长" width="120">
              <template #default="{ row }">
                {{ formatDuration(row.duration || 0) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.finished ? 'success' : 'info'" size="small">
                  {{ row.finished ? '已完成' : '学习中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="learningRecords.length === 0 && !tabLoading" style="text-align: center; padding: 40px; color: #909399;">
            暂无学习记录
          </div>
        </el-tab-pane>

        <el-tab-pane label="考试记录" name="exam">
          <el-table :data="examRecords" border stripe v-loading="tabLoading">
            <el-table-column prop="examTitle" label="试卷" min-width="160" show-overflow-tooltip />
            <el-table-column label="成绩" width="120">
              <template #default="{ row }">
                {{ row.score }} / {{ row.totalScore }}
              </template>
            </el-table-column>
            <el-table-column label="是否通过" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isPass ? 'success' : 'danger'" size="small">
                  {{ row.isPass ? '通过' : '未通过' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="170" />
            <el-table-column prop="submitTime" label="提交时间" width="170" />
          </el-table>
          <div v-if="examRecords.length === 0 && !tabLoading" style="text-align: center; padding: 40px; color: #909399;">
            暂无考试记录
          </div>
        </el-tab-pane>

        <el-tab-pane label="订单记录" name="order">
          <el-table :data="orderRecords" border stripe v-loading="tabLoading">
            <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
            <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
            <el-table-column label="金额" width="100">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="payMethod" label="支付方式" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PAID' ? 'success' : 'info'" size="small">
                  {{ row.status === 'PAID' ? '已支付' : row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
          </el-table>
          <div v-if="orderRecords.length === 0 && !tabLoading" style="text-align: center; padding: 40px; color: #909399;">
            暂无订单记录
          </div>
        </el-tab-pane>

        <el-tab-pane label="习题权限" name="exercise">
          <div v-if="exercisesLoading" style="text-align: center; padding: 40px;">
            <el-icon class="is-loading"><i class="el-icon-loading" /></el-icon>
          </div>
          <el-table v-else :data="exerciseAccessList" border stripe>
            <el-table-column prop="courseTitle" label="课程名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="习题权限" width="120" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.hasAccess"
                  active-text="已开通"
                  inactive-text="未开通"
                  @change="(val) => toggleExerciseAccess(row, val)"
                />
              </template>
            </el-table-column>
          </el-table>
          <div v-if="exerciseAccessList.length === 0 && !exercisesLoading" style="text-align: center; padding: 40px; color: #909399;">
            该学员暂无购买课程
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { get, post, del } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const tabLoading = ref(false)
const exercisesLoading = ref(false)
const activeTab = ref('learning')
const student = ref({})

const learningRecords = ref([])
const examRecords = ref([])
const orderRecords = ref([])
const exerciseAccessList = ref([])

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}

async function fetchStudent() {
  loading.value = true
  try {
    const res = await get(`/admin/students/${route.params.id}`)
    student.value = res.data || {}
  } catch { student.value = {} } finally { loading.value = false }
}

async function fetchTabData(tab) {
  tabLoading.value = true
  try {
    const endpoint = {
      learning: `/admin/students/${route.params.id}/learning`,
      exam: `/admin/students/${route.params.id}/exams`,
      order: `/admin/students/${route.params.id}/orders`
    }
    const res = await get(endpoint[tab])
    const data = res.data?.records || res.data?.list || res.data || []
    if (tab === 'learning') learningRecords.value = data
    else if (tab === 'exam') examRecords.value = data
    else if (tab === 'order') orderRecords.value = data
  } catch {
    if (tab === 'learning') learningRecords.value = []
    else if (tab === 'exam') examRecords.value = []
    else if (tab === 'order') orderRecords.value = []
  } finally { tabLoading.value = false }
}

watch(activeTab, (tab) => {
  if (tab === 'exercise') {
    if (exerciseAccessList.value.length === 0) fetchExerciseAccess()
    return
  }
  const arr = { learning: learningRecords, exam: examRecords, order: orderRecords }[tab]
  if (arr && arr.value.length === 0) {
    fetchTabData(tab)
  }
})

async function fetchExerciseAccess() {
  exercisesLoading.value = true
  try {
    const res = await get(`/admin/students/${route.params.id}/exercise-access`)
    exerciseAccessList.value = res.data || []
  } catch { exerciseAccessList.value = [] } finally { exercisesLoading.value = false }
}

async function toggleExerciseAccess(row, val) {
  try {
    if (val) {
      await post(`/admin/students/${route.params.id}/exercise-access/${row.courseId}`)
    } else {
      await del(`/admin/students/${route.params.id}/exercise-access/${row.courseId}`)
    }
    row.hasAccess = val
    ElMessage.success(val ? '已开通习题权限' : '已撤销习题权限')
  } catch {
    row.hasAccess = !val
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchStudent()
  fetchTabData('learning')
})
</script>

<style scoped>
.student-info-card {
  margin-bottom: 0;
}
</style>
