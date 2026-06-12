// pages/practice-question/practice-question.js
const app = getApp()

const TYPE_MAP = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }

Page({
  data: {
    chapterId: '',
    questions: [],
    answers: {},
    currentIndex: 0,
    currentQuestion: null,     // 当前题目（同步到 data 确保 WXML 可访问）
    typeLabel: '',             // 当前题型标签
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

  // 同步 currentQuestion 和 typeLabel 到 data 中，确保 WXML 能访问
  syncCurrentQuestion() {
    const q = this.data.questions[this.data.currentIndex] || null
    const typeLabel = q ? (TYPE_MAP[q.type] || '') : ''
    this.setData({ currentQuestion: q, typeLabel })
  },

  async fetchQuestions() {
    try {
      const res = await app.get('/chapters/' + this.data.chapterId + '/questions')
      let questions = []
      if (res) {
        questions = Array.isArray(res.data) ? res.data
          : Array.isArray(res) ? res
          : []
      }
      // 初始化答案
      const answers = {}
      questions.forEach(q => {
        if (q.type === 'MULTIPLE') answers[q.id] = []
        else if (q.type === 'ESSAY') answers[q.id] = ''
        else answers[q.id] = null
      })
      this.setData({ questions, answers })
      this.syncCurrentQuestion()
    } catch (e) {
      // app.js 已显示具体错误信息，这里用模态框确保用户看清
      const msg = (e && e.message) || '加载题目失败'
      wx.showModal({
        title: '提示',
        content: msg.includes('权限') || msg.includes('开通') ? msg : '加载题目失败，请稍后重试',
        showCancel: false,
        confirmText: '知道了',
        confirmColor: '#0052D9',
        success: () => {
          wx.navigateBack()
        }
      })
    }
  },

  selectSingle(e) {
    const idx = e.currentTarget.dataset.idx
    const q = this.data.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: idx })
  },

  toggleMultiple(e) {
    const idx = e.currentTarget.dataset.idx
    const q = this.data.currentQuestion
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
    const q = this.data.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: val })
  },

  onEssayInput(e) {
    const q = this.data.currentQuestion
    if (!q) return
    this.setData({ ['answers.' + q.id]: e.detail.value })
  },

  prevQuestion() {
    if (this.data.currentIndex > 0) {
      const newIndex = this.data.currentIndex - 1
      this.setData({ currentIndex: newIndex })
      this.syncCurrentQuestion()
    }
  },

  nextQuestion() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      const newIndex = this.data.currentIndex + 1
      this.setData({ currentIndex: newIndex })
      this.syncCurrentQuestion()
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
