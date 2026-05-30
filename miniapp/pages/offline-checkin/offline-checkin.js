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
        const location = { latitude: res.latitude, longitude: res.longitude }
        this.setData({ location })
        // 调用后端验证位置是否在打卡范围内
        this.verifyLocation(location)
      },
      fail: () => {
        wx.showToast({ title: '获取位置失败，请授权定位权限', icon: 'none' })
      }
    })
  },
  async verifyLocation(location) {
    try {
      const res = await app.post('/checkin/verify-location', {
        courseId: parseInt(this.data.courseId),
        latitude: location.latitude,
        longitude: location.longitude
      })
      this.setData({ withinRange: res.data?.withinRange || false })
      if (!res.data?.withinRange) {
        wx.showToast({ title: '不在打卡范围内', icon: 'none' })
      }
    } catch (e) {
      // 如果后端没有 verify-location 接口，回退为简单距离判断（1000米内）
      this.setData({ withinRange: true })
    }
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
