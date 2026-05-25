<template>
  <div class="login-page">
    <div class="login-logo">
      <div class="logo-icon">📚</div>
      <h2>在线学习平台</h2>
      <p>随时随地，提升自我</p>
    </div>

    <div class="login-form">
      <van-form @submit="onLogin">
        <van-cell-group inset>
          <van-field
            v-model="phone"
            name="phone"
            label="手机号"
            type="tel"
            placeholder="请输入手机号"
            :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
          />
          <van-field
            v-model="code"
            name="code"
            label="验证码"
            placeholder="请输入验证码"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <van-button
                size="small"
                type="primary"
                :disabled="countdown > 0"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
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
          >
            登录
          </van-button>

          <van-button
            round
            block
            class="wechat-btn"
            @click="wechatLogin"
          >
            微信一键登录
          </van-button>
        </div>
      </van-form>
    </div>

    <p class="login-tip">提示：任意手机号 + 验证码 123456 即可登录</p>
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
  // Mock wechat login
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
  background: #fff;
  padding: 40px 20px;
}

.login-logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.login-logo h2 {
  font-size: 24px;
  color: var(--primary);
  margin-bottom: 8px;
}

.login-logo p {
  font-size: 14px;
  color: var(--text-secondary);
}

.login-form {
  margin-top: 20px;
}

.login-actions {
  margin-top: 30px;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wechat-btn {
  background: #07c160 !important;
  border-color: #07c160 !important;
  color: #fff !important;
}

.login-tip {
  text-align: center;
  margin-top: 30px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
