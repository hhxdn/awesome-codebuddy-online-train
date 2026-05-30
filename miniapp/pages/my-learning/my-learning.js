// pages/my-learning/my-learning.js
const app = getApp()
Page({
  data: { records: [] },
  onShow() { this.fetchRecords() },
  async fetchRecords() {
    try { const res = await app.get('/user/learning-records'); this.setData({ records: res.data?.records || res.data || [] }) }
    catch (e) { this.setData({ records: [] }) }
  }
})
