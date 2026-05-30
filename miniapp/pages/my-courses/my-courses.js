// pages/my-courses/my-courses.js
const app = getApp()
Page({
  data: { courseList: [] },
  onShow() { this.fetchCourses() },
  async fetchCourses() {
    try { const res = await app.get('/user/courses'); this.setData({ courseList: res.data || [] }) }
    catch (e) { this.setData({ courseList: [] }) }
  },
  goDetail(e) { wx.navigateTo({ url: '/pages/course-detail/course-detail?id=' + e.currentTarget.dataset.id }) }
})
