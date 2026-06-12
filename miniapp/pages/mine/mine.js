// pages/mine/mine.js
const app = getApp()

Page({
  data: {
    userInfo: {},
    stats: {}
  },

  onShow() {
    this.loadUserInfo()
    this.loadStats()
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      // 脱敏手机号
      const phone = userInfo.phone || ''
      const maskedPhone = phone.length > 7
        ? phone.substring(0, 3) + '****' + phone.substring(7)
        : phone
      this.setData({
        userInfo: { ...userInfo, phone: maskedPhone }
      })
    }
  },

  async loadStats() {
    try {
      const res = await app.get('/user/stats')
      this.setData({ stats: res.data || {} })
    } catch (e) {
      this.setData({ stats: {} })
    }
  },

  goMyCourses() {
    wx.navigateTo({ url: '/pages/my-courses/my-courses' })
  },

  goMyOrders() {
    wx.navigateTo({ url: '/pages/order-list/order-list' })
  },

  goMyWrong() {
    wx.navigateTo({ url: '/pages/my-wrong/my-wrong' })
  },

  goMyExams() {
    wx.navigateTo({ url: '/pages/my-exam-records/my-exam-records' })
  },

  goMyLearning() {
    wx.navigateTo({ url: '/pages/my-learning/my-learning' })
  },

  goMyCertificates() {
    wx.navigateTo({ url: '/pages/my-certificates/my-certificates' })
  },

  goMyReservations() {
    wx.navigateTo({ url: '/pages/my-reservations/my-reservations' })
  },

  goMyCourseReservations() {
    wx.navigateTo({ url: '/pages/my-course-reservations/my-course-reservations' })
  },

  goPracticeCourses() {
    wx.navigateTo({ url: '/pages/practice-courses/practice-courses' })
  },

  goNewsList() {
    wx.navigateTo({ url: '/pages/news-list/news-list' })
  },

  goEnroll() {
    wx.navigateTo({ url: '/pages/enroll/enroll' })
  },

  goQaSubmit() {
    wx.navigateTo({ url: '/pages/qa-submit/qa-submit' })
  },

  goAbout() {
    wx.navigateTo({ url: '/pages/about/about' })
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          app.globalData.token = ''
          app.globalData.userInfo = null
          app.globalData.approvalChecked = false
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})
