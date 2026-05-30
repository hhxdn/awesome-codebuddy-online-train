// pages/home/home.js
const app = getApp()

Page({
  data: {
    activeL1: 0,
    activeL2: 0,
    activeL3: 0,
    loading: false,
    categories: [],
    courseList: [],
    gradients: [
      'linear-gradient(135deg, #0052D9, #366EF4)',
      'linear-gradient(135deg, #00A870, #2BA471)',
      'linear-gradient(135deg, #ED7B2F, #E37318)',
      'linear-gradient(135deg, #8B5CF6, #7C3AED)',
      'linear-gradient(135deg, #E34D59, #C9353F)',
      'linear-gradient(135deg, #0594FA, #0052D9)'
    ]
  },

  onLoad() {
    this.fetchCategories()
    this.fetchCourses()
  },

  onShow() {
    // 每次显示时刷新课程
    if (this.data.categories.length > 0) {
      this.fetchCourses()
    }
  },

  onPullDownRefresh() {
    Promise.all([this.fetchCategories(), this.fetchCourses()]).finally(() => {
      wx.stopPullDownRefresh()
    })
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

  async fetchCourses() {
    this.setData({ loading: true })
    try {
      const params = {}
      const cid = this.data.activeL3 > 0 ? this.data.activeL3 : (this.data.activeL2 > 0 ? this.data.activeL2 : this.data.activeL1)
      if (cid > 0) params.categoryId = cid
      const res = await app.get('/courses', params)
      this.setData({ courseList: res.data?.records || res.data || [] })
    } catch (e) {
      this.setData({ courseList: [] })
    }
    this.setData({ loading: false })
  },

  // 计算属性
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
    const id = e.currentTarget.dataset.id
    this.setData({ activeL1: id, activeL2: 0, activeL3: 0 }, () => {
      this.fetchCourses()
    })
  },

  selectL2(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ activeL2: id, activeL3: 0 }, () => {
      this.fetchCourses()
    })
  },

  selectL3(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ activeL3: id }, () => {
      this.fetchCourses()
    })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/course-detail/course-detail?id=' + id })
  },

  goSearch() {
    wx.navigateTo({ url: '/pages/search/search' })
  }
})
