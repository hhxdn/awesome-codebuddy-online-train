<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-blur b1"></div>
      <div class="bg-blur b2"></div>
    </div>

    <div class="login-content">
      <!-- Brand -->
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 40 40" width="40" height="40" fill="none">
            <rect width="40" height="40" rx="10" fill="#0052D9"/>
            <path d="M12 17L20 10L28 17V27C28 28.1 27.1 29 26 29H14C12.9 29 12 28.1 12 27V17Z" stroke="white" stroke-width="2" fill="none"/>
            <path d="M17 29V21H23V29" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="app-name">在线学习</h1>
        <p class="app-desc">系统化学习，提升专业技能</p>
      </div>

      <!-- Form Card -->
      <div class="login-card">
        <div class="card-tabs">
          <span class="tab" :class="{ active: activeTab === 'login' }" @click="switchTab('login')">登录</span>
          <span class="tab" :class="{ active: activeTab === 'register' }" @click="switchTab('register')">注册</span>
        </div>

        <!-- Login Form -->
        <van-form v-if="activeTab === 'login'" @submit="onLogin">
          <van-cell-group inset>
            <van-field
              v-model="loginForm.phone" name="phone" type="tel" maxlength="11"
              placeholder="手机号"
              left-icon="phone-o"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
            />
            <van-field
              v-model="loginForm.password" name="password" type="password"
              placeholder="密码"
              left-icon="lock"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
          </van-cell-group>
          <div class="form-action">
            <van-button round block type="primary" native-type="submit" :loading="loading" class="action-btn">
              登录
            </van-button>
          </div>
        </van-form>

        <!-- Register Form -->
        <van-form v-else @submit="onRegister">
          <van-cell-group inset>
            <van-field
              v-model="registerForm.phone" name="phone" type="tel" maxlength="11"
              placeholder="手机号"
              left-icon="phone-o"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
            />
            <van-field
              v-model="registerForm.realName" name="realName"
              placeholder="真实姓名"
              left-icon="user-o"
              :rules="[{ required: true, message: '请输入真实姓名' }]"
            />
            <van-field
              v-model="registerForm.gender"
              name="gender"
              placeholder="请选择性别"
              left-icon="friends-o"
              :rules="[{ required: true, message: '请选择性别' }]"
              is-link readonly clickable
              @click-input="showGenderPicker = true"
            />
            <van-field
              v-model="registerForm.age"
              name="age"
              type="digit"
              placeholder="年龄"
              left-icon="birthday-cake-o"
              :rules="[{ required: true, message: '请输入年龄' }]"
            />
            <van-field
              v-model="registerForm.education"
              name="education"
              placeholder="请选择学历"
              left-icon="certificate"
              :rules="[{ required: true, message: '请选择学历' }]"
              is-link readonly clickable
              @click-input="showEducationPicker = true"
            />
            <van-field
              v-model="registerForm.major"
              name="major"
              placeholder="专业"
              left-icon="bookmark-o"
              :rules="[{ required: true, message: '请输入专业' }]"
            />
            <van-field
              v-model="registerForm.password" name="password" type="password"
              placeholder="设置密码（至少6位）"
              left-icon="lock"
              :rules="[{ required: true, message: '请设置密码' }, { validator: pwdValidator, message: '密码至少6位' }]"
            />
            <van-field
              v-model="registerForm.confirmPassword" name="confirmPassword" type="password"
              placeholder="确认密码"
              left-icon="checked"
              :rules="[{ required: true, message: '请确认密码' }, { validator: cpwdValidator, message: '两次密码不一致' }]"
            />
          </van-cell-group>
          <div class="form-action">
            <van-button round block type="primary" native-type="submit" :loading="loading" class="action-btn">
              注册
            </van-button>
          </div>
        </van-form>
      </div>

      <!-- 性别选择 -->
      <van-action-sheet
        v-model:show="showGenderPicker"
        :actions="genderActions"
        @select="onGenderSelect"
        cancel-text="取消"
      />

      <!-- 学历选择 -->
      <van-action-sheet
        v-model:show="showEducationPicker"
        :actions="educationActions"
        @select="onEducationSelect"
        cancel-text="取消"
      />

      <p class="login-agreement">登录即表示同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { setToken, setUser } from '../../utils/auth'
import { post } from '../../api'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const showGenderPicker = ref(false)
const showEducationPicker = ref(false)

const loginForm = reactive({ phone: '', password: '' })
const registerForm = reactive({
  phone: '', realName: '', gender: '', age: '', education: '', major: '',
  password: '', confirmPassword: ''
})

