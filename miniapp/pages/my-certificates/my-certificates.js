// pages/my-certificates/my-certificates.js
const app = getApp()
Page({
  data: { certificates: [] },
  onShow() { this.fetchCertificates() },
  async fetchCertificates() {
    try { const res = await app.get('/certificates'); this.setData({ certificates: res.data || [] }) }
    catch (e) { this.setData({ certificates: [] }) }
  },
  showDetail(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: item.title || item.name,
      content: (item.description || '') + '\n\n颁发日期：' + (item.issueDate || item.createTime || ''),
      showCancel: false
    })
  }
})
