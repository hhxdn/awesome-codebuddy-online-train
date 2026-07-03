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
      // 使用 skipGlobalError 禁止全局 toast，由本页自行处理付费 403
      const res = await app.request({
        url: '/chapters/' + chapterId,
        method: 'GET',
        skipGlobalError: true
      })
      if (res && res.data) {
        this.setData({
          chapter: res.data,
          courseName: res.data.courseName || ''
        })

        // 获取同一课程的所有章节 + 购买状态
        if (res.data.courseId) {
          const [chaptersRes, accessRes] = await Promise.all([
            app.get('/courses/' + res.data.courseId + '/chapters'),
            app.get('/courses/' + res.data.courseId + '/access').catch(() => ({ data: null }))
          ])
          const chapters = (chaptersRes && chaptersRes.data) || []
          const purchased = accessRes?.data?.accessible || false
          const idx = chapters.findIndex(c => c.id == chapterId)
          // 未购买时，上一节/下一节只能跳转到免费章节
          const filterFree = (list) => list.filter(c => purchased || c.free)
          const freeChapters = filterFree(chapters)
          const freeIdx = freeChapters.findIndex(c => c.id == chapterId)
          this.setData({
            chapters,
            prevChapter: freeIdx > 0 ? freeChapters[freeIdx - 1] : null,
            nextChapter: freeIdx < freeChapters.length - 1 ? freeChapters[freeIdx + 1] : null
          })
        }
      }
    } catch (e) {
      const errMsg = e.message || ''
      if (errMsg.includes('购买课程') || errMsg.includes('购买')) {
        wx.showModal({
          title: '需要购买课程',
          content: errMsg + '，请前往课程详情页购买',
          showCancel: false,
          confirmText: '我知道了',
          confirmColor: '#0052D9',
          success() {
            wx.navigateBack()
          }
        })
      } else if (errMsg !== '登录已过期') {
        wx.showToast({ title: '加载失败', icon: 'none' })
      }
    }
  },

  switchChapter(e) {
    const id = e.currentTarget.dataset.id
    if (!id || id == this.data.chapterId) return
    // 保存当前进度
    this.saveProgress()
    // 直接切换数据，不使用 redirectTo（同页面跳转会失败）
    this.setData({
      chapterId: String(id),
      chapter: {},
      prevChapter: null,
      nextChapter: null,
      currentPosition: 0,
      duration: 0,
      playbackRate: 1.0
    })
    this.fetchChapter()
    // 滚动到顶部
    wx.pageScrollTo({ scrollTop: 0, duration: 200 })
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
