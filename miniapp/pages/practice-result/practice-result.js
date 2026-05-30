// pages/practice-result/practice-result.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    resultId: '',
    result: {}
  },

  onLoad(options) {
    this.setData({
      chapterId: options.chapterId,
      resultId: options.resultId
    })
    this.fetchResult()
  },

  async fetchResult() {
    // 从后端获取结果（如果resultId存在的话）
    // 暂时展示基本结果
    const eventChannel = this.getOpenerEventChannel?.()
    if (eventChannel) {
      eventChannel.on('practiceResult', (data) => {
        this.setData({ result: data })
      })
    }
  },

  backToHome() {
    wx.navigateBack({ delta: 2 })
  },

  retry() {
    wx.redirectTo({ url: '/pages/practice-question/practice-question?chapterId=' + this.data.chapterId })
  }
})
