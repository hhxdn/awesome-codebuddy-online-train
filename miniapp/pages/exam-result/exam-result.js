// pages/exam-result/exam-result.js
const app = getApp()

Page({
  data: {
    recordId: '',
    result: {},
    paperName: ''  // 考试名称
  },

  onLoad(options) {
    this.setData({ recordId: options.recordId })
    this.fetchResult()
  },

  async fetchResult() {
    try {
      const res = await app.get('/exam/records/' + this.data.recordId)
      const data = res.data || {}
      // 从paper.title提取考试名称
      const paperName = data.paper?.title || data.paperName || ''
      this.setData({ result: data, paperName })
    } catch (e) {
      wx.showToast({ title: '加载结果失败', icon: 'none' })
    }
  },

  goBack() {
    wx.switchTab({ url: '/pages/exam-list/exam-list' })
  }
})
