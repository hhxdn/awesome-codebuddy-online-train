<template>
  <div class="profile-page page-fade-in">
    <!-- Header -->
    <div class="profile-header">
      <div class="header-setting" @click="showToast('个人设置')">
        <van-icon name="setting-o" size="20" color="rgba(255,255,255,0.7)" />
      </div>
      <van-image round width="68" height="68" :src="user.avatar" class="avatar">
        <template #error>
          <div class="avatar-fallback">{{ user.nickname?.charAt(0) || 'U' }}</div>
        </template>
      </van-image>
      <div class="nickname">{{ user.nickname || '未设置昵称' }}</div>
      <div class="phone-line" v-if="user.phone">
        <van-icon name="phone-o" size="12" /> {{ formatPhone(user.phone) }}
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-icon s-time"><van-icon name="clock-o" size="18" /></div>
        <span class="stat-value">{{ stats.studyDuration || 0 }}<small>h</small></span>
        <span class="stat-label">学习时长</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon s-course"><van-icon name="bookmark-o" size="18" /></div>
        <span class="stat-value">{{ stats.courseCount || 0 }}</span>
        <span class="stat-label">在学课程</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon s-practice"><van-icon name="edit" size="18" /></div>
        <span class="stat-value">{{ stats.practiceCount || 0 }}</span>
        <span class="stat-label">练习次数</span>
      </div>
    </div>

    <!-- Menu -->
    <div class="menu-section" v-for="(group, gIdx) in menuGroups" :key="gIdx">
      <div class="menu-label">{{ gIdx === 0 ? '学习管理' : gIdx === 1 ? '学习记录' : '其他' }}</div>
      <van-cell-group inset>
        <van-cell
          v-for="item in group" :key="item.path"
          :title="item.title" is-link @click="navigate(item.path)"
          class="menu-cell"
        >
          <template #icon>
            <div class="menu-icon" :class="item.iconClass">
              <van-icon :name="item.icon" size="18" />
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- Logout -->
    <div class="logout-wrap">
      <van-button block round plain type="danger" @click="logout" class="logout-btn">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getUser, clearAuth } from '../../utils/auth'
import { get } from '../../api'

const router = useRouter()
const user = ref({})
const stats = ref({})

const menuGroups = [
  [
    { title: '我的课程', icon: 'bookmark-o', iconClass: 'm-blue', path: '/my-courses' },
    { title: '我的练习', icon: 'edit', iconClass: 'm-indigo', path: '/practice-courses' },
    { title: '我的订单', icon: 'records-o', iconClass: 'm-orange', path: '/my-orders' },
    { title: '我的预约', icon: 'location-o', iconClass: 'm-teal', path: '/my-reservations' },
    { title: '课程预约', icon: 'bookmark-o', iconClass: 'm-cyan', path: '/my-course-reservations' }
  ],
  [
    { title: '错题回顾', icon: 'cross', iconClass: 'm-red', path: '/my-wrong' },
    { title: '考试记录', icon: 'certificate', iconClass: 'm-green', path: '/my-exams' },
    { title: '学习记录', icon: 'clock-o', iconClass: 'm-purple', path: '/my-learning' },
    { title: '我的证书', icon: 'medal-o', iconClass: 'm-gold', path: '/my-certificates' }
  ],
  [
    { title: '关于我们', icon: 'info-o', iconClass: 'm-gray', path: '/about' }
  ]
]

function formatPhone(phone) {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

async function fetchStats() {
  try {
    const res = await get('/user/stats')
    if (res.data) stats.value = res.data
  } catch (e) {
    stats.value = { studyDuration: 0, courseCount: 0, practiceCount: 0 }
  }
}

function navigate(path) { router.push(path) }

function logout() {
  showConfirmDialog({ title: '退出登录', message: '确定要退出吗？' })
    .then(() => { clearAuth(); router.replace('/login') })
    .catch(() => {})
}

onMounted(() => {
  user.value = getUser() || {}
  fetchStats()
})
</script>

<style scoped>
.profile-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 50px;
}

/* Header */
.avatar-fallback {
  width: 68px; height: 68px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; font-weight: 700; color: #fff;
}

.header-setting {
  position: absolute; top: 16px; right: 16px;
  width: 36px; height: 36px; border-radius: 50%;
  background: rgba(255,255,255,0.1);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; z-index: 1;
}

.phone-line {
  font-size: 13px; opacity: 0.7; margin-top: 4px;
  display: flex; align-items: center; justify-content: center; gap: 4px;
}

/* Stats Icons */
.stat-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}
.s-time { background: var(--primary-bg); color: var(--primary); }
.s-course { background: var(--success-light); color: #00A870; }
.s-practice { background: var(--warning-light); color: #ED7B2F; }

.stat-value { font-size: 22px; font-weight: 700; }
.stat-value small { font-size: 13px; font-weight: 500; margin-left: 2px; }

/* Menu */
.menu-section { margin-top: 14px; }
.menu-label { font-size: 12px; color: var(--text-muted); padding-left: 20px; padding-right: 20px; padding-bottom: 8px; font-weight: 500; }
.menu-cell { padding: 14px 16px !important; }
.menu-cell :deep(.van-cell__title) { font-weight: 400; }
.menu-icon {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  margin-right: 12px;
}
.m-blue { background: var(--primary-bg); color: var(--primary); }
.m-orange { background: var(--warning-light); color: #ED7B2F; }
.m-red { background: var(--danger-light); color: #E34D59; }
.m-green { background: var(--success-light); color: #00A870; }
.m-purple { background: #F3F0FF; color: #7C3AED; }
.m-gold { background: #FFF7E6; color: #E37318; }
.m-teal { background: #E6FFFA; color: #00B4D8; }
.m-cyan { background: #E0F7FA; color: #0097A7; }
.m-indigo { background: #EEF2FF; color: #4F46E5; }
.m-gray { background: #F5F5F5; color: #666; }

.logout-wrap { padding: 40px 20px; }
.logout-btn {
  height: 46px; font-size: 14px; border: 1px solid #E34D59 !important;
  color: var(--text-secondary) !important;
}

/* ========================================
   Desktop (>=768px) overrides
   ======================================== */
@media (min-width: 768px) {
  .profile-page {
    padding-bottom: 0;
  }

  /* Profile header */
  .profile-page :deep(.profile-header) {
    padding: 48px 40px 40px;
    border-radius: 0 0 16px 16px;
  }

  /* Settings icon */
  .header-setting {
    right: 20px;
    top: 24px;
  }

  /* Stats row */
  .profile-page :deep(.stats-row) {
    max-width: 700px;
    margin: -16px auto 0;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow);
    position: relative;
    z-index: 2;
    padding: 24px 40px;
  }

  /* Fix divider position for wider stats row */
  .profile-page :deep(.stat-item:not(:last-child)::after) {
    right: -50px;
  }

  /* Logout - centered card */
  .logout-wrap {
    max-width: 400px;
    margin: 40px auto;
    padding: 0 20px;
  }
}

/* Large desktop (>=1200px) */
@media (min-width: 1200px) {
  .profile-page :deep(.stats-row) {
    max-width: 800px;
  }
}
</style>
