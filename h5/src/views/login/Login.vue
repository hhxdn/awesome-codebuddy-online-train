<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shapes">
        <div class="shape s1"></div>
        <div class="shape s2"></div>
        <div class="shape s3"></div>
      </div>
    </div>

    <div class="login-content">
      <!-- Logo -->
      <div class="login-logo">
        <div class="logo-circle">
          <svg viewBox="0 0 48 48" width="44" height="44" fill="none">
            <rect width="48" height="48" rx="14" fill="white" opacity="0.15"/>
            <path d="M14 20L24 12L34 20V32C34 33.1 33.1 34 32 34H16C14.9 34 14 33.1 14 32V20Z" stroke="white" stroke-width="2.5" fill="none"/>
            <path d="M21 34V25H27V34" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="20" r="3" fill="white" opacity="0.6"/>
          </svg>
        </div>
        <h1 class="app-name">在线学习平台</h1>
        <p class="app-slogan">随时随地，提升自我</p>
      </div>

      <!-- Login Form Card -->
      <div class="login-card">
        <div class="card-header">
          <span class="card-tab active">手机登录</span>
        </div>

        <van-form @submit="onLogin" class="login-form">
          <van-cell-group inset>
            <van-field
              v-model="phone"
              name="phone"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
              class="phone-field"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="phone-o" size="18" />
                </div>
              </template>
            </van-field>
            <van-field
              v-model="code"
              name="code"
              placeholder="请输入验证码"
              maxlength="6"
              type="digit"
              :rules="[{ required: true, message: '请输入验证码' }]"
              class="code-field"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="shield-o" size="18" />
                </div>
              </template>
              <template #button>
                <van-button
                  size="small"
                  plain
                  type="primary"
                  :disabled="countdown > 0"
                  @click="sendCode"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <div class="login-actions">
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              class="login-btn"
            >
              登 录
            </van-button>

            <div class="divider-row">
              <span class="divider-line"></span>
              <span class="divider-text">其他方式登录</span>
              <span class="divider-line"></span>
            </div>

            <van-button
              round
              block
              class="wechat-btn"
              @click="wechatLogin"
            >
              <van-icon name="wechat" size="20" />
              微信一键登录
            </van-button>
          </div>
        </van-form>
      </div>

      <!-- Tip -->
      <div class="login-tip">
        <van-icon name="info-o" size="14" />
        <span>提示：任意手机号 + 验证码 123456 即可登录</span>
      </div>

      <p class="login-footer-text">
        登录即表示同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { setToken, setUser } from '../../utils/auth'
import { post } from '../../api'

const router = useRouter()
const phone = ref('')
const code = ref('')
const countdown = ref(0)
const loading = ref(false)

function sendCode() {
  if (!phone.value) {
    showToast('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    showToast('请输入正确的手机号')
    return
  }
  showToast('验证码已发送（模拟）')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

async function onLogin() {
  if (code.value !== '123456') {
    showToast('验证码错误（模拟：请输入123456）')
    return
  }

  loading.value = true
  try {
    const res = await post('/user/login', {
      phone: phone.value,
      code: code.value
    })
    if (res.data && res.data.token) {
      setToken(res.data.token)
      setUser(res.data.user)
      loading.value = false
      showToast('登录成功')
      router.replace('/')
    } else {
      loading.value = false
      showToast('登录失败')
    }
  } catch (e) {
    loading.value = false
  }
}

function wechatLogin() {
  showToast('微信登录功能开发中')
  setToken('mock_wechat_token_' + Date.now())
  setUser({
    id: 2,
    nickname: '微信用户',
    phone: '',
    avatar: ''
  })
  showToast('微信登录成功')
  setTimeout(() => router.replace('/'), 500)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* Background */
.login-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(160deg, #3a54d4 0%, #4f6ef7 30%, #7b94fa 60%, #a5b4fc 100%);
  z-index: 0;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.shape.s1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -60px;
}

.shape.s2 {
  width: 160px;
  height: 160px;
  top: 35%;
  left: -40px;
}

.shape.s3 {
  width: 120px;
  height: 120px;
  bottom: 10%;
  right: 15%;
}

/* Content */
.login-content {
  position: relative;
  z-index: 1;
  padding: 50px 20px 40px;
  max-width: 420px;
  margin: 0 auto;
}

/* Logo */
.login-logo {
  text-align: center;
  margin-bottom: 36px;
}

.logo-circle {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.app-name {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 6px;
  font-weight: 400;
}

/* Login Card */
.login-card {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 0 0 8px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.card-header {
  padding: 20px 20px 8px;
}

.card-tab {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-color);
  padding-bottom: 8px;
  border-bottom: 2px solid var(--primary);
}

.login-form {
  padding-top: 4px;
}

.login-form :deep(.van-cell-group--inset) {
  margin: 0;
}

.field-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  margin-right: 8px;
}

.login-form :deep(.van-field) {
  padding: 14px 20px;
  font-size: 15px;
}

.login-form :deep(.van-field__left-icon) {
  margin-right: 0;
}

.phone-field :deep(.van-field__body) {
  border-bottom: 1px solid var(--border-color);
}

/* Actions */
.login-actions {
  margin-top: 28px;
  padding: 0 20px;
}

.login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 6px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(79, 110, 247, 0.4);
  transition: all var(--transition);
}

.login-btn:active {
  transform: scale(0.98);
}

.divider-row {
  display: flex;
  align-items: center;
  margin: 24px 0;
  gap: 14px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.divider-text {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
}

.wechat-btn {
  height: 46px;
  font-size: 14px;
  font-weight: 600;
  background: var(--success-light) !important;
  border: 1.5px solid var(--success) !important;
  color: #16a34a !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all var(--transition);
}

.wechat-btn:active {
  transform: scale(0.98);
  background: #dcfce7 !important;
}

.send-code-btn {
  border-radius: 20px !important;
  font-size: 12px !important;
  height: 32px !important;
  font-weight: 500;
}

/* Tip & Footer */
.login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 24px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 10px 18px;
  backdrop-filter: blur(8px);
  max-width: 320px;
  margin-left: auto;
  margin-right: auto;
}

.login-footer-text {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.login-footer-text a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
}
</style>