const genderActions = [{ name: '男' }, { name: '女' }]
const educationActions = [
  { name: '高中' }, { name: '中专' }, { name: '大专' },
  { name: '本科' }, { name: '硕士' }, { name: '博士' }, { name: '其他' }
]

function switchTab(tab) { activeTab.value = tab }
function pwdValidator(val) { return val.length >= 6 }
function cpwdValidator(val) { return val === registerForm.password }

function onGenderSelect(action) {
  registerForm.gender = action.name
  showGenderPicker.value = false
}
function onEducationSelect(action) {
  registerForm.education = action.name
  showEducationPicker.value = false
}

async function onLogin() {
  loading.value = true
  try {
    const res = await post('/user/login', { phone: loginForm.phone, password: loginForm.password })
    if (res.data?.token) {
      setToken(res.data.token); setUser(res.data.user)
      showToast('登录成功')
      const approvalStatus = res.data?.approvalStatus || 'APPROVED'
      const hasProfile = res.data?.user?.realName != null && res.data?.user?.realName !== ''
      if (approvalStatus === 'PENDING') {
        router.replace(hasProfile ? '/pending-approval' : '/register-profile')
      } else {
        router.replace('/')
      }
    } else { showToast(res.message || '登录失败') }
  } catch (e) { showToast(e.message || '登录失败') }
  loading.value = false
}

async function onRegister() {
  if (registerForm.password !== registerForm.confirmPassword) { showToast('两次密码不一致'); return }
  if (registerForm.password.length < 6) { showToast('密码至少6位'); return }
  loading.value = true
  try {
    const res = await post('/user/register', {
      phone: registerForm.phone,
      realName: registerForm.realName,
      gender: registerForm.gender,
      age: registerForm.age,
      education: registerForm.education,
      major: registerForm.major,
      contactPhone: registerForm.phone,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })
    if (res.data?.token) {
      setToken(res.data.token); setUser(res.data.user)
      showToast('注册成功')
      router.replace('/')
    } else { showToast(res.message || '注册失败') }
  } catch (e) {
    showDialog({ title: '注册失败', message: e.message || '请稍后重试', confirmButtonText: '知道了' })
  }
  loading.value = false
}
</script>

<style scoped>
.login-page { min-height: 100vh; position: relative; overflow: hidden; }
.login-bg {
  position: fixed; inset: 0;
  background: linear-gradient(160deg, #003CAB 0%, #0052D9 40%, #366EF4 100%);
  z-index: 0;
}
.bg-blur {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,0.05);
}
.b1 { width: 260px; height: 260px; top: -80px; right: -60px; }
.b2 { width: 160px; height: 160px; bottom: 10%; left: -40px; }

.login-content {
  position: relative; z-index: 1;
  padding-top: 56px; padding-left: 20px; padding-right: 20px; padding-bottom: 40px; max-width: 420px; margin: 0 auto;
}

.login-brand { text-align: center; margin-bottom: 40px; }
.brand-icon { margin-bottom: 14px; display: inline-block; }
.app-name { font-size: 26px; font-weight: 700; color: #fff; letter-spacing: 1px; }
.app-desc { font-size: 14px; color: rgba(255,255,255,0.6); margin-top: 6px; }

.login-card {
  background: #fff; border-radius: 16px; padding-bottom: 4px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
  overflow: hidden;
}
.card-tabs { display: flex; padding-top: 20px; padding-left: 20px; padding-right: 20px; gap: 20px; }
.tab {
  font-size: 17px; font-weight: 500; color: var(--text-muted);
  padding-bottom: 10px; cursor: pointer; position: relative;
}
.tab.active { color: var(--text-color); font-weight: 700; }
.tab.active::after {
  content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 24px; height: 3px; border-radius: 2px; background: var(--primary);
}
.form-action { padding: 28px 20px; }
.action-btn {
  height: 48px; font-size: 16px; font-weight: 600; letter-spacing: 2px;
  background: var(--primary) !important; border: none !important;
  box-shadow: 0 4px 16px rgba(0,82,217,0.3);
}
.action-btn:active { transform: scale(0.98); }

.login-agreement {
  text-align: center; margin-top: 24px; font-size: 12px; color: rgba(255,255,255,0.5);
}
.login-agreement a { color: rgba(255,255,255,0.7); text-decoration: none; }

:deep(.van-cell-group--inset) { margin: 0 !important; }
:deep(.van-field__left-icon) { margin-right: 8px; color: var(--text-placeholder); }
</style>
