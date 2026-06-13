// pages/my-learning/my-learning.js
const app = getApp()
Page({
  data: { records: [] },
  onShow() { this.fetchRecords() },
  async fetchRecords() {
    try {
      const res = await app.get('/user/learning-records')
      const data = res.data || []
      // 后端返回的是数组（课程聚合数据），将 duration 从秒转为分钟便于展示
      const records = (Array.isArray(data) ? data : (data.records || [])).map(item => ({
        ...item,
        duration: item.duration ? Math.round(item.duration / 60) : (item.totalDuration ? Math.round(item.totalDuration / 60) : 0)
      }))
      this.setData({ records })
    }
    catch (e) { this.setData({ records: [] }) }
  },
  goCourseDetail(e) {
    const courseId = e.currentTarget.dataset.id
    if (courseId) {
      wx.navigateTo({ url: '/pages/course-detail/course-detail?courseId=' + courseId })
    }
  }
})
