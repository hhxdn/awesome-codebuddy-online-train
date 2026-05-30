// pages/practice-question/practice-question.js
const app = getApp()

Page({
  data: {
    chapterId: '',
    questions: [],
    answers: {},
    currentIndex: 0,
    submitting: false,
    optionLabels: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'],
    utils: {
      isMultiSelected(arr, idx) {
        return arr && arr.includes(idx)
      }
    }
  },

  onLoad(options) {
    this.setData({ chapterId: options.chapterId })
    this.fetchQuestions()
  },

  async fetchQuestions() {
    try {
      const res = await app.get('/chapters/' + this.data.chapterId + '/questions')
      const questions = res.data || []
      // 初始化答案
      const answers = {}
      questions.forEach(q => {
        if (q.type === 'MULTIPLE') answers[q.id] = []
        else if (q.type === 'ESSAY') answers[q.id] = ''
        else answers[q.id] = null
      })
      this.setData({ questions, answers })
    } catch (e) {
      wx.showToast({ title: '加载题目失败', icon: 'none' })
    }
  },

  get currentQuestion() {
    return this.data.questions[this.data.currentIndex] || null
  },

  get typeLabel() {
    const map = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }
    const q = this.currentQuestion
    return q ? (map[q.type] || '') : ''
  },

  selectSingle(e) {
    const idx = e.currentTarget.dataset.idx
    const q = this.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: idx })
  },

  toggleMultiple(e) {
    const idx = e.currentTarget.dataset.idx
    const q = this.currentQuestion
    if (!q) return
    const answers = this.data.answers
    let arr = answers[q.id] || []
    const pos = arr.indexOf(idx)
    if (pos >= 0) arr.splice(pos, 1)
    else arr.push(idx)
    this.setData({ ['answers.' + q.id]: arr })
  },

  selectJudge(e) {
    const val = e.currentTarget.dataset.val
    const q = this.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: val })
  },

  onEssayInput(e) {
    const q = this.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: e.detail.value })
  },

  prevQuestion() {
    if (this.data.currentIndex > 0) {
      this.setData({ currentIndex: this.data.currentIndex - 1 })
    }
  },

  nextQuestion() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.setData({ currentIndex: this.data.currentIndex + 1 })
    }
  },

  async submitAnswer() {
    if (this.data.submitting) return
    this.setData({ submitting: true })

    try {
      const answers = this.data.answers
      const answerList = Object.keys(answers).map(questionId => ({
        questionId: parseInt(questionId),
        answer: Array.isArray(answers[questionId])
          ? answers[questionId].join(',')
          : String(answers[questionId] ?? '')
      }))

      const res = await app.post('/practice/submit', {
        chapterId: parseInt(this.data.chapterId),
        answers: answerList
      })

      // 通过 navigateTo + eventChannel 传递结果（redirectTo 不支持 eventChannel）
      const resultData = res.data || {}
      wx.navigateTo({
        url: '/pages/practice-result/practice-result?chapterId=' + this.data.chapterId,
        success: (navRes) => {
          navRes.eventChannel.emit('practiceResult', resultData)
        }
      })
    } catch (e) {
      wx.showToast({ title: '提交失败', icon: 'none' })
    }
    this.setData({ submitting: false })
  }
})
