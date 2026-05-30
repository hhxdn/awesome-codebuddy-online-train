// pages/my-wrong/my-wrong.js
const app = getApp()
Page({
  data: { wrongList: [] },
  onShow() { this.fetchWrong() },
  async fetchWrong() {
    try { const res = await app.get('/user/wrong-questions'); this.setData({ wrongList: res.data || [] }) }
    catch (e) { this.setData({ wrongList: [] }) }
  },
  clearAll() {
    wx.showModal({
      title: '确认清空',
      content: '确定要清空所有错题吗？',
      success: async (res) => {
        if (res.confirm) {
          try { await app.del('/user/wrong-questions'); wx.showToast({ title: '已清空', icon: 'success' }); this.setData({ wrongList: [] }) }
          catch (e) { wx.showToast({ title: '清空失败', icon: 'none' }) }
        }
      }
    })
  }
})
