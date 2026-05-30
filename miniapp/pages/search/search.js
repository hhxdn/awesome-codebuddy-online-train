// pages/search/search.js
const app = getApp()
Page({
  data: {
    keyword: '',
    results: [],
    loading: false,
    hotTags: ['Java', 'Spring', 'Vue', 'Python', 'AI', '数据库', '前端', '后端']
  },
  onInput(e) { this.setData({ keyword: e.detail.value }) },
  clearSearch() { this.setData({ keyword: '', results: [] }) },
  goBack() { wx.navigateBack() },
  searchTag(e) { this.setData({ keyword: e.currentTarget.dataset.tag }, () => { this.doSearch() }) },
  async doSearch() {
    if (!this.data.keyword.trim()) return
    this.setData({ loading: true })
    try {
      const res = await app.get('/courses', { keyword: this.data.keyword.trim() })
      this.setData({ results: res.data?.records || res.data || [] })
    } catch (e) { this.setData({ results: [] }) }
    this.setData({ loading: false })
  },
  goDetail(e) { wx.navigateTo({ url: '/pages/course-detail/course-detail?id=' + e.currentTarget.dataset.id }) }
})
