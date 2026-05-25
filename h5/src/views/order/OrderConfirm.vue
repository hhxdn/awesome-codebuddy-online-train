<template>
  <div class="order-confirm-page page-fade-in">
    <van-nav-bar title="确认订单" left-text="返回" left-arrow @click-left="$router.back()" :border="false" />

    <div class="course-card" v-if="course.title">
      <div class="course-cover">
        <van-image :src="course.cover" width="88" height="60" fit="cover" radius="8">
          <template #error>
            <div class="cover-ph">{{ course.title?.charAt(0) }}</div>
          </template>
        </van-image>
      </div>
      <div class="course-info">
        <h4>{{ course.title }}</h4>
        <span class="cat-tag">{{ course.categoryName }}</span>
        <span class="price-text">¥{{ course.price }}</span>
      </div>
    </div>

    <van-cell-group inset style="margin-top:12px;">
      <van-cell title="课程价格" :value="'¥' + (course.price || 0)" />
      <van-cell title="优惠" value="暂无优惠" />
      <van-cell title="实付金额" class="total-cell">
        <template #value><span class="total-price">¥{{ course.price || 0 }}</span></template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset title="支付方式" style="margin-top:12px;">
      <van-cell title="微信支付" clickable @click="payMethod = 'wechat'" center>
        <template #icon>
          <div class="pay-icon pay-wx"><van-icon name="wechat" size="18" color="#fff" /></div>
        </template>
        <template #right-icon>
          <van-radio name="wechat" v-model="payMethod" checked-color="#0052D9" />
        </template>
      </van-cell>
      <van-cell title="支付宝" clickable @click="payMethod = 'alipay'" center>
        <template #icon>
          <div class="pay-icon pay-ali"><span style="font-size:15px;font-weight:700;color:#fff;">支</span></div>
        </template>
        <template #right-icon>
          <van-radio name="alipay" v-model="payMethod" checked-color="#0052D9" />
        </template>
      </van-cell>
    </van-cell-group>

    <div class="pay-action">
      <van-button type="primary" block round size="large" :loading="paying" @click="pay" class="pay-btn">
        确认支付 <span class="btn-price">¥{{ course.price || 0 }}</span>
      </van-button>
      <p class="pay-legal">支付即表示同意 <a href="#">《付费服务协议》</a></p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const course = ref({})
const payMethod = ref('wechat')
const paying = ref(false)

async function fetchCourse() {
  try {
    const res = await get('/courses/' + courseId)
    if (res.data) course.value = res.data
  } catch (e) {
    course.value = { id: courseId, title: '加载中...', price: 0 }
  }
}

async function pay() {
  paying.value = true
  try {
    await post('/orders', { courseId: parseInt(courseId), payMethod: payMethod.value })
  } catch (e) {}
  showToast('支付成功')
  setTimeout(() => router.replace('/course/' + courseId), 800)
  paying.value = false
}

onMounted(() => fetchCourse())
</script>

<style scoped>
.order-confirm-page { background: var(--bg-color); min-height: 100vh; }

.course-card {
  background: #fff; margin: 8px 12px; padding: 16px;
  border-radius: 10px; display: flex; gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.cover-ph {
  width: 88px; height: 60px; border-radius: 8px;
  background: linear-gradient(135deg, #0052D9, #366EF4);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700;
}
.course-info { flex: 1; min-width: 0; }
.course-info h4 { font-size: 15px; font-weight: 500; color: var(--text-color); margin-bottom: 6px; }
.cat-tag {
  display: inline-block; font-size: 11px; color: var(--primary);
  background: var(--primary-bg); padding: 2px 8px; border-radius: 4px;
  margin-bottom: 6px;
}
.price-text { font-size: 20px; font-weight: 700; color: var(--danger); display: block; }

.total-cell :deep(.van-cell__value) { font-weight: 600; }
.total-price { font-size: 20px; font-weight: 700; color: var(--danger); }

.pay-icon {
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  margin-right: 12px;
}
.pay-wx { background: #07C160; }
.pay-ali { background: #1677FF; }

.pay-action { padding: 32px 16px; text-align: center; }
.pay-btn {
  height: 50px; font-size: 16px; font-weight: 600; letter-spacing: 1px;
  background: var(--primary) !important; border: none !important;
  box-shadow: 0 6px 20px rgba(0,82,217,0.35);
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-price { font-size: 18px; font-weight: 700; }
.pay-btn:active { transform: scale(0.97); }
.pay-legal { margin-top: 16px; font-size: 12px; color: var(--text-muted); }
.pay-legal a { color: var(--primary); text-decoration: none; }
</style>
