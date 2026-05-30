<template>
  <div class="pending-page">
    <div class="content">
      <div class="icon-wrapper">
        <van-icon name="clock-o" size="64" color="#0052D9" />
      </div>
      <h2>资料已提交</h2>
      <p class="desc">已收到您的申请，我们会尽快电话联系您</p>
      <p class="sub-desc">沟通后购买课程即可开通学习账号</p>

      <div class="flow-steps">
        <div class="step">
          <div class="step-num">1</div>
          <div class="step-text">提交资料</div>
        </div>
        <div class="step-arrow">→</div>
        <div class="step">
          <div class="step-num">2</div>
          <div class="step-text">电话沟通</div>
        </div>
        <div class="step-arrow">→</div>
        <div class="step">
          <div class="step-num">3</div>
          <div class="step-text">购买课程</div>
        </div>
        <div class="step-arrow">→</div>
        <div class="step">
          <div class="step-num active">4</div>
          <div class="step-text">开通账号</div>
        </div>
      </div>

      <div class="status-card">
        <van-icon name="info-o" size="18" color="#0052D9" />
        <span>如有疑问，请联系客服电话：400-XXX-XXXX</span>
      </div>

      <van-button
        round
        block
        type="primary"
        :loading="checking"
        @click="checkStatus"
        class="check-btn"
      >
        刷新状态
      </van-button>

      <van-button
        round
        block
        plain
        type="default"
        @click="handleLogout"
        class="logout-btn"
      >
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get } from '../../api'
import { clearAuth } from '../../utils/auth'

const router = useRouter()
const checking = ref(false)

async function checkStatus() {
  checking.value = true
  try {
    const res = await get('/user/check-status')
    if (res.data?.approvalStatus === 'APPROVED') {
      showToast('账号已开通，正在进入')
      setTimeout(() => router.replace('/'), 800)
    } else if (res.data?.approvalStatus === 'REJECTED') {
      showToast('审核未通过，请联系客服')
    } else {
      showToast('仍在审核中，请耐心等待')
    }
  } catch (e) {
    showToast('检查状态失败')
  }
  checking.value = false
}

function handleLogout() {
  clearAuth()
  router.replace('/login')
}
</script>

<style scoped>
.pending-page {
  min-height: 100vh;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content {
  text-align: center;
  padding: 40px 24px;
  max-width: 360px;
}

.icon-wrapper {
  width: 100px;
  height: 100px;
  background: rgba(0, 82, 217, 0.08);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

h2 {
  font-size: 22px;
  font-weight: 700;
  color: #323233;
  margin: 0 0 12px;
}

.desc {
  font-size: 15px;
  color: #646566;
  margin: 0 0 4px;
}

.sub-desc {
  font-size: 13px;
  color: #969799;
  margin: 0 0 28px;
}

.status-card {
  background: rgba(0, 82, 217, 0.06);
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #0052D9;
  margin-bottom: 32px;
  text-align: left;
}

.flow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  gap: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #E5E6EB;
  color: #969799;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.step-num.active {
  background: #0052D9;
  color: #fff;
}

.step-text {
  font-size: 11px;
  color: #969799;
  white-space: nowrap;
}

.step-arrow {
  font-size: 14px;
  color: #C9CDD4;
  margin: 0 2px;
  margin-top: -14px;
}

.check-btn {
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  background: #0052D9 !important;
  border: none !important;
}

.logout-btn {
  height: 44px;
  font-size: 15px;
  color: #969799;
}
</style>
