// pages/practice-result/practice-result.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    resultId: '',
    result: {
      totalCount: 0,
      rightCount: 0,
      totalScore: 0,
      accuracy: 0
    }
  },

  onLoad(options) {
    this.setData({
      chapterId: options.chapterId || '',
      resultId: options.resultId || ''
    })

    // 通过 eventChannel 接收提交结果（navigateTo 方式进入）
    const eventChannel = this.getOpenerEventChannel?.()
    if (eventChannel) {
      eventChannel.on('practiceResult', (data) => {
        this.setData({ result: data || {} })
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
