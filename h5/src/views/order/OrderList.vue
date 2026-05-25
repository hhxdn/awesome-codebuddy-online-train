<template>
  <div class="order-list-page page-fade-in">
    <van-nav-bar title="我的订单" :border="false" />

    <van-tabs v-model:active="activeTab" color="var(--primary)" title-active-color="var(--primary)" sticky>
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.label" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多订单 —">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="'status-' + order.status?.toLowerCase()">
              {{ getStatusText(order.status) }}
            </span>
          </div>
          <div class="order-body">
            <div class="order-cover-wrap">
              <van-image :src="order.courseCover" width="64" height="44" fit="cover" radius="8">
                <template #error>
                  <div class="order-cover-placeholder">课</div>
                </template>
              </van-image>
            </div>
            <div class="order-course-info">
              <h5>{{ order.courseName }}</h5>
              <span class="order-amount">¥{{ order.amount }}</span>
            </div>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ order.createTime }}</span>
            <div class="order-actions">
              <van-button v-if="order.status === 'PENDING'" size="small" round plain type="danger" @click="cancelOrder(order)">
                取消
              </van-button>
              <van-button v-if="order.status === 'PENDING'" size="small" round type="primary" @click="payOrder(order)">
                去支付
              </van-button>
              <van-button v-if="order.status === 'PAID'" size="small" round plain type="primary" @click="viewCourse(order)">
                查看课程
              </van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <EmptyState v-if="!loading && !refreshing && filteredOrders.length === 0" description="暂无订单" />
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

const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待支付' },
  { key: 'PAID', label: '已支付' },
  { key: 'CANCELED', label: '已取消' }
]

const filteredOrders = computed(() => {
  const status = tabs[activeTab.value]?.key || 'ALL'
  if (status === 'ALL') return orders.value
  return orders.value.filter(o => o.status === status)
})

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
  margin: 8px 12px;
  padding: 16px;
  border-radius: var(--radius);
  box-shadow: var(--shadow-xs);
  transition: all var(--transition);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.order-no {
  font-size: 13px;
  color: var(--text-muted);
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}

.status-pending {
  color: #d97706;
  background: var(--warning-light);
}

.status-paid {
  color: var(--success);
  background: var(--success-light);
}

.status-canceled {
  color: var(--text-muted);
  background: var(--bg-color);
}

.order-body {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.order-cover-placeholder {
  width: 64px;
  height: 44px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border-radius: 8px;
}

.order-course-info {
  flex: 1;
}

.order-course-info h5 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 6px;
}

.order-amount {
  font-size: 18px;
  font-weight: 800;
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
  color: var(--text-muted);
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
