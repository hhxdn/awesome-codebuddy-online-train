// pages/enroll/enroll.js
const app = getApp()

Page({
  data: {
    step: 1,
    loading: false,
    conditions: '',
    form: {
      realName: '',
      gender: '',
      age: '',
      education: '',
      major: '',
      contactPhone: ''
    },
    genderIndex: -1,
    educationIndex: -1,
    genderColumns: ['男', '女'],
    educationColumns: ['高中', '中专', '大专', '本科', '硕士', '博士', '其他']
  },

  onLoad() {
    this.fetchConditions()
  },

  async fetchConditions() {
    try {
      const res = await app.get('/config/enrollment-conditions')
      if (res.data?.content) {
        this.setData({ conditions: res.data.content })
      }
    } catch (e) {
      this.setData({ conditions: '1. 遵纪守法，品行端正\n2. 具有大专及以上学历\n3. 身体健康，无不良嗜好\n4. 具备相关工作经验优先' })
    }
  },

  goStep2() {
    this.setData({ step: 2 })
  },

  goStep1() {
    this.setData({ step: 1 })
  },

  onGenderChange(e) {
    this.setData({ genderIndex: e.detail.value })
    const gender = this.data.genderColumns[e.detail.value]
    this.setData({ 'form.gender': gender })
  },

  onEducationChange(e) {
    this.setData({ educationIndex: e.detail.value })
    const education = this.data.educationColumns[e.detail.value]
    this.setData({ 'form.education': education })
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  async onSubmit() {
    const form = this.data.form
    if (!form.realName.trim()) {
      wx.showToast({ title: '请输入姓名', icon: 'none' })
      return
    }
    if (!form.gender) {
      wx.showToast({ title: '请选择性别', icon: 'none' })
      return
    }
    if (!form.age) {
      wx.showToast({ title: '请输入年龄', icon: 'none' })
      return
    }
    if (!form.education) {
      wx.showToast({ title: '请选择学历', icon: 'none' })
      return
    }
    if (!form.major.trim()) {
      wx.showToast({ title: '请输入专业', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(form.contactPhone.trim())) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      await app.post('/user/submit-profile', {
        realName: form.realName,
        gender: form.gender,
        age: form.age,
        education: form.education,
        major: form.major,
        contactPhone: form.contactPhone
      })
      wx.showModal({
        title: '报名成功',
        content: '您的报名信息已提交成功！',
        showCancel: false,
        success: () => {
          wx.navigateBack()
        }
      })
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
