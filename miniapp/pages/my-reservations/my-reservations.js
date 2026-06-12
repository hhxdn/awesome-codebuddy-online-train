// pages/my-reservations/my-reservations.js
const app = getApp()

Page({
  data: {
    list: [],
    loading: true
  },

  onShow() {
    this.fetchData()
  },

  onPullDownRefresh() {
    this.fetchData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  async fetchData() {
    try {
      const res = await app.get('/exam/reservations/my')
      this.setData({ list: res.data || [] })
    } catch (e) {
      this.setData({ list: [] })
    }
    this.setData({ loading: false })
  },

  async handleCancel(e) {
    const item = e.currentTarget.dataset.item
    const res = await new Promise((resolve) => {
      wx.showModal({
        title: '取消预约',
        content: '确定要取消本次预约吗？',
        success: (r) => resolve(r.confirm)
      })
    })
    if (!res) return
    try {
      await app.put('/exam/reservations/' + item.id + '/cancel')
      wx.showToast({ title: '预约已取消', icon: 'success' })
      const list = this.data.list.map(r => {
        if (r.id === item.id) return { ...r, status: 'CANCELLED' }
        return r
      })
      this.setData({ list })
    } catch (e) {
      // cancelled
    }
  }
})
