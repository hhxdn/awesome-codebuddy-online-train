// pages/qa-submit/qa-submit.js
const app = getApp()

Page({
  data: {
    content: '',
    phone: '',
    images: [],
    submitting: false
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  chooseImage() {
    if (this.data.images.length >= 6) {
      wx.showToast({ title: '最多上传6张图片', icon: 'none' })
      return
    }
    const remain = 6 - this.data.images.length
    wx.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFiles = res.tempFilePaths
        this.uploadImages(tempFiles)
      }
    })
  },

  async uploadImages(tempFiles) {
    wx.showLoading({ title: '上传中...' })
    const urls = []
    for (const filePath of tempFiles) {
      try {
        const res = await this.uploadFile(filePath)
        if (res.data?.url) {
          urls.push(res.data.url)
        }
      } catch (e) {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    }
    wx.hideLoading()
    if (urls.length > 0) {
      this.setData({ images: [...this.data.images, ...urls] })
    }
  },

  uploadFile(filePath) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: app.globalData.baseUrl + '/qa/upload-image',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + app.globalData.token
        },
        success(res) {
          try {
            const data = JSON.parse(res.data)
            resolve(data)
          } catch (e) {
            resolve({ data: { url: '' } })
          }
        },
        fail(err) {
          reject(err)
        }
      })
    })
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index
    const images = [...this.data.images]
    images.splice(index, 1)
    this.setData({ images })
  },

  async handleSubmit() {
    const { content, phone, submitting } = this.data
    if (!content.trim()) {
      wx.showToast({ title: '请填写问题描述', icon: 'none' })
      return
    }
    if (!phone.trim()) {
      wx.showToast({ title: '请填写手机号', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (submitting) return

    this.setData({ submitting: true })
    try {
      const imagesStr = this.data.images.join(',')
      await app.post('/qa/submit', {
        content: content.trim(),
        phone: phone.trim(),
        images: imagesStr
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1000)
    } catch (e) {
      // error handled by interceptor
    } finally {
      this.setData({ submitting: false })
    }
  }
})
