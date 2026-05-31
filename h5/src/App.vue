<template>
  <div v-if="showWechatTip" class="wechat-tip">
    <div class="tip-content">
      <van-icon name="wechat" size="64" color="#07c160" />
      <h2>请在微信中打开</h2>
      <p>本平台仅支持在微信客户端内访问</p>
      <p class="tip-desc">请复制链接后在微信中打开</p>
    </div>
  </div>
  <router-view v-else v-slot="{ Component }">
    <keep-alive include="TabbarLayout">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const WECHAT_ONLY_KEY = 'wechat_only_mode'
const showWechatTip = ref(false)

onMounted(() => {
  const ua = navigator.userAgent.toLowerCase()
  const isWechat = ua.includes('micromessenger')

  // 通过 URL 参数 ?wechat_only=0 或 ?wechat_only=1 实时切换
  const params = new URLSearchParams(window.location.search)
  const urlMode = params.get('wechat_only')
  if (urlMode === '0') {
    localStorage.setItem(WECHAT_ONLY_KEY, '0')
  } else if (urlMode === '1') {
    localStorage.setItem(WECHAT_ONLY_KEY, '1')
  }

  // 读取开关状态，默认关闭（不限制）
  const wechatOnly = localStorage.getItem(WECHAT_ONLY_KEY)
  if (wechatOnly === '1' && !isWechat) {
    showWechatTip.value = true
  }
})
</script>

<style scoped>
.wechat-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f7f8fa;
  padding: 20px;
}
.tip-content {
  text-align: center;
}
.tip-content h2 {
  font-size: 20px;
  color: #323233;
  margin: 20px 0 10px;
}
.tip-content p {
  font-size: 14px;
  color: #969799;
  margin-bottom: 6px;
}
.tip-content .tip-desc {
  margin-top: 16px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  color: #07c160;
  font-weight: 500;
}
</style>
