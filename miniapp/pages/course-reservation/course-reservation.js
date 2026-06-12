// pages/course-reservation/course-reservation.js
const app = getApp()

Page({
  data: {
    courseId: null,
    loading: true,
    submitting: false,
    course: null,
    myReservation: null,
    reservationTime: '',
    remark: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ courseId: options.id })
      this.loadData()
    }
  },

  async loadData() {
    this.setData({ loading: true })
    await Promise.all([this.fetchCourse(), this.fetchMyReservation()])
    this.setData({ loading: false })
  },

  async fetchCourse() {
    try {
      const res = await app.get('/courses/' + this.data.courseId)
      this.setData({ course: res.data })
    } catch (e) {
      this.setData({ course: null })
    }
  },

  async fetchMyReservation() {
    try {
      const res = await app.get('/course/reservations/my')
      const list = res.data || []
      const my = list.find(r => r.courseId === Number(this.data.courseId)) || null
      this.setData({ myReservation: my })
    } catch (e) {
      this.setData({ myReservation: null })
    }
  },

  onTimeChange(e) {
    this.setData({ reservationTime: e.detail.value })
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  async handleReserve() {
    this.setData({ submitting: true })
    try {
      const params = { courseId: Number(this.data.courseId) }
      if (this.data.reservationTime) {
        params.reservationTime = this.data.reservationTime + 'T00:00:00'
      }
      if (this.data.remark) {
        params.remark = this.data.remark
      }
      await app.post('/course/reservations', params)
      wx.showToast({ title: '预约成功', icon: 'success' })
      await this.fetchMyReservation()
    } catch (e) {
      wx.showToast({ title: e.message || '预约失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async handleCancel() {
    const res = await new Promise((resolve) => {
      wx.showModal({
        title: '取消预约',
        content: '确定要取消本次预约吗？',
        success: (r) => resolve(r.confirm)
      })
    })
    if (!res) return
    try {
      await app.put('/course/reservations/' + this.data.myReservation.id + '/cancel')
      wx.showToast({ title: '预约已取消', icon: 'success' })
      this.setData({ 'myReservation.status': 'CANCELLED' })
    } catch (e) {
      // cancelled
    }
  },

  getStatusLabel(status) {
    const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
    return map[status] || status
  }
})
