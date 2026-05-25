<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-content">
      <div class="login-logo">
        <div class="logo-circle">
          <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
            <rect width="48" height="48" rx="12" fill="white" opacity="0.2"/>
            <path d="M14 20L24 12L34 20V32C34 33.1 33.1 34 32 34H16C14.9 34 14 33.1 14 32V20Z" stroke="white" stroke-width="2.5" fill="none"/>
            <path d="M21 34V25H27V34" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="app-name">在线学习平台</h1>
        <p class="app-slogan">随时随地，提升自我</p>
      </div>

      <div class="login-card">
        <van-form @submit="onLogin">
          <van-cell-group inset>
            <van-field
              v-model="phone"
              name="phone"
              label="手机号"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
            >
              <template #left-icon>
                <van-icon name="phone-o" size="18" />
              </template>
            </van-field>
            <van-field
              v-model="code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              maxlength="6"
              type="digit"
              :rules="[{ required: true, message: '请输入验证码' }]"
            >
              <template #left-icon>
                <van-icon name="shield-o" size="18" />
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
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
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
              <van-icon name="wechat" size="18" />
              微信一键登录
            </van-button>
          </div>
        </van-form>
      </div>

      <p class="login-tip">
        <van-icon name="info-o" />
        提示：任意手机号 + 验证码 123456 即可登录
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

.login-bg {
  position: absolute;
  top: -100px;
  left: 0;
  right: 0;
  height: 420px;
  background: linear-gradient(160deg, var(--primary-dark), var(--primary), var(--primary-light));
  border-radius: 0 0 40% 40%;
}

.login-content {
  position: relative;
  z-index: 1;
  padding: 60px 20px 40px;
}

.login-logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255,255,255,0.3), rgba(255,255,255,0.1));
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.app-name {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.app-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 8px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-card :deep(.van-cell-group--inset) {
  margin: 0;
}

.login-actions {
  margin-top: 28px;
  padding: 0 16px;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  box-shadow: 0 4px 12px rgba(79, 110, 247, 0.4);
}

.divider-row {
  display: flex;
  align-items: center;
  margin: 24px 0;
  gap: 12px;
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
  height: 44px;
  font-size: 14px;
  font-weight: 500;
  background: #f0fdf4 !important;
  border: 1.5px solid #22c55e !important;
  color: #16a34a !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.send-code-btn {
  border-radius: 20px !important;
  font-size: 12px !important;
  height: 30px !important;
}

.login-tip {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
</style>
