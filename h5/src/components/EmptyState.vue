<template>
  <div class="empty-state">
    <van-empty :description="description" :image="image" />
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  image: {
    type: String,
    default: 'search'
  }
})
</script>

<style scoped>
.empty-state {
  padding: 40px 0;
}
</style>
