<template>
  <div class="tabbar-layout">
    <div class="tabbar-content">
      <router-view />
    </div>
    <van-tabbar
      v-model="active"
      active-color="#1989fa"
      inactive-color="#7d7e80"
      :safe-area-inset-bottom="true"
      route
    >
      <van-tabbar-item to="/home" icon="home-o" name="home">首页</van-tabbar-item>
      <van-tabbar-item to="/courses" icon="apps-o" name="courses">课程</van-tabbar-item>
      <van-tabbar-item to="/exam" icon="award-o" name="exam">考试</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o" name="mine">我的</van-tabbar-item>
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
</style>
