// pages/pending-approval/pending-approval.js
const app = getApp()
Page({
  data: { status: 'PENDING' },
  onLoad() { this.checkStatus() },
  async checkStatus() {
    try {
      const res = await app.get('/user/check-status')
      this.setData({ status: res.data?.approvalStatus || 'PENDING' })
      if (res.data?.approvalStatus === 'APPROVED') {
        wx.switchTab({ url: '/pages/home/home' })
      }
    } catch (e) {}
  },
  reSubmit() { wx.redirectTo({ url: '/pages/register-profile/register-profile' }) }
})
