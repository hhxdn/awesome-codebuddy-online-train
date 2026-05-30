<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-container">
        <img src="/vite.svg" alt="logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-title">在线学习管理平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/categories">课程分类</el-menu-item>
          <el-menu-item index="/courses">课程管理</el-menu-item>
          <el-menu-item index="/questions">题库管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exam">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>考试管理</span>
          </template>
          <el-menu-item index="/exams">试卷管理</el-menu-item>
          <el-menu-item index="/exams/random">随机组卷</el-menu-item>
          <el-menu-item index="/exams/records">考试记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/students">学员管理</el-menu-item>
          <el-menu-item index="/checkins">线下打卡</el-menu-item>
          <el-menu-item index="/certificates">结业证书</el-menu-item>
          <el-menu-item index="/orders">订单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="stats">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </template>
          <el-menu-item index="/statistics/revenue">营收统计</el-menu-item>
          <el-menu-item index="/statistics/learning">学情统计</el-menu-item>
          <el-menu-item index="/statistics/exam">考试统计</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
            :size="20"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import {
  DataAnalysis, Document, Tickets, User, UserFilled, TrendCharts,
  Fold, Expand, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const username = computed(() => {
  const user = getUser()
  return user ? user.username || user.nickname || '管理员' : '管理员'
})

const currentTitle = computed(() => {
  return route.meta.title || ''
})

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/courses')) return '/courses'
  if (path.startsWith('/exams/edit')) return '/exams'
  if (path.startsWith('/exams/random')) return '/exams/random'
  if (path.startsWith('/exams/records')) return '/exams/records'
  if (path.startsWith('/exams')) return '/exams'
  if (path.startsWith('/questions')) return '/questions'
  if (path.startsWith('/students')) return '/students'
  if (path.startsWith('/checkins')) return '/checkins'
  if (path.startsWith('/certificates')) return '/certificates'
  if (path.startsWith('/orders')) return '/orders'
  if (path.startsWith('/statistics')) return '/statistics/revenue'
  return path
})

function handleCommand(command) {
  if (command === 'logout') {
    removeToken()
    removeUser()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  overflow-x: hidden;
  transition: width 0.3s;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #263445;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-left: 10px;
  white-space: nowrap;
}

.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 6px;
}

.username {
  margin-left: 4px;
  color: #303133;
}

.layout-main {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
}

.el-menu {
  border-right: none;
}
</style>
