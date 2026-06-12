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
    banners: [],
    newsList: [],
    newsModules: [],
    activeNewsModule: 0,
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
    this.fetchBanners()
    this.fetchNewsModules()
    this.fetchNews()
  },

  onShow() {
    if (this._loaded && this.data.categories.length > 0) {
      this.fetchCourses()
      this.fetchNews()
    }
    this._loaded = true
  },

  onPullDownRefresh() {
    Promise.all([this.fetchCategories(), this.fetchCourses(), this.fetchBanners(), this.fetchNews()]).finally(() => {
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
      const params = { size: 100 }
      const cid = this.data.activeL3 > 0 ? this.data.activeL3 : (this.data.activeL2 > 0 ? this.data.activeL2 : this.data.activeL1)
      if (cid > 0) params.categoryId = cid
      const res = await app.get('/courses', params)
      this.setData({ courseList: res.data?.records || res.data || [] })
    } catch (e) {
      this.setData({ courseList: [] })
    }
    this.setData({ loading: false })
  },

  async fetchBanners() {
    try {
      const res = await app.get('/banners')
      this.setData({ banners: res.data || [] })
    } catch (e) {
      this.setData({ banners: [] })
    }
  },

  async fetchNewsModules() {
    try {
      const res = await app.get('/config/news-modules')
      this.setData({ newsModules: res.data || [] })
    } catch (e) {
      this.setData({ newsModules: [] })
    }
  },

  async fetchNews() {
    try {
      const params = {}
      if (this.data.activeNewsModule > 0) params.moduleId = this.data.activeNewsModule
      const res = await app.get('/news', params)
      this.setData({ newsList: (res.data || []).slice(0, 4) })
    } catch (e) {
      this.setData({ newsList: [] })
    }
  },

  switchNewsTab(e) {
    const moduleId = e.currentTarget.dataset.id
    this.setData({ activeNewsModule: moduleId }, () => {
      this.fetchNews()
    })
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
  },

  goBannerLink(e) {
    const item = e.currentTarget.dataset.item
    if (!item.linkUrl) return
    if (item.linkUrl.startsWith('http')) {
      // 小程序无法直接打开外部链接，复制到剪贴板
      wx.setClipboardData({ data: item.linkUrl })
      wx.showToast({ title: '链接已复制', icon: 'none' })
    } else if (item.linkUrl.startsWith('/')) {
      wx.navigateTo({ url: item.linkUrl })
    }
  },

  goNewsDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/news-detail/news-detail?id=' + id })
  },

  goNewsList() {
    wx.navigateTo({ url: '/pages/news-list/news-list' })
  }
})
