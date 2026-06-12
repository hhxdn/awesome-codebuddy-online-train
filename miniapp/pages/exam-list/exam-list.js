// pages/exam-list/exam-list.js
const app = getApp()

Page({
  data: {
    activeL1: 0,
    activeL2: 0,
    activeL3: 0,
    loading: false,
    categories: [],
    examList: [],
    page: 1,
    pageSize: 10,
    hasMore: true
  },

  onLoad(options) {
    this.fetchCategories()
    this.fetchExams(true)
  },

  onShow() {
    if (this.data.examList.length === 0) {
      this.fetchExams(true)
    }
  },

  onPullDownRefresh() {
    this.fetchExams(true).finally(() => wx.stopPullDownRefresh())
  },

  async fetchCategories() {
    try {
      const res = await app.get('/categories/tree')
      if (res.data && res.data.length > 0) {
        this.setData({ categories: res.data })
      }
    } catch (e) {
      this.setData({ categories: [] })
    }
  },

  async fetchExams(refresh = false) {
    if (this.data.loading) return
    this.setData({ loading: true })

    const page = refresh ? 1 : this.data.page
    try {
      const params = { page, pageSize: this.data.pageSize }
      const cid = this.data.activeL3 > 0 ? this.data.activeL3 : (this.data.activeL2 > 0 ? this.data.activeL2 : this.data.activeL1)
      if (cid > 0) params.categoryId = cid

      const res = await app.get('/exams', params)
      const records = res.data?.records || res.data || []

      if (refresh) {
        this.setData({
          examList: records,
          page: 2,
          hasMore: records.length >= this.data.pageSize
        })
      } else {
        this.setData({
          examList: [...this.data.examList, ...records],
          page: page + 1,
          hasMore: records.length >= this.data.pageSize
        })
      }
    } catch (e) {
      if (refresh) this.setData({ examList: [] })
    }
    this.setData({ loading: false })
  },

  onLoadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.fetchExams(false)
    }
  },

  get activeL2List() {
    if (this.data.activeL1 === 0) return []
    const cat = this.data.categories.find(c => c.id === this.data.activeL1)
    return cat?.children || []
  },

  get activeL3List() {
    if (this.data.activeL2 === 0) return []
    for (const cat of this.data.categories) {
      const l2 = cat.children?.find(c => c.id === this.data.activeL2)
      if (l2) return l2.children || []
    }
    return []
  },

  selectL1(e) {
    this.setData({ activeL1: e.currentTarget.dataset.id, activeL2: 0, activeL3: 0 }, () => {
      this.fetchExams(true)
    })
  },

  selectL2(e) {
    this.setData({ activeL2: e.currentTarget.dataset.id, activeL3: 0 }, () => {
      this.fetchExams(true)
    })
  },

  selectL3(e) {
    this.setData({ activeL3: e.currentTarget.dataset.id }, () => {
      this.fetchExams(true)
    })
  },

  goExamStart(e) {
    const item = e.currentTarget.dataset.item
    // 线下考试跳转预约页
    if (item.type === 'OFFLINE') {
      wx.navigateTo({ url: '/pages/exam-reservation/exam-reservation?id=' + item.id })
      return
    }
    wx.navigateTo({ url: '/pages/exam-start/exam-start?paperId=' + item.id })
  }
})
