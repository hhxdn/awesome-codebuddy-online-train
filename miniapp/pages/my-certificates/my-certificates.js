// pages/my-certificates/my-certificates.js
const app = getApp()
Page({
  data: {
    loading: true,
    certificates: [],
    showDetail: false,
    currentCert: null
  },
  onShow() { this.fetchCertificates() },
  async fetchCertificates() {
    this.setData({ loading: true })
    try {
      const res = await app.get('/certificates')
      const list = (res.data || []).map(item => ({
        ...item,
        _issueTime: this.formatTime(item.issueTime)
      }))
      this.setData({ certificates: list })
    } catch (e) {
      this.setData({ certificates: [] })
    } finally {
      this.setData({ loading: false })
    }
  },
  viewDetail(e) {
    const item = e.currentTarget.dataset.item
    this.setData({ currentCert: { ...item, _issueTime: this.formatTime(item.issueTime) }, showDetail: true })
  },
  closeDetail() {
    this.setData({ showDetail: false, currentCert: null })
  },
  formatTime(time) {
    if (!time) return ''
    const d = new Date(time)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  },
  downloadAttachment() {
    const url = this.data.currentCert ? this.data.currentCert.attachmentUrl : ''
    if (!url) return
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            fail: () => {
              wx.showToast({ title: '打开失败', icon: 'none' })
            }
          })
        }
      },
      fail: () => {
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  },
  // 阻止详情弹窗背景滚动穿透
  preventTouchMove() {
    return false
  },
  // 阻止点击详情面板关闭
  noop() {}
})
