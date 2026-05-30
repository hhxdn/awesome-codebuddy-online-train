// pages/offline-checkin/offline-checkin.js
const app = getApp()
Page({
  data: {
    courseId: '',
    location: { latitude: 0, longitude: 0 },
    withinRange: false,
    checkedIn: false,
    checkingIn: false
  },
  onLoad(options) {
    this.setData({ courseId: options.courseId })
    this.checkStatus()
  },
  async checkStatus() {
    try {
      const res = await app.get('/checkin/status/' + this.data.courseId)
      this.setData({ checkedIn: res.data?.checkedIn || false })
    } catch (e) {}
  },
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          location: { latitude: res.latitude, longitude: res.longitude },
          withinRange: true // 简化：实际应调用后端验证距离
        })
      },
      fail: () => {
        wx.showToast({ title: '获取位置失败', icon: 'none' })
      }
    })
  },
  async doCheckin() {
    if (this.data.checkingIn || this.data.checkedIn) return
    this.setData({ checkingIn: true })
    try {
      await app.post('/checkin', {
        courseId: parseInt(this.data.courseId),
        longitude: this.data.location.longitude,
        latitude: this.data.location.latitude
      })
      wx.showToast({ title: '打卡成功', icon: 'success' })
      this.setData({ checkedIn: true })
    } catch (e) {
      wx.showToast({ title: e.message || '打卡失败', icon: 'none' })
    }
    this.setData({ checkingIn: false })
  }
})
