// pages/course-detail/course-detail.js
const app = getApp()

Page({
  data: {
    courseId: '',
    course: {},
    chapters: [],
    purchased: false,
    isPaid: false,
    freeChapterCount: 0,
    descExpanded: false,
    hasLongDesc: false,
    checkedIn: false,
    belongCategory: null
  },

  onLoad(options) {
    this.setData({ courseId: options.id })
    this.fetchDetail()
  },

  async fetchDetail() {
    const courseId = this.data.courseId

    try {
      const res = await app.get('/courses/' + courseId)
      if (res.data) {
        const course = res.data
        this.setData({ course, isPaid: (course.price || 0) > 0, hasLongDesc: (course.description || '').length > 120 })

        if (course.categoryId) {
          try {
            const catRes = await app.get('/categories')
            const allCats = catRes.data || []
            const path = this.buildCategoryPath(allCats, course.categoryId)
            course.categoryPath = path.join(' > ')
            const cat = allCats.find(c => c.id === course.categoryId)
            if (cat && cat.parentId != null) {
              const ancestor = this.findPricedAncestor(allCats, cat.parentId)
              this.setData({
                belongCategory: ancestor || (cat.price > 0 || cat.isFree === 0 ? cat : null)
              })
            }
            this.setData({ course })
          } catch (e) {}
        }
      }
    } catch (e) {
      this.setData({
        course: { id: courseId, title: '加载中...', price: 0, categoryName: '', studentCount: 0, description: '' }
      })
    }

    try {
      const res = await app.get('/courses/' + courseId + '/chapters')
      this.setData({ chapters: res.data || [] })
    } catch (e) {
      this.setData({ chapters: [] })
    }

    try {
      const res = await app.get('/courses/' + courseId + '/access')
      this.setData({
        purchased: res.data?.accessible,
        freeChapterCount: res.data?.freeChapterCount || 0
      })
    } catch (e) {
      this.setData({ purchased: false, freeChapterCount: 0 })
    }

    if (this.data.course.courseType === 'OFFLINE') {
      try {
        const res = await app.get('/checkin/status/' + courseId)
        this.setData({ checkedIn: res.data?.checkedIn })
      } catch (e) {
        this.setData({ checkedIn: false })
      }
    }
  },

  buildCategoryPath(allCats, catId, path = []) {
    const cat = allCats.find(c => c.id === catId)
    if (!cat) return path
    path.unshift(cat.name)
    if (cat.parentId) {
      return this.buildCategoryPath(allCats, cat.parentId, path)
    }
    return path
  },

  findPricedAncestor(allCats, parentId) {
    const parent = allCats.find(c => c.id === parentId)
    if (!parent) return null
    if (parent.price > 0 || parent.isFree === 0) return parent
    if (parent.parentId) return this.findPricedAncestor(allCats, parent.parentId)
    return null
  },

  toggleDesc() {
    this.setData({ descExpanded: !this.data.descExpanded })
  },

  goChapter(e) {
    const ch = e.currentTarget.dataset.ch
    if (this.data.isPaid && !this.data.purchased && !ch.free) {
      wx.showToast({ title: '请先购买课程', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/video-player/video-player?chapterId=' + ch.id })
  },

  goPractice(e) {
    const ch = e.currentTarget.dataset.ch
    if (this.data.isPaid && !this.data.purchased && !ch.free) {
      wx.showToast({ title: '请先购买课程', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/practice-home/practice-home?chapterId=' + ch.id })
  },

  startLearn() {
    if (this.data.chapters.length > 0) {
      wx.navigateTo({ url: '/pages/video-player/video-player?chapterId=' + this.data.chapters[0].id })
    } else {
      wx.showToast({ title: '暂无章节内容', icon: 'none' })
    }
  },

  buyNow() {
    wx.navigateTo({ url: '/pages/order-confirm/order-confirm?courseId=' + this.data.courseId })
  },

  buyCategory() {
    if (this.data.belongCategory) {
      wx.navigateTo({ url: '/pages/order-confirm/order-confirm?categoryId=' + this.data.belongCategory.id })
    }
  },

  goCheckin() {
    wx.navigateTo({ url: '/pages/offline-checkin/offline-checkin?courseId=' + this.data.courseId })
  },

  goReserve() {
    wx.navigateTo({ url: '/pages/course-reservation/course-reservation?id=' + this.data.courseId })
  }
})
