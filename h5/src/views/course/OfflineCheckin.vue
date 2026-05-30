<template>
  <div class="checkin-page">
    <van-nav-bar title="线下打卡" left-arrow @click-left="$router.back()" />

    <div v-if="loading" class="loading-wrap">
      <van-loading type="spinner" size="32" color="#0052D9" />
      <p>加载中...</p>
    </div>

    <template v-else>
      <!-- 课程信息 -->
      <div class="section-card">
        <h3 class="course-title">{{ course.title }}</h3>
        <p class="course-desc">线下课程需到指定地点打卡完成</p>
        <div class="info-row">
          <span class="info-label">打卡范围</span>
          <span class="info-value">{{ checkinRadius }}米内</span>
        </div>
        <div v-if="prerequisiteTitle" class="info-row">
          <span class="info-label">前置课程</span>
          <span class="info-value" :class="{ 'prerequisite-done': prerequisiteFinished }">
            {{ prerequisiteTitle }}
            <van-icon v-if="prerequisiteFinished" name="success" color="#00A870" size="14" />
            <van-icon v-else name="warning-o" color="#E37318" size="14" />
          </span>
        </div>
      </div>

      <!-- 已打卡 -->
      <div v-if="checkedIn" class="section-card success-card">
        <van-icon name="success" size="48" color="#00A870" />
        <h3>已打卡</h3>
        <p>您已完成此线下课程</p>
      </div>

      <!-- 未打卡 -->
      <template v-else>
        <div v-if="!prerequisiteFinished && prerequisiteTitle" class="section-card warning-card">
          <van-icon name="warning-o" size="20" color="#E37318" />
          <span>请先完成「{{ prerequisiteTitle }}」全部章节后，才能打卡</span>
        </div>

        <div class="section-card">
          <h4>获取当前位置</h4>
          <p class="location-hint">点击下方按钮获取您的位置，验证是否在打卡范围内</p>

          <div v-if="currentLat" class="location-info">
            <van-tag type="primary" size="medium">当前位置</van-tag>
            <p class="coord">经度: {{ currentLng?.toFixed(6) }}</p>
            <p class="coord">纬度: {{ currentLat?.toFixed(6) }}</p>
          </div>

          <div v-if="distance != null" class="distance-info">
            <van-icon name="location-o" size="20" color="#0052D9" />
            <span>距离打卡点约 <strong>{{ distance }}</strong> 米</span>
            <van-tag v-if="distance <= checkinRadius" type="success" size="medium">在范围内</van-tag>
            <van-tag v-else type="danger" size="medium">超出范围</van-tag>
          </div>

          <van-button
            block
            round
            type="primary"
            :loading="locating"
            @click="getLocation"
            class="locate-btn"
          >
            <van-icon name="aim" size="18" />
            获取位置
          </van-button>

          <van-button
            v-if="distance != null && distance <= checkinRadius && prerequisiteFinished"
            block
            round
            type="primary"
            :loading="checkingIn"
            @click="doCheckin"
            class="checkin-btn"
          >
            确认打卡
          </van-button>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId

const loading = ref(true)
const locating = ref(false)
const checkingIn = ref(false)
const course = ref({})
const checkedIn = ref(false)
const checkinRadius = ref(3000)
const prerequisiteTitle = ref('')
const prerequisiteFinished = ref(true)
const currentLng = ref(null)
const currentLat = ref(null)
const distance = ref(null)

async function fetchStatus() {
  loading.value = true
  try {
    const res = await get('/checkin/status/' + courseId)
    if (res.data) {
      course.value = { id: courseId, title: res.data.courseTitle || '线下课程' }
      checkedIn.value = res.data.checkedIn
      checkinRadius.value = res.data.checkinRadius || 3000
      prerequisiteTitle.value = res.data.prerequisiteTitle || ''
      prerequisiteFinished.value = res.data.prerequisiteFinished !== false
    }
  } catch (e) {
    course.value = { id: courseId, title: '线下课程' }
  } finally {
    loading.value = false
  }
}

function getLocation() {
  locating.value = true
  if (typeof wx !== 'undefined' && wx.getLocation) {
    // 微信小程序环境
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        currentLat.value = res.latitude
        currentLng.value = res.longitude
        calculateDistance()
        locating.value = false
      },
      fail: () => {
        showToast('获取位置失败，请授权位置权限')
        locating.value = false
      }
    })
  } else if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        currentLat.value = pos.coords.latitude
        currentLng.value = pos.coords.longitude
        calculateDistance()
        locating.value = false
      },
      () => {
        showToast('获取位置失败，请允许定位权限')
        locating.value = false
      },
      { enableHighAccuracy: true, timeout: 10000 }
    )
  } else {
    showToast('您的设备不支持定位')
    locating.value = false
  }
}

async function calculateDistance() {
  // 后端计算距离
  try {
    const res = await post('/checkin', {
      courseId: Number(courseId),
      longitude: currentLng.value,
      latitude: currentLat.value
    })
    // 后端返回的距离信息（如果超出范围返回的是提示）
    if (res.data?.distance != null) {
      distance.value = res.data.distance
    }
  } catch (e) {
    showToast('计算距离失败')
  }
}

async function doCheckin() {
  checkingIn.value = true
  try {
    const res = await post('/checkin', {
      courseId: Number(courseId),
      longitude: currentLng.value,
      latitude: currentLat.value
    })
    if (res.data?.success) {
      showToast('打卡成功！')
      checkedIn.value = true
    } else if (res.data?.message) {
      showToast(res.data.message)
    }
  } catch (e) {
    showToast(e.response?.data?.message || '打卡失败')
  } finally {
    checkingIn.value = false
  }
}

onMounted(() => fetchStatus())
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.section-card {
  margin: 12px 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.loading-wrap {
  text-align: center;
  padding: 60px 0;
  color: #969799;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  color: #323233;
  margin: 0 0 8px;
}

.course-desc {
  font-size: 13px;
  color: #969799;
  margin: 0 0 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-top: 1px solid #f5f5f5;
}

.info-label {
  font-size: 13px;
  color: #969799;
}

.info-value {
  font-size: 13px;
  color: #323233;
  display: flex;
  align-items: center;
  gap: 4px;
}

.prerequisite-done {
  color: #00A870;
}

.success-card {
  text-align: center;
  padding: 40px 20px;
}

.success-card h3 {
  font-size: 18px;
  color: #00A870;
  margin: 12px 0 4px;
}

.success-card p {
  font-size: 13px;
  color: #969799;
  margin: 0;
}

.warning-card {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #E37318;
  background: #FFF7E6;
}

.location-hint {
  font-size: 13px;
  color: #969799;
  margin: 8px 0 16px;
}

.location-info {
  background: #f0f5ff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.coord {
  font-size: 12px;
  color: #646566;
  margin: 4px 0 0;
}

.distance-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #323233;
  flex-wrap: wrap;
}

.locate-btn {
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  background: #0052D9 !important;
  border: none !important;
  margin-bottom: 12px;
}

.checkin-btn {
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  background: #00A870 !important;
  border: none !important;
}
</style>
