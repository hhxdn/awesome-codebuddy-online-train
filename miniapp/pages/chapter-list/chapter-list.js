// pages/chapter-list/chapter-list.js
const app = getApp()
Page({
  data: { courseId: '', courseName: '', chapters: [], progress: 0, finishedCount: 0, purchased: false, isPaid: false, loaded: false },
  onLoad(options) {
    this.setData({ courseId: options.id || options.courseId })
    this.fetchChapters()
  },
  async fetchChapters() {
    const courseId = this.data.courseId
    // 并行加载章节 + 课程信息 + 购买状态，避免 isPaid/purchased 未就绪时用户点击绕过检查
    const [chaptersRes, courseRes, accessRes] = await Promise.all([
      app.get('/courses/' + courseId + '/chapters').catch(() => ({ data: [] })),
      app.get('/courses/' + courseId).catch(() => ({ data: null })),
      app.get('/courses/' + courseId + '/access').catch(() => ({ data: null }))
    ])
    const chapters = chaptersRes.data || []
    const finishedCount = chapters.filter(c => c.completed).length
    const progress = chapters.length > 0 ? Math.round(finishedCount / chapters.length * 100) : 0
    const course = courseRes && courseRes.data
    this.setData({
      chapters, finishedCount, progress,
      courseName: chapters[0]?.courseName || '',
      isPaid: (course && (course.price || 0) > 0),
      purchased: accessRes?.data?.accessible || false,
      loaded: true
    })
  },
  goChapter(e) {
    const id = e.currentTarget.dataset.id
    const ch = this.data.chapters.find(c => c.id == id)
    if (this.data.isPaid && !this.data.purchased && ch && !ch.free) {
      wx.showModal({
        title: '需要购买课程',
        content: '该章节需要购买课程后才能观看',
        showCancel: false,
        confirmText: '我知道了',
        confirmColor: '#0052D9'
      })
      return
    }
    wx.navigateTo({ url: '/pages/video-player/video-player?chapterId=' + id })
  },

  async goPractice(e) {
    const chapterId = e.currentTarget.dataset.id
    const ch = this.data.chapters.find(c => c.id == chapterId)
    // 付费课程且未购买，检查是否免费章节
    if (this.data.isPaid && !this.data.purchased && ch && !ch.free) {
      wx.showModal({
        title: '需要购买课程',
        content: '该章节练习需要购买课程后才能使用',
        showCancel: false,
        confirmText: '我知道了',
        confirmColor: '#0052D9'
      })
      return
    }
    // 先检查练习权限（与 H5 一致）
    try {
      const chRes = await app.get('/chapters/' + chapterId)
      const courseId = chRes.data && chRes.data.courseId
      if (courseId) {
        const accessRes = await app.get('/courses/' + courseId + '/exercise-access')
        if (!accessRes.data || !accessRes.data.hasExerciseAccess) {
          wx.showModal({
            title: '练习权限未开通',
            content: '您还没有开通该课程的练习题权限，请联系管理员为您开通后再来练习',
            showCancel: false,
            confirmText: '知道了',
            confirmColor: '#0052D9'
          })
          return
        }
      }
    } catch (e) {
      wx.showModal({
        title: '练习权限未开通',
        content: '您还没有开通该课程的练习题权限，请联系管理员为您开通后再来练习',
        showCancel: false,
        confirmText: '知道了',
        confirmColor: '#0052D9'
      })
      return
    }
    wx.navigateTo({ url: '/pages/practice-home/practice-home?chapterId=' + chapterId })
  }
})
