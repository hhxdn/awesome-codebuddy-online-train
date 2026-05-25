<template>
  <div class="profile-page">
    <!-- User Header -->
    <div class="profile-header">
      <div class="header-bg-pattern"></div>
      <van-image round width="72" height="72" :src="user.avatar" class="avatar">
        <template #error>
          <div class="avatar-placeholder">{{ user.nickname?.charAt(0) || 'U' }}</div>
        </template>
      </van-image>
      <div class="nickname">{{ user.nickname || '未登录' }}</div>
      <div class="phone">{{ user.phone || '' }}</div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-icon stat-icon-time">
          <van-icon name="clock-o" size="16" />
        </div>
        <span class="stat-value">{{ stats.studyDuration || 0 }}<small>h</small></span>
        <span class="stat-label">学习时长</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon-course">
          <van-icon name="bookmark-o" size="16" />
        </div>
        <span class="stat-value">{{ stats.courseCount || 0 }}</span>
        <span class="stat-label">学习课程</span>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon-practice">
          <van-icon name="edit" size="16" />
        </div>
        <span class="stat-value">{{ stats.practiceCount || 0 }}</span>
        <span class="stat-label">练习次数</span>
      </div>
    </div>

    <!-- Menu -->
    <div class="menu-section" v-for="(group, gIdx) in menuGroups" :key="gIdx">
      <div class="menu-group-title" v-if="gIdx === 0">学习管理</div>
      <div class="menu-group-title" v-else-if="gIdx === 1">学习记录</div>
      <van-cell-group inset>
        <van-cell
          v-for="item in group"
          :key="item.path"
          :title="item.title"
          is-link
          @click="navigate(item.path)"
          center
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
      <van-button block round plain type="danger" @click="logout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
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
    { title: '我的错题', icon: 'cross', iconClass: 'icon-red', path: '/my-wrong' },
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

.header-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
}

.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.stat-icon-time { background: #e8f4ff; color: var(--primary); }
.stat-icon-course { background: #e6f9ee; color: var(--success); }
.stat-icon-practice { background: #fef3c7; color: var(--warning); }

.stat-value small {
  font-size: 13px;
  font-weight: 400;
  margin-left: 2px;
}

.menu-section {
  margin-top: 12px;
}

.menu-group-title {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 20px 8px;
  font-weight: 500;
}

.menu-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.icon-blue { background: #e8f4ff; color: var(--primary); }
.icon-orange { background: #fff7ed; color: #f97316; }
.icon-red { background: #fef2f2; color: var(--danger); }
.icon-green { background: #f0fdf4; color: var(--success); }
.icon-purple { background: #f5f3ff; color: #8b5cf6; }

.logout-section {
  padding: 32px 16px;
}
</style>
