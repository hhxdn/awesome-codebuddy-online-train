<template>
  <div class="empty-state">
    <div class="empty-icon">
      <svg viewBox="0 0 80 80" fill="none" width="72" height="72">
        <circle cx="40" cy="40" r="36" stroke="#E5E6EB" stroke-width="1.5" stroke-dasharray="3 3"/>
        <path d="M28 42C28 42 32 34 40 34C48 34 52 42 52 42" stroke="#C9CDD4" stroke-width="1.5" stroke-linecap="round"/>
        <rect x="32" y="46" width="16" height="8" rx="4" stroke="#C9CDD4" stroke-width="1.5"/>
        <circle cx="33" cy="31" r="2" fill="#E5E6EB"/>
        <circle cx="47" cy="31" r="2" fill="#E5E6EB"/>
      </svg>
    </div>
    <p class="empty-text">{{ description }}</p>
    <p class="empty-sub" v-if="subText">{{ subText }}</p>
  </div>
</template>

<script setup>
defineProps({
  description: { type: String, default: '暂无数据' },
  subText: { type: String, default: '' }
})
</script>

<style scoped>
.empty-state {
  padding: 60px 20px; text-align: center;
}
.empty-icon { margin-bottom: 16px; display: inline-block; }
.empty-text {
  font-size: 14px; color: var(--text-muted);
}
.empty-sub {
  font-size: 12px; color: var(--text-placeholder);
  margin-top: 6px;
}
</style>
