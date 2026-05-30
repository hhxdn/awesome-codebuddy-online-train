<template>
  <div class="certificates-page">
    <van-nav-bar title="我的证书" left-arrow @click-left="$router.back()" />

    <div v-if="loading" class="loading-wrap">
      <van-loading type="spinner" size="32" color="#0052D9" />
    </div>

    <div v-else-if="certificates.length === 0" class="empty-wrap">
      <van-empty description="暂无结业证书" image="search" />
    </div>

    <div v-else class="cert-list">
      <div v-for="cert in certificates" :key="cert.id" class="cert-card" @click="viewDetail(cert)">
        <div class="cert-header">
          <van-icon name="certificate" size="28" color="#E37318" />
          <div class="cert-info">
            <h4>{{ cert.title }}</h4>
            <p>{{ cert.courseTitle }}</p>
          </div>
          <van-icon name="arrow" size="16" color="#C9CDD4" />
        </div>
        <div class="cert-footer">
          <span class="cert-no">编号: {{ cert.certNo }}</span>
          <span class="cert-time">{{ formatTime(cert.issueTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 证书详情弹窗 -->
    <van-action-sheet v-model:show="showDetail" title="证书详情" :style="{ padding: '0' }">
      <div v-if="currentCert" class="detail-content">
        <div class="detail-card">
          <div class="detail-badge">结业证书</div>
          <h2>{{ currentCert.title }}</h2>
          <p class="detail-desc">{{ currentCert.content }}</p>
          <div class="detail-meta">
            <p><strong>证书编号：</strong>{{ currentCert.certNo }}</p>
            <p><strong>颁发时间：</strong>{{ formatTime(currentCert.issueTime) }}</p>
            <p><strong>课程：</strong>{{ currentCert.courseTitle }}</p>
          </div>
        </div>
        <van-button block round type="primary" class="close-btn" @click="showDetail = false">
          关闭
        </van-button>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../../api'

const loading = ref(true)
const certificates = ref([])
const showDetail = ref(false)
const currentCert = ref(null)

async function fetchCertificates() {
  loading.value = true
  try {
    const res = await get('/certificates')
    certificates.value = res.data || []
  } catch (e) {
    certificates.value = []
  } finally {
    loading.value = false
  }
}

function viewDetail(cert) {
  currentCert.value = cert
  showDetail.value = true
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(() => fetchCertificates())
</script>

<style scoped>
.certificates-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.loading-wrap, .empty-wrap {
  padding: 80px 0;
  text-align: center;
}

.cert-list {
  padding: 12px 16px;
}

.cert-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s;
}

.cert-card:active {
  transform: scale(0.98);
}

.cert-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cert-info {
  flex: 1;
}

.cert-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0 0 4px;
}

.cert-info p {
  font-size: 13px;
  color: #969799;
  margin: 0;
}

.cert-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.cert-no {
  font-size: 12px;
  color: #C9CDD4;
}

.cert-time {
  font-size: 12px;
  color: #969799;
}

.detail-content {
  padding: 20px;
}

.detail-card {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFF 100%);
  border: 2px solid #E37318;
  border-radius: 16px;
  padding: 30px 24px;
  text-align: center;
}

.detail-badge {
  display: inline-block;
  background: #E37318;
  color: #fff;
  padding: 4px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.detail-card h2 {
  font-size: 20px;
  color: #323233;
  margin: 0 0 12px;
}

.detail-desc {
  font-size: 14px;
  color: #646566;
  line-height: 1.8;
  margin: 0 0 20px;
}

.detail-meta {
  text-align: left;
  font-size: 13px;
  color: #646566;
}

.detail-meta p {
  margin: 6px 0;
}

.close-btn {
  margin-top: 20px;
  height: 44px;
  background: #0052D9 !important;
  border: none !important;
}
</style>
