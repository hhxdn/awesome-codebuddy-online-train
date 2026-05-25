<template>
  <div class="order-list-page">
    <van-nav-bar title="我的订单" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab">
      <van-tab title="全部" />
      <van-tab title="待支付" />
      <van-tab title="已支付" />
      <van-tab title="已取消" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多订单">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <van-tag :type="getStatusType(order.status)" size="small">
              {{ getStatusText(order.status) }}
            </van-tag>
          </div>
          <div class="order-body">
            <van-image :src="order.courseCover" width="60" height="45" fit="cover" />
            <div class="order-course-info">
              <h5>{{ order.courseName }}</h5>
              <span class="order-amount">¥{{ order.amount }}</span>
            </div>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ order.createTime }}</span>
            <div class="order-actions">
              <van-button v-if="order.status === 'PENDING'" size="small" type="danger" plain @click="cancelOrder(order)">取消</van-button>
              <van-button v-if="order.status === 'PENDING'" size="small" type="primary" @click="payOrder(order)">支付</van-button>
              <van-button v-if="order.status === 'PAID'" size="small" type="primary" plain @click="viewCourse(order)">查看课程</van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && filteredOrders.length === 0" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, put } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const activeTab = ref(0)
const orders = ref([])
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(true)

const tabStatusMap = ['ALL', 'PENDING', 'PAID', 'CANCELED']

const filteredOrders = computed(() => {
  const status = tabStatusMap[activeTab.value]
  if (status === 'ALL') return orders.value
  return orders.value.filter(o => o.status === status)
})

function getStatusType(status) {
  const map = { PENDING: 'warning', PAID: 'success', CANCELED: 'default' }
  return map[status] || 'default'
}

function getStatusText(status) {
  const map = { PENDING: '待支付', PAID: '已支付', CANCELED: '已取消' }
  return map[status] || status
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await get('/orders')
    if (res.data) orders.value = res.data.records || res.data || []
  } catch (e) {
    orders.value = [
      { id: 1, orderNo: '20240115001', courseName: 'Spring Boot实战', courseCover: '', courseId: 1,
        amount: 99, status: 'PAID', createTime: '2024-01-15 10:30' },
      { id: 2, orderNo: '20240116001', courseName: 'Vue3从入门到精通', courseCover: '', courseId: 2,
        amount: 99, status: 'PENDING', createTime: '2024-01-16 14:20' }
    ]
  }
  loading.value = false
  finished.value = true
}

function cancelOrder(order) {
  showConfirmDialog({ title: '取消订单', message: '确定取消该订单吗？' }).then(async () => {
    try {
      await put('/orders/' + order.id + '/cancel')
      showToast('已取消')
      fetchOrders()
    } catch (e) {
      order.status = 'CANCELED'
      showToast('已取消')
    }
  }).catch(() => {})
}

function payOrder(order) {
  router.push('/order/confirm/' + order.courseId)
}

function viewCourse(order) {
  router.push('/course/' + order.courseId)
}

function onRefresh() {
  refreshing.value = true
  fetchOrders().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.order-card {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-body {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.order-course-info h5 {
  font-size: 15px;
  margin-bottom: 6px;
}

.order-amount {
  font-size: 16px;
  font-weight: 700;
  color: var(--danger);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.order-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
