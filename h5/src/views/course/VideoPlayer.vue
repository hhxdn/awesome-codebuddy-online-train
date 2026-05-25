<template>
  <div class="video-player-page">
    <van-nav-bar :title="chapter.title || '视频播放'" left-text="返回" left-arrow @click-left="$router.back()">
      <template #right>
        <span class="nav-chapter-title" v-if="chapter.title">{{ chapter.title }}</span>
      </template>
    </van-nav-bar>

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
      />
    </div>

    <!-- Speed Controls -->
    <div class="speed-panel">
      <span class="speed-label">倍速</span>
      <div class="speed-controls">
        <button
          v-for="speed in speeds"
          :key="speed"
          :class="['speed-btn', { active: currentSpeed === speed }]"
          @click="changeSpeed(speed)"
        >
          {{ speed }}x
        </button>
      </div>
    </div>

    <!-- Chapter Info -->
    <div class="chapter-info-bar" v-if="chapter.title">
      <van-icon name="play-circle-o" size="18" color="var(--primary)" />
      <span>{{ chapter.title }}</span>
      <span class="chapter-duration" v-if="chapter.duration">{{ chapter.duration }}</span>
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
import { ref, onMounted, onUnmounted } from 'vue'
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

async function fetchChapterDetail() {
  try {
    const res = await get('/chapters/' + chapterId)
    if (res.data) {
      chapter.value = res.data
      videoUrl.value = res.data.videoUrl || ''
    }
  } catch (e) {
    chapter.value = { id: chapterId, title: 'Demo 视频章节', videoUrl: '' }
    videoUrl.value = 'https://www.w3schools.com/html/mov_bbb.mp4'
    nextChapterId.value = parseInt(chapterId) + 1
    nextChapterTitle.value = '下一章节'
  }
}

function changeSpeed(speed) {
  currentSpeed.value = speed
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

function onTimeUpdate() {}

function onVideoEnded() {
  showToast('播放完成')
  post('/chapters/' + chapterId + '/finish').catch(() => {})
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

.nav-chapter-title {
  font-size: 12px;
  color: var(--text-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  padding: 10px 16px;
  background: #1a1a1a;
  gap: 12px;
}

.speed-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.speed-controls {
  display: flex;
  gap: 6px;
}

.speed-btn {
  padding: 4px 14px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: transparent;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.15s;
}

.speed-btn.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.chapter-info-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #1a1a1a;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.chapter-duration {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin-left: auto;
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
