<template>
  <van-action-sheet
    v-model:show="localVisible"
    title="确认支付"
    :close-on-click-action="false"
  >
    <div class="payment-content">
      <div class="payment-course">{{ courseName }}</div>
      <div class="payment-amount">
        <span class="label">支付金额</span>
        <span class="value">¥{{ amount }}</span>
      </div>
      <div class="payment-action">
        <van-button type="primary" block round @click="$emit('confirm')">立即支付</van-button>
        <van-button block round plain style="margin-top:12px;" @click="$emit('cancel')">取消</van-button>
      </div>
    </div>
  </van-action-sheet>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  amount: { type: Number, default: 0 },
  courseName: { type: String, default: '' }
})

defineEmits(['confirm', 'cancel'])

const localVisible = computed({ get: () => props.visible, set: (val) => {} })
</script>

<style scoped>
.payment-content { padding: 20px 16px; }
.payment-course {
  font-size: 16px; font-weight: 500; color: var(--text-color);
  margin-bottom: 16px; text-align: center;
}
.payment-amount {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 0; border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light); margin-bottom: 20px;
}
.payment-amount .label { font-size: 14px; color: var(--text-secondary); }
.payment-amount .value { font-size: 24px; font-weight: 700; color: var(--danger); }
.payment-action { padding: 0 10px; }
</style>
