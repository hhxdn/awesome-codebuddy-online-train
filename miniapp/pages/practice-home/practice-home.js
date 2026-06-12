// pages/practice-home/practice-home.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    chapter: {},
    stats: {},
    historyList: [],
    hasExerciseAccess: true,
    loading: true
  },

  onLoad(options) {
    this.setData({ chapterId: options.chapterId })
    this.fetchData()
  },

  async fetchData() {
    const chapterId = this.data.chapterId
    this.setData({ loading: true })

    // 先获取章节信息和权限
    try {
      const chRes = await app.get('/chapters/' + chapterId)
      const chapter = chRes.data || {}
      this.setData({ chapter })

      // 检查练习题权限（与 H5 一致）
      const courseId = chapter.courseId
      if (courseId) {
        try {
          const accessRes = await app.get('/courses/' + courseId + '/exercise-access')
          const hasAccess = accessRes.data && accessRes.data.hasExerciseAccess
          this.setData({ hasExerciseAccess: hasAccess })
        } catch (e) {
          this.setData({ hasExerciseAccess: false })
        }
      }
    } catch (e) {
      this.setData({ chapter: { id: chapterId, title: '未知章节' }, hasExerciseAccess: false })
    }

    // 无权限则不加载统计数据（与 H5 一致）
    if (!this.data.hasExerciseAccess) {
      this.setData({ loading: false })
      return
    }

    try {
      const res = await app.get('/chapters/' + chapterId + '/practice/stats')
      this.setData({ stats: res.data || {} })
    } catch (e) {
      this.setData({ stats: {} })
    }

    this.setData({ loading: false })
  },

  startPractice() {
    if (!this.data.hasExerciseAccess) {
      wx.showModal({
        title: '练习权限未开通',
        content: '您还没有开通该课程的练习题权限，请联系管理员为您开通后再来练习',
        showCancel: false,
        confirmText: '知道了',
        confirmColor: '#0052D9'
      })
      return
    }
    if (!this.data.stats.totalQuestions && this.data.stats.totalQuestions === 0) {
      wx.showToast({ title: '该章节暂无练习题', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/practice-question/practice-question?chapterId=' + this.data.chapterId })
  },

  goBack() {
    wx.navigateBack()
  }
})
