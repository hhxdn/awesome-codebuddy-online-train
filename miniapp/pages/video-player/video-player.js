// pages/video-player/video-player.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    chapter: {},
    courseName: '',
    chapters: [],
    playbackRate: 1.0,
    speeds: [0.5, 0.75, 1.0, 1.25, 1.5, 2.0],
    prevChapter: null,
    nextChapter: null,
    currentPosition: 0,
    duration: 0,
    savedTimer: null
  },

  onLoad(options) {
    this.setData({ chapterId: options.chapterId })
    this.fetchChapter()
  },

  onUnload() {
    // 保存进度
    if (this.data.savedTimer) clearTimeout(this.data.savedTimer)
    this.saveProgress()
  },

  onHide() {
    this.saveProgress()
  },

  async fetchChapter() {
    const chapterId = this.data.chapterId
    try {
      const res = await app.get('/chapters/' + chapterId)
      if (res.data) {
        this.setData({
          chapter: res.data,
          courseName: res.data.courseName || ''
        })

        // 获取同一课程的所有章节
        if (res.data.courseId) {
          try {
            const chaptersRes = await app.get('/courses/' + res.data.courseId + '/chapters')
            const chapters = chaptersRes.data || []
            const idx = chapters.findIndex(c => c.id == chapterId)
            this.setData({
              chapters,
              prevChapter: idx > 0 ? chapters[idx - 1] : null,
              nextChapter: idx < chapters.length - 1 ? chapters[idx + 1] : null
            })
          } catch (e) {}
        }
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  switchChapter(e) {
    const id = e.currentTarget.dataset.id
    this.saveProgress()
    wx.redirectTo({ url: '/pages/video-player/video-player?chapterId=' + id })
  },

  setSpeed(e) {
    const rate = e.currentTarget.dataset.rate
    this.setData({ playbackRate: rate })
    const ctx = wx.createVideoContext('videoPlayer')
    if (ctx) ctx.playbackRate(rate)
  },

  onTimeUpdate(e) {
    this.setData({ currentPosition: e.detail.currentTime, duration: e.detail.duration })
    // 每5秒保存一次进度
    if (this.data.savedTimer) clearTimeout(this.data.savedTimer)
    this.data.savedTimer = setTimeout(() => {
      this.saveProgress()
    }, 5000)
  },

  onEnded() {
    this.saveProgress()
    // 标记章节完成
    this.markFinished()
  },

  onPlay() {
    const ctx = wx.createVideoContext('videoPlayer')
    if (ctx && this.data.playbackRate !== 1.0) {
      ctx.playbackRate(this.data.playbackRate)
    }
  },

  async saveProgress() {
    const { chapterId, currentPosition } = this.data
    if (!chapterId || currentPosition <= 0) return
    try {
      await app.post('/learning/progress', {
        chapterId: parseInt(chapterId),
        position: Math.floor(currentPosition)
      })
    } catch (e) {
      // 静默保存
    }
  },

  async markFinished() {
    try {
      await app.post('/chapters/' + this.data.chapterId + '/finish')
    } catch (e) {}
  }
})
