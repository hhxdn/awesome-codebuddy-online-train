<template>
  <div class="tabbar-layout">
    <!-- Desktop Sidebar -->
    <aside class="desktop-sidebar">
      <div class="sidebar-brand">
        <div class="sidebar-logo">
          <svg viewBox="0 0 32 32" width="32" height="32" fill="none">
            <rect width="32" height="32" rx="8" fill="#0052D9"/>
            <path d="M10 14L16 9L22 14V22C22 22.6 21.6 23 21 23H11C10.4 23 10 22.6 10 22V14Z" stroke="white" stroke-width="1.5" fill="none"/>
            <path d="M14 23V17H18V23" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="brand-name">在线学习</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/home" class="nav-item" :class="{ active: active === 0 }">
          <van-icon name="home-o" size="20" /><span>首页</span>
        </router-link>
        <router-link to="/courses" class="nav-item" :class="{ active: active === 1 }">
          <van-icon name="apps-o" size="20" /><span>课程中心</span>
        </router-link>
        <router-link to="/practice-courses" class="nav-item" :class="{ active: active === 2 }">
          <van-icon name="edit" size="20" /><span>练习</span>
        </router-link>
        <router-link to="/exam" class="nav-item" :class="{ active: active === 3 }">
          <van-icon name="certificate" size="20" /><span>在线考试</span>
        </router-link>
        <router-link to="/mine" class="nav-item" :class="{ active: active === 4 }">
          <van-icon name="user-o" size="20" /><span>个人中心</span>
        </router-link>
      </nav>
    </aside>

    <!-- Content Area -->
    <div class="tabbar-content">
      <router-view v-slot="{ Component }">
        <transition name="page-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- Mobile Tabbar -->
    <van-tabbar
      v-model="active"
      active-color="#0052D9"
      inactive-color="#86909C"
      :safe-area-inset-bottom="true"
      route
      class="main-tabbar"
    >
      <van-tabbar-item to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/courses" icon="apps-o">课程</van-tabbar-item>
      <van-tabbar-item to="/practice-courses" icon="edit">练习</van-tabbar-item>
      <van-tabbar-item to="/exam" icon="certificate">考试</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const tabMap = { home: 0, courses: 1, 'practice-courses': 2, exam: 3, mine: 4, 'my-courses': 4, 'my-orders': 4, 'my-wrong': 4, 'my-exams': 4, 'my-learning': 4, 'my-certificates': 4 }

watch(
  () => route.path,
  (path) => {
    const parts = path.split('/')
    if (parts[1] && tabMap[parts[1]] !== undefined) {
      active.value = tabMap[parts[1]]
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.tabbar-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.tabbar-content {
  flex: 1;
  padding-bottom: 50px;
  padding-bottom: calc(50px + constant(safe-area-inset-bottom));
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
}

/* Desktop Sidebar - hidden by default on mobile */
.desktop-sidebar {
  display: none;
}

.main-tabbar {
  box-shadow: 0 -1px 8px rgba(0, 0, 0, 0.04) !important;
  background: #fff !important;
}

.main-tabbar :deep(.van-tabbar-item__icon) {
  margin-bottom: 2px;
}

.main-tabbar :deep(.van-tabbar-item__text) {
  font-size: 10px;
}

/* ========================================
   Desktop Layout (>=768px)
   Left sidebar + main content area
   ======================================== */
@media (min-width: 768px) {
  .tabbar-layout {
    flex-direction: row;
    max-width: 1300px;
    margin: 0 auto;
    min-height: 100vh;
  }

  .desktop-sidebar {
    display: flex;
    flex-direction: column;
    width: 200px;
    min-height: 100vh;
    background: #fff;
    border-right: 1px solid var(--border-light);
    padding: 20px 0;
    overflow-y: auto;
    flex-shrink: 0;
  }

  .sidebar-brand {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 16px 18px;
    margin: 0 12px 16px;
    border-bottom: 1px solid var(--border-light);
  }

  .sidebar-logo {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }

  .sidebar-brand .brand-name {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-color);
    white-space: nowrap;
  }

  .sidebar-nav {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 0 10px;
    flex: 1;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    border-radius: 8px;
    color: var(--text-secondary);
    font-size: 14px;
    text-decoration: none;
    transition: all var(--transition);
    cursor: pointer;
  }

  .nav-item:hover {
    background: var(--bg-color);
    color: var(--text-color);
  }

  .nav-item.active {
    background: var(--primary-bg);
    color: var(--primary);
    font-weight: 600;
  }

  .nav-item.active:hover {
    background: var(--primary-bg);
    color: var(--primary);
  }

  .tabbar-content {
    flex: 1;
    padding-bottom: 0 !important;
    min-height: 100vh;
    background: var(--bg-color);
  }

  /* Hide mobile tabbar on desktop */
  .main-tabbar {
    display: none !important;
  }
}

/* Page transition */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-slide-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.page-slide-leave-to {
  opacity: 0;
  transform: translateX(-16px);
}
</style>
