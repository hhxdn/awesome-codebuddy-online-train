// pages/index/index.js
const app = getApp()

Page({
  onLoad() {
    // 入口页：判断登录状态后跳转
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    // 检查审核状态
    app.checkApprovalStatus().then(({ approvalStatus, hasProfile }) => {
      if (approvalStatus === 'PENDING') {
        wx.redirectTo({ url: hasProfile ? '/pages/pending-approval/pending-approval' : '/pages/register-profile/register-profile' })
      } else if (approvalStatus === 'REJECTED') {
        wx.redirectTo({ url: '/pages/pending-approval/pending-approval' })
      } else {
        wx.switchTab({ url: '/pages/home/home' })
      }
    }).catch(() => {
      wx.switchTab({ url: '/pages/home/home' })
    })
  }
})
