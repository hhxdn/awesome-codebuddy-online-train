// pages/exam-result/exam-result.js
const app = getApp()

Page({
  data: {
    recordId: '',
    result: {}
  },

  onLoad(options) {
    this.setData({ recordId: options.recordId })
    this.fetchResult()
  },

  async fetchResult() {
    try {
      const res = await app.get('/exam/records/' + this.data.recordId)
      this.setData({ result: res.data || {} })
    } catch (e) {
      wx.showToast({ title: '加载结果失败', icon: 'none' })
    }
  },

  goBack() {
    wx.switchTab({ url: '/pages/exam-list/exam-list' })
  }
})
