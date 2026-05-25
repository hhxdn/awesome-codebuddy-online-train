<template>
  <div class="empty-state">
    <div class="empty-icon-wrap">
      <div class="empty-icon">
        <svg viewBox="0 0 80 80" fill="none" width="80" height="80">
          <circle cx="40" cy="40" r="38" stroke="#e5e7eb" stroke-width="1.5" stroke-dasharray="4 4"/>
          <path d="M28 42C28 42 32 34 40 34C48 34 52 42 52 42" stroke="#d1d5db" stroke-width="2" stroke-linecap="round"/>
          <path d="M30 52L35 46H45L50 52" stroke="#d1d5db" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="33" cy="30" r="2" fill="#e5e7eb"/>
          <circle cx="47" cy="30" r="2" fill="#e5e7eb"/>
        </svg>
      </div>
    </div>
    <p class="empty-text">{{ description }}</p>
    <p class="empty-sub" v-if="subText">{{ subText }}</p>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  subText: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-icon-wrap {
  margin-bottom: 18px;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  font-size: 15px;
  color: var(--text-muted);
  font-weight: 500;
}

.empty-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 6px;
  opacity: 0.7;
}
</style>
