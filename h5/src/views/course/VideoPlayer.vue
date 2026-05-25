<template>
  <div class="video-player-page">
    <van-nav-bar :title="chapter.title || '视频播放'" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- Video Player -->
    <div class="video-player-container">
      <video
        ref="videoRef"
        :src="videoUrl"
        controls
        controlslist="nodownload"
        class="video-player"
        @timeupdate="onTimeUpdate"
        @ended="onVideoEnded"
        @loadedmetadata="onLoaded"
      >
        <source :src="videoUrl" type="video/mp4">
        您的浏览器不支持视频播放
      </video>
    </div>

    <!-- Speed Controls -->
    <div class="speed-controls">
      <button
        v-for="speed in speeds"
        :key="speed"
        :class="['speed-btn', { active: currentSpeed === speed }]"
        @click="changeSpeed(speed)"
      >
        {{ speed === 1 ? '1x' : speed + 'x' }}
      </button>
    </div>

    <!-- Next Chapter -->
    <div class="next-chapter" v-if="nextChapterId">
      <van-button block round type="primary" @click="goNext">
        下一节 {{ nextChapterTitle }}
      </van-button>
    </div>
    <div class="next-chapter" v-else>
      <van-button block round type="success" disabled>
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
const speeds = [0.5, 1, 1.25, 1.5, 2]
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
  // Resume from saved position
  const saved = localStorage.getItem('video_position_' + chapterId)
  if (saved && videoRef.value) {
    videoRef.value.currentTime = parseFloat(saved)
  }
}

function onTimeUpdate() {
  // Auto save position
}

function onVideoEnded() {
  showToast('播放完成')
  try {
    post('/chapters/' + chapterId + '/finish')
  } catch (e) {}
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
  try {
    post('/learning/progress', {
      chapterId: chapterId,
      position: videoRef.value?.currentTime || 0
    })
  } catch (e) {}
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

.video-player-container {
  background: #000;
}

.video-player {
  width: 100%;
  max-height: 50vh;
  display: block;
}

.next-chapter {
  padding: 16px;
  background: #fff;
}
</style>
