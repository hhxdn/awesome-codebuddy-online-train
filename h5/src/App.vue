<template>
  <router-view v-slot="{ Component }">
    <keep-alive include="TabbarLayout">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script setup>
</script>
