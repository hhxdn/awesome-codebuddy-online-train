<template>
  <div class="tabbar-layout">
    <div class="tabbar-content">
      <router-view v-slot="{ Component }">
        <transition name="page-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
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

const tabMap = { home: 0, courses: 1, exam: 2, mine: 3 }

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
