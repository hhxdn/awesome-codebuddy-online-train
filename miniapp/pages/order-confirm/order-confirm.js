// pages/order-confirm/order-confirm.js
const app = getApp()

Page({
  data: {
    courseId: '',
    categoryId: '',
    productType: 'COURSE',
    productInfo: {},
    payMethod: 'WECHAT',
    submitting: false
  },

  onLoad(options) {
    if (options.categoryId) {
      this.setData({ categoryId: options.categoryId, productType: 'CATEGORY' })
      this.fetchCategoryInfo()
    } else if (options.courseId) {
      this.setData({ courseId: options.courseId, productType: 'COURSE' })
      this.fetchCourseInfo()
    }
  },

  async fetchCourseInfo() {
    try {
      const res = await app.get('/courses/' + this.data.courseId)
      this.setData({ productInfo: res.data || {} })
    } catch (e) {
      wx.showToast({ title: '加载课程信息失败', icon: 'none' })
    }
  },

  async fetchCategoryInfo() {
    try {
      const res = await app.get('/categories/' + this.data.categoryId)
      this.setData({ productInfo: res.data || {} })
    } catch (e) {
      wx.showToast({ title: '加载分类信息失败', icon: 'none' })
    }
  },

  selectPayMethod(e) {
    this.setData({ payMethod: e.currentTarget.dataset.method })
  },

  async createOrder() {
    if (this.data.submitting) return
    this.setData({ submitting: true })

    try {
      const data = {
        productType: this.data.productType,
        payMethod: this.data.payMethod
      }
      if (this.data.productType === 'CATEGORY') {
        data.categoryId = parseInt(this.data.categoryId)
      } else {
        data.courseId = parseInt(this.data.courseId)
      }

      const res = await app.post('/orders', data)
      const orderId = res.data?.id

      // 模拟支付
      if (orderId) {
        try {
          await app.post('/orders/' + orderId + '/pay')
          wx.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            wx.navigateBack()
          }, 1200)
        } catch (e) {
          wx.showToast({ title: '支付失败', icon: 'none' })
        }
      } else {
        wx.showToast({ title: '创建订单失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '下单失败', icon: 'none' })
    }
    this.setData({ submitting: false })
  }
})
