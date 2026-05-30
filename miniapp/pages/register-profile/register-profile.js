// pages/register-profile/register-profile.js
const app = getApp()
Page({
  data: { form: { realName: '', gender: 'MALE', age: '', education: '', major: '', phone: '' }, submitting: false },
  onFieldInput(e) { const f = e.currentTarget.dataset.field; this.setData({ ['form.' + f]: e.detail.value }) },
  setGender(e) { this.setData({ 'form.gender': e.currentTarget.dataset.val }) },
  async submitProfile() {
    if (!this.data.form.realName) { wx.showToast({ title: '请输入姓名', icon: 'none' }); return }
    this.setData({ submitting: true })
    try {
      await app.post('/user/submit-profile', this.data.form)
      wx.showToast({ title: '提交成功', icon: 'success' })
      wx.redirectTo({ url: '/pages/pending-approval/pending-approval' })
    } catch (e) { wx.showToast({ title: '提交失败', icon: 'none' }) }
    this.setData({ submitting: false })
  }
})
