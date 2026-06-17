<template>
  <div class="order-list-page page-fade-in">
    <van-nav-bar title="我的订单" :border="false" />

    <van-tabs v-model:active="activeTab" color="#0052D9" title-active-color="#0052D9" sticky>
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.label" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多了 —">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-head">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-status" :class="'os-' + (order.status || '').toLowerCase()">
              {{ statusMap[order.status] || order.status }}
            </span>
          </div>
          <div class="order-body">
            <div class="order-cover">
              <van-image :src="order.courseCover" width="64" height="48" fit="cover" radius="6">
                <template #error>
                  <div class="cover-fb">课</div>
                </template>
              </van-image>
            </div>
            <div class="order-info">
              <h5>{{ order.courseName }}</h5>
              <span class="order-amount">¥{{ order.amount }}</span>
            </div>
          </div>
          <div class="order-foot">
            <span class="order-time">{{ order.createTime }}</span>
            <div class="order-actions">
              <van-button v-if="order.status === 'PENDING'" size="small" round plain type="danger" @click="cancelOrder(order)">取消</van-button>
              <van-button v-if="order.status === 'PENDING'" size="small" round type="primary" @click="payOrder(order)">去支付</van-button>
              <van-button v-if="order.status === 'PAID'" size="small" round plain type="primary" @click="viewCourse(order)">查看课程</van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <div v-if="!loading && !refreshing && filteredOrders.length === 0" class="empty-wrap">
      <EmptyState description="暂无订单" />
    </div>
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

const statusMap = { PENDING: '待支付', PAID: '已支付', CANCELED: '已取消' }

const filteredOrders = computed(() => {
  const s = tabs[activeTab.value]?.key || 'ALL'
  return s === 'ALL' ? orders.value : orders.value.filter(o => o.status === s)
})

async function fetchOrders() {
  loading.value = true
  try {
    const res = await get('/orders')
    if (res.data) orders.value = res.data.records || res.data || []
  } catch (e) { orders.value = [] }
  loading.value = false; finished.value = true
}

function cancelOrder(order) {
  showConfirmDialog({ title: '取消订单', message: '确定取消吗？' }).then(async () => {
    try { await put('/orders/' + order.id + '/cancel'); showToast('已取消') } catch (e) {}
    fetchOrders()
  }).catch(() => {})
}

function payOrder(order) { router.push('/order/confirm/' + order.courseId) }
function viewCourse(order) { router.push('/course/' + order.courseId) }
function onRefresh() { refreshing.value = true; fetchOrders().finally(() => { refreshing.value = false }) }

onMounted(() => fetchOrders())
</script>

<style scoped>
.order-list-page { background: var(--bg-color); min-height: 100vh; }

.order-card {
  background: #fff; margin: 8px 12px; padding: 16px;
  border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.order-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
}
.order-no { font-size: 12px; color: var(--text-muted); font-family: 'SF Mono', Menlo, monospace; }
.order-status { font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 4px; }
.os-pending { color: #ED7B2F; background: var(--warning-light); }
.os-paid { color: #00A870; background: var(--success-light); }
.os-canceled { color: var(--text-muted); background: var(--bg-color); }

.order-body { display: flex; gap: 12px; margin-bottom: 12px; }
.cover-fb {
  width: 64px; height: 48px; border-radius: 6px;
  background: linear-gradient(135deg, #0052D9, #366EF4);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 700;
}
.order-info { flex: 1; }
.order-info h5 { font-size: 14px; font-weight: 500; color: var(--text-color); margin-bottom: 6px; }
.order-amount { font-size: 18px; font-weight: 700; color: var(--danger); }

.order-foot {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 10px; border-top: 1px solid var(--border-light);
}
.order-time { font-size: 12px; color: var(--text-muted); }
.order-actions { display: flex; gap: 8px; }

@media (min-width: 768px) {
  .order-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  }
}
</style>
