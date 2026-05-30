// pages/login/login.js
const app = getApp()

Page({
  data: {
    activeTab: 'login',
    loading: false,
    loginForm: { phone: '', password: '' },
    registerForm: { phone: '', password: '', confirmPassword: '' }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  onLoginPhoneInput(e) { this.setData({ 'loginForm.phone': e.detail.value }) },
  onLoginPwdInput(e) { this.setData({ 'loginForm.password': e.detail.value }) },
  onRegPhoneInput(e) { this.setData({ 'registerForm.phone': e.detail.value }) },
  onRegPwdInput(e) { this.setData({ 'registerForm.password': e.detail.value }) },
  onRegCpwdInput(e) { this.setData({ 'registerForm.confirmPassword': e.detail.value }) },

  async onLogin() {
    const { phone, password } = this.data.loginForm
    if (!phone) { wx.showToast({ title: '请输入手机号', icon: 'none' }); return }
    if (!/^1[3-9]\d{9}$/.test(phone)) { wx.showToast({ title: '手机号格式不正确', icon: 'none' }); return }
    if (!password) { wx.showToast({ title: '请输入密码', icon: 'none' }); return }

    this.setData({ loading: true })
    try {
      const res = await app.post('/user/login', { phone, password })
      if (res.data?.token) {
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.user)
        app.globalData.token = res.data.token
        app.globalData.userInfo = res.data.user
        wx.showToast({ title: '登录成功', icon: 'success' })

        const approvalStatus = res.data?.approvalStatus || 'APPROVED'
        const hasProfile = res.data?.user?.realName && res.data?.user?.realName !== ''
        if (approvalStatus === 'PENDING') {
          wx.redirectTo({ url: hasProfile ? '/pages/pending-approval/pending-approval' : '/pages/register-profile/register-profile' })
        } else {
          wx.switchTab({ url: '/pages/home/home' })
        }
      } else {
        wx.showToast({ title: res.message || '登录失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    }
    this.setData({ loading: false })
  },

  async onRegister() {
    const { phone, password, confirmPassword } = this.data.registerForm
    if (!phone) { wx.showToast({ title: '请输入手机号', icon: 'none' }); return }
    if (!/^1[3-9]\d{9}$/.test(phone)) { wx.showToast({ title: '手机号格式不正确', icon: 'none' }); return }
    if (!password || password.length < 6) { wx.showToast({ title: '密码至少6位', icon: 'none' }); return }
    if (password !== confirmPassword) { wx.showToast({ title: '两次密码不一致', icon: 'none' }); return }

    this.setData({ loading: true })
    try {
      const res = await app.post('/user/register', { phone, password, confirmPassword })
      if (res.data?.token) {
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.user)
        app.globalData.token = res.data.token
        app.globalData.userInfo = res.data.user
        wx.showToast({ title: '注册成功', icon: 'success' })
        wx.redirectTo({ url: '/pages/register-profile/register-profile' })
      } else {
        wx.showToast({ title: res.message || '注册失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '注册失败', icon: 'none' })
    }
    this.setData({ loading: false })
  }
})
