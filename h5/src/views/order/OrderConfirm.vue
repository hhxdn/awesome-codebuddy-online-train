<template>
  <div class="order-confirm-page page-fade-in">
    <van-nav-bar title="确认订单" left-text="返回" left-arrow @click-left="$router.back()" :border="false" />

    <!-- Course Info -->
    <div class="course-info-card" v-if="course.title">
      <div class="course-cover-wrap">
        <van-image :src="course.cover" width="88" height="60" fit="cover" radius="10">
          <template #error>
            <div class="cover-placeholder">{{ course.title?.charAt(0) }}</div>
          </template>
        </van-image>
      </div>
      <div class="course-detail">
        <h4>{{ course.title }}</h4>
        <div class="course-meta">
          <span class="meta-cat">{{ course.categoryName }}</span>
        </div>
        <span class="course-price">¥{{ course.price }}</span>
      </div>
    </div>

    <!-- Amount -->
    <van-cell-group inset style="margin-top: 12px;">
      <van-cell title="课程价格" :value="'¥' + (course.price || 0)" />
      <van-cell title="优惠">
        <template #value>
          <span class="discount-text">暂无优惠</span>
        </template>
      </van-cell>
      <van-cell title="实付金额" class="total-cell">
        <template #value>
          <span class="total-price">¥{{ course.price || 0 }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Payment Method -->
    <van-cell-group inset title="支付方式" style="margin-top: 12px;">
      <van-cell title="微信支付" clickable @click="payMethod = 'wechat'" center class="pay-cell">
        <template #icon>
          <div class="pay-icon wechat">
            <van-icon name="wechat" size="18" color="#fff" />
          </div>
        </template>
        <template #right-icon>
          <van-radio :name="'wechat'" v-model="payMethod" checked-color="var(--primary)" />
        </template>
      </van-cell>
      <van-cell title="支付宝" clickable @click="payMethod = 'alipay'" center class="pay-cell">
        <template #icon>
          <div class="pay-icon alipay">
            <span style="font-size:15px;font-weight:800;color:#fff;">支</span>
          </div>
        </template>
        <template #right-icon>
          <van-radio :name="'alipay'" v-model="payMethod" checked-color="var(--primary)" />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Pay Button -->
    <div class="pay-action">
      <van-button type="primary" block round size="large" :loading="paying" @click="pay" class="pay-btn">
        确认支付 <span class="btn-amount">¥{{ course.price || 0 }}</span>
      </van-button>
      <p class="pay-agreement">
        支付即表示同意 <a href="#">《付费服务协议》</a>
      </p>
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
    course.value = { id: courseId, title: 'Spring Boot 实战教程', cover: '', price: 99 }
  }
}

async function pay() {
  paying.value = true
  try {
    await post('/orders', { courseId: parseInt(courseId), payMethod: payMethod.value })
    showToast('支付成功')
    setTimeout(() => {
      router.replace('/course/' + courseId)
    }, 1000)
  } catch (e) {
    showToast('支付成功（模拟）')
    setTimeout(() => {
      router.replace('/course/' + courseId)
    }, 1000)
  }
  paying.value = false
}

onMounted(() => {
  fetchCourse()
})
</script>

<style scoped>
.order-confirm-page {
  background: var(--bg-color);
  min-height: 100vh;
}

/* Course Info Card */
.course-info-card {
  background: #fff;
  margin: 8px 12px;
  padding: 16px;
  border-radius: var(--radius);
  display: flex;
  gap: 14px;
  align-items: flex-start;
  box-shadow: var(--shadow-xs);
}

.course-cover-wrap {
  flex-shrink: 0;
}

.cover-placeholder {
  width: 88px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  border-radius: 10px;
}

.course-detail {
  flex: 1;
  min-width: 0;
}

.course-detail h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 6px;
  line-height: 1.4;
}

.course-meta {
  margin-bottom: 8px;
}

.meta-cat {
  font-size: 11px;
  color: var(--primary);
  background: var(--primary-bg);
  padding: 2px 8px;
  border-radius: 4px;
}

.course-price {
  font-size: 20px;
  font-weight: 800;
  color: var(--danger);
}

/* Amount */
.discount-text {
  color: var(--text-muted);
  font-size: 13px;
}

.total-cell {
  font-weight: 700;
}

.total-price {
  font-size: 20px;
  font-weight: 800;
  color: var(--danger);
}

/* Payment Method */
.pay-cell {
  padding: 14px 16px !important;
}

.pay-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}

.pay-icon.wechat {
  background: linear-gradient(135deg, #07c160, #05a048);
}

.pay-icon.alipay {
  background: linear-gradient(135deg, #1677ff, #0958d9);
}

/* Pay Action */
.pay-action {
  padding: 28px 16px;
  text-align: center;
}

.pay-btn {
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important;
  border: none !important;
  box-shadow: 0 8px 24px rgba(79, 110, 247, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition);
}

.pay-btn:active {
  transform: scale(0.97);
  box-shadow: 0 4px 12px rgba(79, 110, 247, 0.3);
}

.btn-amount {
  font-size: 18px;
  font-weight: 800;
}

.pay-agreement {
  margin-top: 16px;
  font-size: 12px;
  color: var(--text-muted);
}

.pay-agreement a {
  color: var(--primary);
  text-decoration: none;
}
</style>
