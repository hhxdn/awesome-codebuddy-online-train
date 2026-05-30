// pages/order-list/order-list.js
const app = getApp()

Page({
  data: {
    activeTab: 'ALL',
    orderList: [],
    loading: false,
    page: 1,
    hasMore: true
  },

  onLoad(options) {
    if (options.tab) {
      this.setData({ activeTab: options.tab })
    }
    this.fetchOrders(true)
  },

  onPullDownRefresh() {
    this.fetchOrders(true).finally(() => wx.stopPullDownRefresh())
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab }, () => {
      this.fetchOrders(true)
    })
  },

  async fetchOrders(refresh = false) {
    if (this.data.loading) return
    this.setData({ loading: true })
    const page = refresh ? 1 : this.data.page
    try {
      const params = { page, pageSize: 10 }
      if (this.data.activeTab !== 'ALL') params.status = this.data.activeTab
      const res = await app.get('/orders', params)
      const records = res.data?.records || res.data || []
      if (refresh) {
        this.setData({ orderList: records, page: 2, hasMore: records.length >= 10 })
      } else {
        this.setData({ orderList: [...this.data.orderList, ...records], page: page + 1, hasMore: records.length >= 10 })
      }
    } catch (e) {
      if (refresh) this.setData({ orderList: [] })
    }
    this.setData({ loading: false })
  },

  onLoadMore() {
    if (this.data.hasMore && !this.data.loading) this.fetchOrders(false)
  },

  async cancelOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定取消此订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.put('/orders/' + id + '/cancel')
            wx.showToast({ title: '已取消', icon: 'success' })
            this.fetchOrders(true)
          } catch (e) {
            wx.showToast({ title: '取消失败', icon: 'none' })
          }
        }
      }
    })
  },

  async payOrder(e) {
    const id = e.currentTarget.dataset.id
    try {
      await app.post('/orders/' + id + '/pay')
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.fetchOrders(true)
    } catch (e) {
      wx.showToast({ title: '支付失败', icon: 'none' })
    }
  }
})
