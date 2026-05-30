// pages/courses/courses.js
const app = getApp()

Page({
  data: {
    activeL1: 0,
    activeL2: 0,
    activeL3: 0,
    keyword: '',
    loading: false,
    categories: [],
    courseList: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    gradients: [
      'linear-gradient(135deg, #0052D9, #366EF4)',
      'linear-gradient(135deg, #00A870, #2BA471)',
      'linear-gradient(135deg, #ED7B2F, #E37318)',
      'linear-gradient(135deg, #8B5CF6, #7C3AED)',
      'linear-gradient(135deg, #E34D59, #C9353F)',
      'linear-gradient(135deg, #0594FA, #0052D9)'
    ]
  },

  onLoad(options) {
    if (options.keyword) {
      this.setData({ keyword: options.keyword })
    }
    this.fetchCategories()
    this.fetchCourses(true)
  },

  onShow() {
    // Tab切换到课程页时刷新
    if (this.data.courseList.length === 0) {
      this.fetchCourses(true)
    }
  },

  onPullDownRefresh() {
    this.fetchCourses(true).finally(() => wx.stopPullDownRefresh())
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

  async fetchCourses(refresh = false) {
    if (this.data.loading) return
    this.setData({ loading: true })

    const page = refresh ? 1 : this.data.page
    try {
      const params = {
        page,
        pageSize: this.data.pageSize
      }
      const cid = this.data.activeL3 > 0 ? this.data.activeL3 : (this.data.activeL2 > 0 ? this.data.activeL2 : this.data.activeL1)
      if (cid > 0) params.categoryId = cid
      if (this.data.keyword) params.keyword = this.data.keyword

      const res = await app.get('/courses', params)
      const records = res.data?.records || res.data || []
      const total = res.data?.total || 0

      if (refresh) {
        this.setData({
          courseList: records,
          page: 2,
          hasMore: records.length >= this.data.pageSize && records.length < total
        })
      } else {
        this.setData({
          courseList: [...this.data.courseList, ...records],
          page: page + 1,
          hasMore: records.length >= this.data.pageSize
        })
      }
    } catch (e) {
      if (refresh) this.setData({ courseList: [] })
    }
    this.setData({ loading: false })
  },

  onLoadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.fetchCourses(false)
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
      this.fetchCourses(true)
    })
  },

  selectL2(e) {
    this.setData({ activeL2: e.currentTarget.dataset.id, activeL3: 0 }, () => {
      this.fetchCourses(true)
    })
  },

  selectL3(e) {
    this.setData({ activeL3: e.currentTarget.dataset.id }, () => {
      this.fetchCourses(true)
    })
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/course-detail/course-detail?id=' + e.currentTarget.dataset.id })
  },

  goSearch() {
    wx.navigateTo({ url: '/pages/search/search' })
  }
})
