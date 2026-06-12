// pages/news-list/news-list.js
const app = getApp()

Page({
  data: {
    list: [],
    modules: [],
    activeModule: 0,
    loading: true
  },

  onLoad() {
    this.fetchModules()
    this.fetchData()
  },

  onPullDownRefresh() {
    this.fetchData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  async fetchModules() {
    try {
      const res = await app.get('/config/news-modules')
      this.setData({ modules: res.data || [] })
    } catch (e) {
      this.setData({ modules: [] })
    }
  },

  async fetchData() {
    this.setData({ loading: true })
    try {
      const params = {}
      if (this.data.activeModule > 0) params.moduleId = this.data.activeModule
      const res = await app.get('/news', params)
      this.setData({ list: res.data || [] })
    } catch (e) {
      this.setData({ list: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchModule(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ activeModule: id }, () => {
      this.fetchData()
    })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/news-detail/news-detail?id=' + id })
  },

  formatTime(time) {
    if (!time) return ''
    const d = new Date(time)
    const now = new Date()
    const diff = now - d
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
  }
})
