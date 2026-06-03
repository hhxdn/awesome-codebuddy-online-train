<template>
  <div class="video-player-page">
    <van-nav-bar :title="chapter.title || '视频播放'" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Video Player -->
    <div class="video-container">
      <div class="video-placeholder" v-if="!videoUrl">
        <van-icon name="video-o" size="48" color="rgba(255,255,255,0.4)" />
        <p>暂无视频资源</p>
      </div>
      <video
        v-else
        ref="videoRef"
        :src="videoUrl"
        controls
        controlslist="nodownload"
        class="video-player"
        @timeupdate="onTimeUpdate"
        @ended="onVideoEnded"
        @loadedmetadata="onLoaded"
        @play="onPlay"
        @error="onVideoError"
      />
    </div>

    <!-- Speed Controls -->
    <div v-if="videoUrl" class="speed-panel">
      <span class="speed-label">倍速播放</span>
      <div class="speed-options">
        <span
          v-for="speed in speeds"
          :key="speed"
          :class="['speed-item', { active: currentSpeed === speed }]"
          @click="changeSpeed(speed)"
        >
          {{ formatSpeed(speed) }}
        </span>
      </div>
    </div>

    <!-- Next Chapter -->
    <div class="next-section">
      <van-button v-if="nextChapterId" block round type="primary" size="large" @click="goNext" class="next-btn">
        下一节：{{ nextChapterTitle }}
        <van-icon name="arrow" />
      </van-button>
      <van-button v-else block round plain type="success" size="large" disabled>
        <van-icon name="success" />
        已学完所有章节
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const chapterId = route.params.chapterId

const videoRef = ref(null)
const videoUrl = ref('')
const chapter = ref({})
const currentSpeed = ref(1)
const speeds = [0.5, 0.75, 1, 1.25, 1.5, 2]
const nextChapterId = ref(null)
const nextChapterTitle = ref('')
const hasReportedFinish = ref(false)
let progressTimer = null

onMounted(async () => {
  await fetchChapterDetail()
  loadProgress()
  startProgressSave()
})

onUnmounted(() => {
  saveProgress()
  if (progressTimer) clearInterval(progressTimer)
})

const FALLBACK_VIDEO = 'https://www.w3schools.com/html/mov_bbb.mp4'

function isValidVideoUrl(url) {
  if (!url) return false
  if (url.includes('example.com')) return false
  return true
}

async function fetchChapterDetail() {
  try {
    const res = await get('/chapters/' + chapterId)
    if (res.data) {
      chapter.value = res.data
      const rawUrl = res.data.videoUrl || ''
      videoUrl.value = isValidVideoUrl(rawUrl) ? rawUrl : FALLBACK_VIDEO
    }
  } catch (e) {
    chapter.value = { id: chapterId, title: 'Demo 视频章节', videoUrl: '' }
    videoUrl.value = FALLBACK_VIDEO
    nextChapterId.value = parseInt(chapterId) + 1
    nextChapterTitle.value = '下一章节'
  }
}

function formatSpeed(speed) {
  return speed.toFixed(1).replace(/\.0$/, '') + 'x'
}

async function changeSpeed(speed) {
  currentSpeed.value = speed
  await nextTick()
  if (videoRef.value) {
    videoRef.value.playbackRate = speed
  }
}

function onLoaded() {
  const saved = localStorage.getItem('video_position_' + chapterId)
  if (saved && videoRef.value) {
    videoRef.value.currentTime = parseFloat(saved)
  }
}

function onTimeUpdate() {
  if (hasReportedFinish.value || !videoRef.value) return
  const video = videoRef.value
  if (video.duration && video.duration > 0) {
    const progress = video.currentTime / video.duration
    if (progress >= 0.9) {
      hasReportedFinish.value = true
      post('/chapters/' + chapterId + '/finish')
        .then(() => {
          showToast('已学完本章节，可进入下一节')
        })
        .catch(() => {})
    }
  }
}

function onPlay() {
  // 重新应用倍速设置（部分浏览器在播放开始时重置 playbackRate）
  if (videoRef.value && currentSpeed.value !== 1) {
    videoRef.value.playbackRate = currentSpeed.value
  }
}

function onVideoError() {
  console.warn('视频加载失败: ' + videoUrl.value)
  if (videoUrl.value !== FALLBACK_VIDEO) {
    videoUrl.value = FALLBACK_VIDEO
  }
}

function onVideoEnded() {
  if (!hasReportedFinish.value) {
    hasReportedFinish.value = true
    post('/chapters/' + chapterId + '/finish').catch(() => {})
  }
  showToast('播放完成')
}

function loadProgress() {
  const saved = localStorage.getItem('video_position_' + chapterId)
  if (saved && videoRef.value) {
    videoRef.value.currentTime = parseFloat(saved)
  }
}

function saveProgress() {
  if (videoRef.value) {
    localStorage.setItem('video_position_' + chapterId, videoRef.value.currentTime)
  }
  post('/learning/progress', {
    chapterId: chapterId,
    position: videoRef.value?.currentTime || 0
  }).catch(() => {})
}

function startProgressSave() {
  progressTimer = setInterval(() => {
    saveProgress()
  }, 30000)
}

function goNext() {
  if (nextChapterId.value) {
    router.push('/video/' + nextChapterId.value)
  }
}
</script>

<style scoped>
.video-player-page {
  background: #000;
  min-height: 100vh;
}

.video-container {
  background: #000;
  position: relative;
}

.video-placeholder {
  width: 100%;
  height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
}

.video-player {
  width: 100%;
  max-height: 50vh;
  display: block;
}

.speed-panel {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  gap: 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.speed-label {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
  flex-shrink: 0;
  margin-right: 14px;
}

.speed-options {
  display: flex;
  gap: 0;
  flex: 1;
  justify-content: space-between;
}

.speed-item {
  font-size: 14px;
  font-weight: 600;
  color: #4E5969;
  background: #F2F3F5;
  padding: 8px 0;
  border-radius: 8px;
  text-align: center;
  flex: 1;
  margin: 0 3px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.speed-item:active {
  transform: scale(0.94);
}

.speed-item.active {
  background: linear-gradient(135deg, #0052D9, #366EF4);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 82, 217, 0.35);
}

.next-section {
  padding: 20px 16px;
  background: #1a1a1a;
}

.next-btn {
  height: 48px;
  font-size: 15px;
}
</style>
