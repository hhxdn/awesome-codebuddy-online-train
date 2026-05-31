<template>
  <div class="my-reservations-page page-fade-in">
    <van-nav-bar title="我的课程预约" left-text="返回" left-arrow @click-left="$router.back()" :border="false" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="list.length > 0">
        <div v-for="item in list" :key="item.id" class="reservation-card">
          <div class="card-header">
            <div class="course-cover-mini">
              <img v-if="item.courseCover" :src="item.courseCover" alt="cover" />
              <van-icon v-else name="bookmark-o" size="24" color="#999" />
            </div>
            <div class="header-info">
              <h3 class="course-title">{{ item.courseTitle }}</h3>
              <span class="status-badge" :class="'badge-' + item.status.toLowerCase()">
                {{ statusLabel(item.status) }}
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="info-row" v-if="item.reservationTime">
              <van-icon name="clock-o" size="14" />
              <span>预约时间：{{ item.reservationTime }}</span>
            </div>
            <div class="info-row">
              <van-icon name="records" size="14" />
              <span>提交时间：{{ item.createTime }}</span>
            </div>
            <div v-if="item.remark" class="info-row">
              <van-icon name="notes-o" size="14" />
              <span>备注：{{ item.remark }}</span>
            </div>
          </div>
          <div class="card-footer" v-if="item.status === 'PENDING'">
            <van-button size="small" round type="default" @click="handleCancel(item)">取消预约</van-button>
          </div>
          <div class="card-footer" v-if="item.status === 'CONFIRMED'">
            <van-button size="small" round type="warning" @click="goCheckin(item)">去打卡</van-button>
          </div>
        </div>
      </div>
      <div v-else-if="!loading" class="empty-wrap">
        <van-empty description="暂无课程预约记录" />
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, put } from '../../api'
import { showToast, showDialog } from 'vant'

const router = useRouter()
const loading = ref(true)
const refreshing = ref(false)
const list = ref([])

function statusLabel(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
  return map[status] || status
}

async function fetchData() {
  try {
    const res = await get('/course/reservations/my')
    list.value = res.data || []
  } catch {
    list.value = []
  }
  loading.value = false
}

async function handleCancel(item) {
  try {
    await showDialog({
      title: '取消预约',
      message: '确定要取消本次预约吗？',
      showCancelButton: true
    })
    await put(`/course/reservations/${item.id}/cancel`)
    showToast('预约已取消')
    item.status = 'CANCELLED'
  } catch {
    // cancelled
  }
}

function goCheckin(item) {
  router.push('/checkin/' + item.courseId)
}

function onRefresh() {
  refreshing.value = true
  fetchData().finally(() => { refreshing.value = false })
}

onMounted(() => fetchData())
</script>

<style scoped>
.my-reservations-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.reservation-card {
  background: #fff;
  margin: 8px 12px;
  padding: 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.card-header {
  display: flex; gap: 12px;
  margin-bottom: 12px;
}

.course-cover-mini {
  width: 60px; height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.course-cover-mini img {
  width: 100%; height: 100%; object-fit: cover;
}

.header-info {
  flex: 1;
  display: flex; justify-content: space-between; align-items: flex-start;
}

.course-title {
  font-size: 15px; font-weight: 600;
  color: var(--text-color);
  flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-right: 10px;
}

.status-badge {
  font-size: 12px; font-weight: 500;
  padding: 2px 10px; border-radius: 12px;
  flex-shrink: 0;
}

.badge-pENDING { background: #FDF6EC; color: #E6A23C; }
.badge-confirmed { background: #F0F9EB; color: #67C23A; }
.badge-cancelled { background: #F4F4F5; color: #909399; }
.badge-completed { background: #ECF5FF; color: #409EFF; }

.card-body {
  padding-bottom: 10px;
}

.info-row {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: var(--text-secondary);
  margin-top: 6px;
}

.card-footer {
  border-top: 1px solid var(--border-light);
  padding-top: 10px;
  text-align: right;
}
</style>
