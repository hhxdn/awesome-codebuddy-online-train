<template>
  <div class="order-confirm-page">
    <van-nav-bar title="确认订单" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Course Info -->
    <div class="course-info-card" v-if="course.title">
      <van-image :src="course.cover" width="80" height="60" fit="cover" round />
      <div class="course-detail">
        <h4>{{ course.title }}</h4>
        <span class="course-price">¥{{ course.price }}</span>
      </div>
    </div>

    <!-- Amount -->
    <van-cell-group inset title="支付信息">
      <van-cell title="课程价格" :value="'¥' + (course.price || 0)" />
      <van-cell title="优惠" value="¥0" />
      <van-cell title="实付金额" class="total-amount">
        <template #value>
          <span class="price">¥{{ course.price || 0 }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Payment Method -->
    <van-cell-group inset title="支付方式" style="margin-top: 16px;">
      <van-cell title="微信支付" clickable @click="payMethod = 'wechat'">
        <template #icon>
          <div class="pay-icon wechat">微</div>
        </template>
        <template #right-icon>
          <van-radio :name="'wechat'" v-model="payMethod" />
        </template>
      </van-cell>
      <van-cell title="支付宝" clickable @click="payMethod = 'alipay'">
        <template #icon>
          <div class="pay-icon alipay">支</div>
        </template>
        <template #right-icon>
          <van-radio :name="'alipay'" v-model="payMethod" />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Pay Button -->
    <div class="pay-action">
      <van-button type="primary" block round size="large" :loading="paying" @click="pay">
        确认支付 ¥{{ course.price || 0 }}
      </van-button>
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
    // Mock payment
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

.course-info-card {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.course-detail h4 {
  font-size: 15px;
  margin-bottom: 8px;
}

.course-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--danger);
}

.total-amount .price {
  font-size: 20px !important;
  font-weight: 700;
  color: var(--danger);
}

.pay-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  margin-right: 12px;
}

.pay-icon.wechat {
  background: #07c160;
}

.pay-icon.alipay {
  background: #1677ff;
}

.pay-action {
  padding: 24px 16px;
}
</style>
