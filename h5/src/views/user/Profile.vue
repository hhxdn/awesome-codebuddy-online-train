<template>
  <div class="profile-page page-fade-in">
    <!-- User Header -->
    <div class="profile-header">
      <div class="header-bg-pattern"></div>
      <div class="header-edit" @click="showToast('编辑资料功能开发中')">
        <van-icon name="setting-o" size="20" color="#fff" />
      </div>
      <van-image round width="72" height="72" :src="user.avatar" class="avatar">
        <template #error>
          <div class="avatar-placeholder">{{ user.nickname?.charAt(0) || 'U' }}</div>
        </template>
      </van-image>
      <div class="nickname">{{ user.nickname || '未登录' }}</div>
      <div class="phone" v-if="user.phone">
        <van-icon name="phone-o" size="13" />
        {{ user.phone }}
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-icon stat-icon-time">
          <van-icon name="clock-o" size="18" />
        </div>
        <span class="stat-value">{{ stats.studyDuration || 0 }}<small>h</small></span>
        <span class="stat-label">学习时长</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon-course">
          <van-icon name="bookmark-o" size="18" />
        </div>
        <span class="stat-value">{{ stats.courseCount || 0 }}</span>
        <span class="stat-label">学习课程</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon-practice">
          <van-icon name="edit" size="18" />
        </div>
        <span class="stat-value">{{ stats.practiceCount || 0 }}</span>
        <span class="stat-label">练习次数</span>
      </div>
    </div>

    <!-- Menu -->
    <div class="menu-section" v-for="(group, gIdx) in menuGroups" :key="gIdx">
      <div class="menu-group-title">{{ gIdx === 0 ? '学习管理' : '学习记录' }}</div>
      <van-cell-group inset>
        <van-cell
          v-for="item in group"
          :key="item.path"
          :title="item.title"
          is-link
          @click="navigate(item.path)"
          center
          class="menu-cell"
        >
          <template #icon>
            <div class="menu-icon" :class="item.iconClass">
              <van-icon :name="item.icon" size="18" />
            </div>
          </template>
          <template #value v-if="item.badge">
            <van-badge :content="item.badge" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- Logout -->
    <div class="logout-section">
      <van-button block round plain type="danger" @click="logout" class="logout-btn">
        <van-icon name="revoke" size="16" />
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
    { title: '我的课程', icon: 'bookmark-o', iconClass: 'icon-blue', path: '/my-courses' },
    { title: '我的订单', icon: 'records-o', iconClass: 'icon-orange', path: '/my-orders' }
  ],
  [
    { title: '错题回顾', icon: 'cross', iconClass: 'icon-red', path: '/my-wrong' },
    { title: '考试记录', icon: 'certificate', iconClass: 'icon-green', path: '/my-exams' },
    { title: '学习记录', icon: 'clock-o', iconClass: 'icon-purple', path: '/my-learning' }
  ]
]

async function fetchStats() {
  try {
    const res = await get('/user/stats')
    if (res.data) stats.value = res.data
  } catch (e) {
    stats.value = { studyDuration: 12, courseCount: 3, practiceCount: 15 }
  }
}

function navigate(path) {
  router.push(path)
}

function logout() {
  showConfirmDialog({
    title: '退出登录',
    message: '确定要退出登录吗？'
  }).then(() => {
    clearAuth()
    router.replace('/login')
  }).catch(() => {})
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
.profile-header {
  background: linear-gradient(160deg, #3a54d4, var(--primary), var(--primary-light));
  padding: 44px 20px 32px;
  text-align: center;
  color: #fff;
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
  position: relative;
  overflow: hidden;
}

.header-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(255,255,255,0.08) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.06) 0%, transparent 50%);
}

.header-edit {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
  color: #fff;
}

.profile-header .nickname {
  font-size: 20px;
  font-weight: 700;
  margin-top: 8px;
  position: relative;
}

.profile-header .phone {
  font-size: 13px;
  opacity: 0.75;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* Stats */
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.stat-icon-time { background: var(--primary-bg); color: var(--primary); }
.stat-icon-course { background: var(--success-light); color: var(--success); }
.stat-icon-practice { background: var(--warning-light); color: var(--warning); }

.stat-value {
  font-size: 24px;
  font-weight: 800;
}

.stat-value small {
  font-size: 14px;
  font-weight: 500;
  margin-left: 2px;
}

/* Menu */
.menu-section {
  margin-top: 14px;
}

.menu-group-title {
  font-size: 13px;
  color: var(--text-muted);
  padding: 0 20px 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.menu-cell {
  padding: 14px 16px !important;
}

.menu-cell :deep(.van-cell__title) {
  font-weight: 500;
}

.menu-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.icon-blue { background: var(--primary-bg); color: var(--primary); }
.icon-orange { background: var(--warning-light); color: #f97316; }
.icon-red { background: var(--danger-light); color: var(--danger); }
.icon-green { background: var(--success-light); color: var(--success); }
.icon-purple { background: #f5f3ff; color: #8b5cf6; }

/* Logout */
.logout-section {
  padding: 36px 20px;
}

.logout-btn {
  height: 46px;
  font-size: 14px;
  font-weight: 500;
  border: 1.5px solid var(--danger) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition);
}

.logout-btn:active {
  background: var(--danger-light) !important;
}
</style>
