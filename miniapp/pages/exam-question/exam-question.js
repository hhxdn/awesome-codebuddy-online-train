// pages/exam-question/exam-question.js
const app = getApp()

Page({
  data: {
    recordId: '',
    questions: [],
    answers: {},
    currentIndex: 0,
    submitting: false,
    countdownText: '',
    urgent: false,
    cheatCount: 0,
    totalSeconds: 0,
    timer: null,
    optionLabels: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'],
    utils: {
      isMultiSelected(arr, idx) {
        return arr && arr.includes(idx)
      }
    }
  },

  onLoad(options) {
    this.setData({ recordId: options.recordId })
    this.fetchQuestions()
    this.startCountdown()
    // 监听切出
    this.startCheatDetection()
  },

  onUnload() {
    if (this.data.timer) clearInterval(this.data.timer)
  },

  onHide() {
    // 记录切出
    this.setData({ cheatCount: this.data.cheatCount + 1 })
  },

  async fetchQuestions() {
    try {
      const res = await app.get('/exam/records/' + this.data.recordId + '/questions')
      const questions = res.data || []
      const answers = {}
      questions.forEach(q => {
        if (q.type === 'MULTIPLE') answers[q.id] = []
        else if (q.type === 'ESSAY') answers[q.id] = ''
        else answers[q.id] = null
      })
      // 获取考试时长
      const duration = (res.data && res.data[0] && res.data[0].examDuration) || 60
      this.setData({ questions, answers, totalSeconds: duration * 60 })
      this.updateCountdown()
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

  isAnswered(qId) {
    const ans = this.data.answers[qId]
    if (ans === undefined || ans === null || ans === '') return false
    if (Array.isArray(ans)) return ans.length > 0
    return true
  },

  startCountdown() {
    this.data.timer = setInterval(() => {
      if (this.data.totalSeconds <= 0) {
        clearInterval(this.data.timer)
        this.submitExam()
        return
      }
      this.setData({ totalSeconds: this.data.totalSeconds - 1 })
      this.updateCountdown()
    }, 1000)
  },

  updateCountdown() {
    const s = this.data.totalSeconds
    const min = Math.floor(s / 60)
    const sec = s % 60
    this.setData({
      countdownText: `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`,
      urgent: s <= 300 // 5分钟变红
    })
  },

  startCheatDetection() {
    // 小程序自动检测 onHide 即为切出
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

  jumpTo(e) {
    this.setData({ currentIndex: e.currentTarget.dataset.idx })
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

  async submitExam() {
    if (this.data.submitting) return
    if (this.data.timer) clearInterval(this.data.timer)

    // 二次确认
    wx.showModal({
      title: '确认交卷',
      content: '确定要提交试卷吗？交卷后将无法修改答案。',
      success: async (res) => {
        if (!res.confirm) {
          this.startCountdown()
          return
        }
        this.setData({ submitting: true })

        try {
          const answers = this.data.answers
          const answerList = Object.keys(answers).map(questionId => ({
            questionId: parseInt(questionId),
            answer: Array.isArray(answers[questionId])
              ? answers[questionId].join(',')
              : String(answers[questionId] ?? '')
          }))

          await app.post('/exam/submit', {
            recordId: parseInt(this.data.recordId),
            answers: answerList,
            cheatCount: this.data.cheatCount
          })

          wx.redirectTo({ url: '/pages/exam-result/exam-result?recordId=' + this.data.recordId })
        } catch (e) {
          wx.showToast({ title: '提交失败', icon: 'none' })
          this.startCountdown()
        }
        this.setData({ submitting: false })
      }
    })
  }
})
