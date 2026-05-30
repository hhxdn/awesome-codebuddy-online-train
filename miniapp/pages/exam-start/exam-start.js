// pages/exam-start/exam-start.js
const app = getApp()

Page({
  data: {
    paperId: '',
    exam: {},
    starting: false
  },

  onLoad(options) {
    this.setData({ paperId: options.paperId })
    this.fetchExamDetail()
  },

  async fetchExamDetail() {
    try {
      const res = await app.get('/exams/' + this.data.paperId)
      this.setData({ exam: res.data || {} })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async startExam() {
    if (this.data.starting) return
    this.setData({ starting: true })

    try {
      const res = await app.post('/exam/start', { paperId: parseInt(this.data.paperId) })
      const recordId = res.data?.id || res.data?.recordId
      if (recordId) {
        wx.redirectTo({ url: '/pages/exam-question/exam-question?recordId=' + recordId })
      } else {
        wx.showToast({ title: '开始考试失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '开始考试失败', icon: 'none' })
    }
    this.setData({ starting: false })
  }
})
