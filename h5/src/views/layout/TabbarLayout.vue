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
      active-color="var(--primary)"
      inactive-color="#9ca3af"
      :safe-area-inset-bottom="true"
      route
      :border="true"
      class="main-tabbar"
    >
      <van-tabbar-item to="/home">
        <template #icon="props">
          <div class="tab-icon-wrap" :class="{ 'tab-active': props.active }">
            <van-icon name="home-o" size="20" />
          </div>
        </template>
        首页
      </van-tabbar-item>
      <van-tabbar-item to="/courses">
        <template #icon="props">
          <div class="tab-icon-wrap" :class="{ 'tab-active': props.active }">
            <van-icon name="apps-o" size="20" />
          </div>
        </template>
        课程
      </van-tabbar-item>
      <van-tabbar-item to="/exam">
        <template #icon="props">
          <div class="tab-icon-wrap" :class="{ 'tab-active': props.active }">
            <van-icon name="certificate" size="20" />
          </div>
        </template>
        考试
      </van-tabbar-item>
      <van-tabbar-item to="/mine">
        <template #icon="props">
          <div class="tab-icon-wrap" :class="{ 'tab-active': props.active }">
            <van-icon name="manager-o" size="20" />
          </div>
        </template>
        我的
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref('home')

watch(
  () => route.path,
  (path) => {
    const parts = path.split('/')
    if (parts[1]) {
      active.value = parts[1]
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
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.06) !important;
  background: #fff !important;
}

.tab-icon-wrap {
  position: relative;
  transition: all var(--transition);
}

.tab-icon-wrap.tab-active {
  transform: scale(1.05);
}

.main-tabbar :deep(.van-tabbar-item) {
  transition: all var(--transition);
}

.main-tabbar :deep(.van-tabbar-item__text) {
  font-size: 10px;
  font-weight: 500;
}

/* Page transition */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
