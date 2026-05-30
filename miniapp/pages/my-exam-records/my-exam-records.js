// pages/my-exam-records/my-exam-records.js
const app = getApp()
Page({
  data: { records: [] },
  onShow() { this.fetchRecords() },
  async fetchRecords() {
    try { const res = await app.get('/user/exam-records'); this.setData({ records: res.data?.records || res.data || [] }) }
    catch (e) { this.setData({ records: [] }) }
  },
  goDetail(e) { wx.navigateTo({ url: '/pages/exam-result/exam-result?recordId=' + e.currentTarget.dataset.id }) }
})
