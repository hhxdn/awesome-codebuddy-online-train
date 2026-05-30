// pages/chapter-list/chapter-list.js
const app = getApp()
Page({
  data: { courseId: '', courseName: '', chapters: [], progress: 0, finishedCount: 0 },
  onLoad(options) {
    this.setData({ courseId: options.id || options.courseId })
    this.fetchChapters()
  },
  async fetchChapters() {
    try {
      const res = await app.get('/courses/' + this.data.courseId + '/chapters')
      const chapters = res.data || []
      const finishedCount = chapters.filter(c => c.completed).length
      const progress = chapters.length > 0 ? Math.round(finishedCount / chapters.length * 100) : 0
      this.setData({ chapters, finishedCount, progress, courseName: chapters[0]?.courseName || '' })
    } catch (e) { this.setData({ chapters: [] }) }
  },
  goChapter(e) { wx.navigateTo({ url: '/pages/video-player/video-player?chapterId=' + e.currentTarget.dataset.id }) },
  goPractice(e) { wx.navigateTo({ url: '/pages/practice-home/practice-home?chapterId=' + e.currentTarget.dataset.id }) }
})
