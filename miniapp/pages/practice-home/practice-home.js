// pages/practice-home/practice-home.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    chapter: {},
    stats: {},
    historyList: []
  },

  onLoad(options) {
    this.setData({ chapterId: options.chapterId })
    this.fetchData()
  },

  async fetchData() {
    const chapterId = this.data.chapterId

    try {
      const res = await app.get('/chapters/' + chapterId)
      this.setData({ chapter: res.data || {} })
    } catch (e) {}

    try {
      const res = await app.get('/chapters/' + chapterId + '/practice/stats')
      this.setData({ stats: res.data || {} })
    } catch (e) {
      this.setData({ stats: {} })
    }

    // 练习历史需要从后端获取，这里暂用stats中的数据
    // 如果后端有单独的练习记录接口可以替换
  },

  startPractice() {
    wx.navigateTo({ url: '/pages/practice-question/practice-question?chapterId=' + this.data.chapterId })
  }
})
