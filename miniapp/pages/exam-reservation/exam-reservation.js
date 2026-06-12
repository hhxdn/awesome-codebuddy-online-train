// pages/exam-reservation/exam-reservation.js
const app = getApp()

Page({
  data: {
    paperId: null,
    loading: true,
    submitting: false,
    exam: null,
    myReservation: null,
    reservationTime: '',
    remark: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ paperId: options.id })
      this.loadData()
    }
  },

  async loadData() {
    this.setData({ loading: true })
    await Promise.all([this.fetchExam(), this.fetchMyReservation()])
    this.setData({ loading: false })
  },

  async fetchExam() {
    try {
      const res = await app.get('/exams/' + this.data.paperId)
      // 后端返回字段 { id, name, duration, totalScore, passScore, questionCount }
      const data = res.data || {}
      this.setData({
        exam: {
          id: data.id,
          title: data.name || data.title,
          durationMinutes: data.duration || 60,
          totalScore: data.totalScore || 100,
          passScore: data.passScore || 60,
          questionCount: data.questionCount || 0
        }
      })
    } catch (e) {
      this.setData({ exam: null })
    }
  },

  async fetchMyReservation() {
    try {
      const res = await app.get('/exam/reservations/my')
      const list = res.data || []
      const my = list.find(r => r.examPaperId === Number(this.data.paperId)) || null
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
      const params = { examPaperId: Number(this.data.paperId) }
      if (this.data.reservationTime) {
        params.reservationTime = this.data.reservationTime + 'T00:00:00'
      }
      if (this.data.remark) {
        params.remark = this.data.remark
      }
      await app.post('/exam/reservations', params)
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
      await app.put('/exam/reservations/' + this.data.myReservation.id + '/cancel')
      wx.showToast({ title: '预约已取消', icon: 'success' })
      this.setData({ 'myReservation.status': 'CANCELLED' })
    } catch (e) {
      // cancelled
    }
  }
})
