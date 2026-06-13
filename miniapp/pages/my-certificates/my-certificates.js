// pages/my-certificates/my-certificates.js
const app = getApp()
Page({
  data: {
    loading: true,
    certificates: [],
    showDetail: false,
    currentCert: null,
    downloading: false
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
  // 判断是否为图片
  _isImage(url) {
    const lower = (url || '').toLowerCase()
    return /\.(png|jpg|jpeg|gif|webp|bmp)(\?|$)/.test(lower)
  },
  downloadAttachment() {
    const url = this.data.currentCert ? this.data.currentCert.attachmentUrl : ''
    if (!url) return

    this.setData({ downloading: true })
    wx.showLoading({ title: '下载中...', mask: true })

    const isImage = this._isImage(url)

    if (isImage) {
      // 图片：使用 previewImage，可长按保存
      wx.downloadFile({
        url: url,
        success: (res) => {
          wx.hideLoading()
          if (res.statusCode === 200) {
            wx.previewImage({
              urls: [url],
              current: url,
              fail: () => {
                wx.showToast({ title: '预览失败', icon: 'none' })
              }
            })
          } else {
            wx.showToast({ title: '下载失败，请重试', icon: 'none' })
          }
          this.setData({ downloading: false })
        },
        fail: (err) => {
          wx.hideLoading()
          // COS 域名可能未配置 downloadFile 白名单，尝试直接预览
          wx.previewImage({
            urls: [url],
            current: url,
            fail: () => {
              wx.showToast({ title: '下载失败，请检查证书附件', icon: 'none' })
            }
          })
          this.setData({ downloading: false })
        }
      })
    } else {
      // 文档（PDF/Word等）：下载后打开
      wx.downloadFile({
        url: url,
        success: (res) => {
          wx.hideLoading()
          if (res.statusCode === 200) {
            wx.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              success: () => {
                wx.showToast({ title: '下载完成', icon: 'success' })
              },
              fail: () => {
                wx.showToast({ title: '文件类型不支持预览，请用其他应用打开', icon: 'none' })
              }
            })
          } else {
            wx.showToast({ title: '下载失败，请重试', icon: 'none' })
          }
          this.setData({ downloading: false })
        },
        fail: (err) => {
          wx.hideLoading()
          wx.showToast({ title: '下载失败，请检查网络', icon: 'none' })
          this.setData({ downloading: false })
        }
      })
    }
  },
  // 阻止详情弹窗背景滚动穿透
  preventTouchMove() {
    return false
  },
  // 阻止点击详情面板关闭
  noop() {}
})
