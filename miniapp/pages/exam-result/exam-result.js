// pages/exam-result/exam-result.js
const app = getApp()

Page({
  data: {
    recordId: '',
    result: {},
    paperName: '',        // 考试名称
    score: 0,             // 分数（平铺，供WXML直接使用）
    totalScore: 0,        // 总分
    passed: false,        // 是否通过（平铺）
    correctCount: 0,      // 正确题数
    totalCount: 0,        // 总题数
    timeUsed: '',         // 用时
    details: []           // 答题详情
  },

  onLoad(options) {
    this.setData({ recordId: options.recordId })
    this.fetchResult()
  },

  async fetchResult() {
    try {
      const res = await app.get('/exam/records/' + this.data.recordId)
      const raw = res.data || {}
      const record = raw.record || {}
      const paper = raw.paper || {}
      const answers = raw.answers || []
      // 试卷总题数：优先用后端返回的 questionCount，其次用答题记录数量，回退到 paper 关联题目数
      const totalCount = raw.questionCount || answers.length || 0

      // 提取平铺字段
      const paperName = paper.title || raw.paperName || ''
      const score = record.score != null ? Number(record.score) : (raw.score || 0)
      const totalScore = paper.totalScore != null ? Number(paper.totalScore) : (raw.totalScore || 100)
      const passed = record.isPass === 1 || record.passed === true || raw.passed === true || false

      // 构建答题详情
      let correctCount = 0
      const details = answers.map(a => {
        const q = a.question || {}
        const isCorrect = a.isCorrect === 1 || a.isCorrect === true
        if (isCorrect) correctCount++
        return {
          questionId: a.id || q.id,
          type: q.type || 'SINGLE',
          content: q.content || '',
          correct: isCorrect,
          myAnswer: a.userAnswer != null ? String(a.userAnswer) : '',
          correctAnswer: q.answer != null ? String(q.answer) : '',
          analysis: q.analysis || ''
        }
      })

      // 计算用时
      let timeUsed = ''
      if (record.startTime && record.submitTime) {
        const diff = Math.round((new Date(record.submitTime) - new Date(record.startTime)) / 60000)
        timeUsed = diff + '分钟'
      }

      this.setData({
        result: raw,
        paperName,
        score,
        totalScore,
        passed,
        correctCount,
        totalCount,
        timeUsed,
        details
      })
    } catch (e) {
      wx.showToast({ title: '加载结果失败', icon: 'none' })
    }
  },

  goBack() {
    wx.switchTab({ url: '/pages/exam-list/exam-list' })
  }
})
