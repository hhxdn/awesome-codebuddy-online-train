// pages/my-certificates/my-certificates.js
const app = getApp()
Page({
  data: { certificates: [] },
  onShow() { this.fetchCertificates() },
  async fetchCertificates() {
    try {
      const res = await app.get('/certificates')
      this.setData({ certificates: res.data || [] })
    } catch (e) {
      this.setData({ certificates: [] })
    }
  },
  showDetail(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: item.title || item.name,
      content: (item.content || '') + '\n\n颁发日期：' + (item.issueTime || ''),
      showCancel: false,
      confirmText: item.attachmentUrl ? '查看附件' : '确定',
      success: (res) => {
        if (res.confirm && item.attachmentUrl) {
          this.downloadAttachment(item.attachmentUrl)
        }
      }
    })
  },
  downloadAttachment(url) {
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            success: () => {},
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
  }
})
