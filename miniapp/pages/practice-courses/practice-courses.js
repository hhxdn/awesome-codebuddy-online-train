// pages/practice-courses/practice-courses.js
const app = getApp()

Page({
  data: {
    courses: [],
    loading: true
  },

  onLoad() {
    this.fetchCourses()
  },

  onShow() {
    if (this._loaded) {
      this.fetchCourses()
    }
    this._loaded = true
  },

  async fetchCourses() {
    this.setData({ loading: true })
    try {
      const res = await app.get('/courses/with-exercises')
      this.setData({ courses: res.data || [] })
    } catch (e) {
      this.setData({ courses: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  goCourse(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/course-detail/course-detail?id=' + id })
  },

  goBrowseCourses() {
    wx.switchTab({ url: '/pages/courses/courses' })
  }
})
