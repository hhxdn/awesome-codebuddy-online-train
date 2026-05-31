// pages/about/about.js
const app = getApp()

Page({
  data: {
    content: '',
    loading: true
  },

  onLoad() {
    this.loadContent()
  },

  async loadContent() {
    try {
      const res = await app.get('/config/about_us')
      this.setData({
        content: (res.data && res.data.configValue) || '',
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  }
})
