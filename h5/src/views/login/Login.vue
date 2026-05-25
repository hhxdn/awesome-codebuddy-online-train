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

      <!-- Login/Register Form Card -->
      <div class="login-card">
        <div class="card-header">
          <span
            class="card-tab"
            :class="{ active: activeTab === 'login' }"
            @click="switchTab('login')"
          >登录</span>
          <span
            class="card-tab"
            :class="{ active: activeTab === 'register' }"
            @click="switchTab('register')"
          >注册</span>
        </div>

        <!-- 登录表单 -->
        <van-form v-if="activeTab === 'login'" @submit="onLogin" class="login-form">
          <van-cell-group inset>
            <van-field
              v-model="loginForm.phone"
              name="phone"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="phone-o" size="18" />
                </div>
              </template>
            </van-field>
            <van-field
              v-model="loginForm.password"
              name="password"
              type="password"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="lock" size="18" />
                </div>
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
          </div>
        </van-form>

        <!-- 注册表单 -->
        <van-form v-else @submit="onRegister" class="login-form">
          <van-cell-group inset>
            <van-field
              v-model="registerForm.phone"
              name="phone"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="phone-o" size="18" />
                </div>
              </template>
            </van-field>
            <van-field
              v-model="registerForm.password"
              name="password"
              type="password"
              placeholder="请设置密码（至少6位）"
              :rules="[{ required: true, message: '请设置密码' }, { validator: passwordValidator, message: '密码长度不能少于6位' }]"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="lock" size="18" />
                </div>
              </template>
            </van-field>
            <van-field
              v-model="registerForm.confirmPassword"
              name="confirmPassword"
              type="password"
              placeholder="请确认密码"
              :rules="[{ required: true, message: '请确认密码' }, { validator: confirmPasswordValidator, message: '两次输入的密码不一致' }]"
            >
              <template #left-icon>
                <div class="field-icon">
                  <van-icon name="checked" size="18" />
                </div>
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
              注 册
            </van-button>
          </div>
        </van-form>
      </div>

      <!-- Tip -->
      <div class="login-tip">
        <van-icon name="info-o" size="14" />
        <span>提示：首次使用请先注册账号</span>
      </div>

      <p class="login-footer-text">
        登录即表示同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { setToken, setUser } from '../../utils/auth'
import { post } from '../../api'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({
  phone: '',
  password: ''
})

const registerForm = reactive({
  phone: '',
  password: '',
  confirmPassword: ''
})

function switchTab(tab) {
  activeTab.value = tab
}

// 密码长度校验
function passwordValidator(val) {
  return val.length >= 6
}

// 确认密码一致性校验
function confirmPasswordValidator(val) {
  return val === registerForm.password
}

async function onLogin() {
  loading.value = true
  try {
    const res = await post('/user/login', {
      phone: loginForm.phone,
      password: loginForm.password
    })
    if (res.data && res.data.token) {
      setToken(res.data.token)
      setUser(res.data.user)
      loading.value = false
      showToast('登录成功')
      router.replace('/')
    } else {
      loading.value = false
      showToast(res.message || '登录失败')
    }
  } catch (e) {
    loading.value = false
    showToast(e.message || '登录失败')
  }
}

async function onRegister() {
  if (registerForm.password !== registerForm.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }
  if (registerForm.password.length < 6) {
    showToast('密码长度不能少于6位')
    return
  }

  loading.value = true
  try {
    const res = await post('/user/register', {
      phone: registerForm.phone,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })
    if (res.data && res.data.token) {
      setToken(res.data.token)
      setUser(res.data.user)
      loading.value = false
      showToast('注册成功')
      router.replace('/')
    } else {
      loading.value = false
      showToast(res.message || '注册失败')
    }
  } catch (e) {
    loading.value = false
    showToast(e.message || '注册失败')
  }
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
  display: flex;
  padding: 20px 20px 0;
  gap: 24px;
}

.card-tab {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-muted);
  padding-bottom: 10px;
  cursor: pointer;
  position: relative;
  transition: all var(--transition);
}

.card-tab.active {
  color: var(--text-color);
  font-weight: 700;
}

.card-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
}

.login-form {
  padding-top: 12px;
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

/* Actions */
.login-actions {
  margin-top: 28px;
  padding: 0 20px 28px;
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
