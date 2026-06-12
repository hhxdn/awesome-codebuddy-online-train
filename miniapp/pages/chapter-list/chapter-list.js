// pages/chapter-list/chapter-list.js
const app = getApp()
Page({
  data: { courseId: '', courseName: '', chapters: [], progress: 0, finishedCount: 0 },
  onLoad(options) {
    this.setData({ courseId: options.id || options.courseId })
    this.fetchChapters()
  },
  async fetchChapters() {
    try {
      const res = await app.get('/courses/' + this.data.courseId + '/chapters')
      const chapters = res.data || []
      const finishedCount = chapters.filter(c => c.completed).length
      const progress = chapters.length > 0 ? Math.round(finishedCount / chapters.length * 100) : 0
      this.setData({ chapters, finishedCount, progress, courseName: chapters[0]?.courseName || '' })
    } catch (e) { this.setData({ chapters: [] }) }
  },
  goChapter(e) { wx.navigateTo({ url: '/pages/video-player/video-player?chapterId=' + e.currentTarget.dataset.id }) },

  async goPractice(e) {
    const chapterId = e.currentTarget.dataset.id
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
