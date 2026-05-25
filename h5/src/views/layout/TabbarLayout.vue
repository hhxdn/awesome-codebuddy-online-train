<template>
  <div class="tabbar-layout">
    <div class="tabbar-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
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
    >
      <van-tabbar-item to="/home" icon="home-o" name="home">
        首页
      </van-tabbar-item>
      <van-tabbar-item to="/courses" icon="apps-o" name="courses">
        课程
      </van-tabbar-item>
      <van-tabbar-item to="/exam" icon="certificate" name="exam">
        考试
      </van-tabbar-item>
      <van-tabbar-item to="/mine" icon="manager-o" name="mine">
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

:deep(.van-tabbar) {
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.05);
}

:deep(.van-tabbar-item__text) {
  font-size: 11px;
}
</style>
