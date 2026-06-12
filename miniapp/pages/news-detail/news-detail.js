// pages/news-detail/news-detail.js
const app = getApp()

Page({
  data: {
    article: null,
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.fetchDetail(options.id)
    }
  },

  async fetchDetail(id) {
    this.setData({ loading: true })
    try {
      const res = await app.get('/news/' + id)
      this.setData({ article: res.data })
    } catch (e) {
      this.setData({ article: null })
    } finally {
      this.setData({ loading: false })
    }
  }
})
