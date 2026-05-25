<template>
  <div class="my-exam-records-page">
    <van-nav-bar title="考试记录" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多记录 —">
        <div v-for="record in records" :key="record.id" class="record-card" @click="viewDetail(record)">
          <div class="record-header">
            <div class="record-name">
              <van-icon name="certificate" size="18" color="var(--primary)" />
              <h4>{{ record.paperName }}</h4>
            </div>
            <van-tag :type="record.passed ? 'success' : 'danger'" size="medium" round>
              {{ record.passed ? '通过' : '未通过' }}
            </van-tag>
          </div>
          <div class="record-stats">
            <span class="record-stat"><van-icon name="gold-coin-o" size="14" /> {{ record.score }}/{{ record.totalScore }}分</span>
            <span class="record-stat"><van-icon name="clock-o" size="14" /> {{ record.duration }}分钟</span>
          </div>
          <div class="record-time">{{ record.createTime }}</div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && records.length === 0" description="暂无考试记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const records = ref([])
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(true)

async function fetchRecords() {
  loading.value = true
  try {
    const res = await get('/user/exam-records')
    if (res.data) records.value = res.data.records || res.data || []
  } catch (e) {
    records.value = [
      { id: 1, paperName: 'Java基础考试', score: 85, totalScore: 100, passed: true, duration: 30, createTime: '2024-01-15' },
      { id: 2, paperName: 'Spring Boot综合测试', score: 55, totalScore: 100, passed: false, duration: 45, createTime: '2024-01-16' }
    ]
  }
  loading.value = false
  finished.value = true
}

function viewDetail(record) {
  router.push('/exam/result/' + record.id)
}

function onRefresh() {
  refreshing.value = true
  fetchRecords().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.my-exam-records-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.record-card {
  background: #fff;
  margin: 10px 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s;
}

.record-card:active {
  transform: scale(0.98);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.record-name {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.record-name h4 {
  font-size: 15px;
  font-weight: 600;
}

.record-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.record-stat {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.record-time {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
