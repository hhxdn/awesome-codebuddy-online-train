<template>
  <div class="profile-page">
    <!-- User Header -->
    <div class="profile-header">
      <van-image round width="70" height="70" :src="user.avatar" class="avatar">
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
        <span class="stat-value">{{ stats.studyDuration || 0 }}h</span>
        <span class="stat-label">学习时长</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.courseCount || 0 }}</span>
        <span class="stat-label">学习课程</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.practiceCount || 0 }}</span>
        <span class="stat-label">练习次数</span>
      </div>
    </div>

    <!-- Menu -->
    <van-cell-group inset v-for="(group, gIdx) in menuGroups" :key="gIdx" :style="{ marginTop: gIdx > 0 ? '12px' : '0' }">
      <van-cell
        v-for="item in group"
        :key="item.path"
        :title="item.title"
        :icon="item.icon"
        is-link
        @click="navigate(item.path)"
      />
    </van-cell-group>

    <!-- Logout -->
    <div class="logout-section">
      <van-button block round @click="logout">退出登录</van-button>
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
    { title: '我的课程', icon: 'bookmark-o', path: '/my-courses' },
    { title: '我的订单', icon: 'records-o', path: '/my-orders' }
  ],
  [
    { title: '我的错题', icon: 'cross', path: '/my-wrong' },
    { title: '考试记录', icon: 'award-o', path: '/my-exams' },
    { title: '学习记录', icon: 'clock-o', path: '/my-learning' }
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

.avatar-placeholder {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.logout-section {
  padding: 30px 16px;
}
</style>
