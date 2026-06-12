// pages/exam-question/exam-question.js
const app = getApp()

const TYPE_MAP = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', ESSAY: '简答题' }

Page({
  data: {
    recordId: '',
    questions: [],
    answers: {},
    currentIndex: 0,
    currentQuestion: null,     // 当前题目（同步到 data 确保 WXML 可访问）
    typeLabel: '',             // 当前题型标签
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
    this.fetchQuestions()     // 加载完题目后再启动倒计时（见 fetchQuestions）
    this.startCheatDetection()
  },

  onUnload() {
    if (this.data.timer) clearInterval(this.data.timer)
  },

  onHide() {
    // 记录切出
    this.setData({ cheatCount: this.data.cheatCount + 1 })
  },

  // 同步 currentQuestion 和 typeLabel 到 data 中，确保 WXML 能访问
  syncCurrentQuestion() {
    const q = this.data.questions[this.data.currentIndex] || null
    const typeLabel = q ? (TYPE_MAP[q.type] || '') : ''
    this.setData({ currentQuestion: q, typeLabel })
  },

  async fetchQuestions() {
    try {
      const res = await app.get('/exam/records/' + this.data.recordId + '/questions')
      // 兼容后端多种返回格式：{ data: { questions: [...] } } 或 { data: [...] } 或直接数组
      let questions = []
      let duration = 3600  // 默认60分钟（后端返回秒数）
      if (res) {
        const result = res.data || {}
        // 兼容多种响应格式
        if (Array.isArray(result.questions)) {
          questions = result.questions
        } else if (Array.isArray(result)) {
          questions = result
        } else if (Array.isArray(res)) {
          questions = res
        }
        // duration：后端返回秒数（已由 paper.getDurationMinutes() * 60 计算），直接使用
        const rawDuration = result.duration || result.examDuration || result.totalDuration
        if (rawDuration) {
          duration = parseInt(rawDuration)
        }
      }
      const answers = {}
      questions.forEach(q => {
        if (q.type === 'MULTIPLE') answers[q.id] = []
        else if (q.type === 'ESSAY') answers[q.id] = ''
        else answers[q.id] = null
      })
      this.setData({ questions, answers, totalSeconds: duration })
      this.syncCurrentQuestion()
      this.updateCountdown()
      this.startCountdown()   // 加载完题目后再开始倒计时
    } catch (e) {
      wx.showModal({
        title: '提示',
        content: '加载题目失败，请返回重试',
        showCancel: false,
        confirmText: '知道了',
        confirmColor: '#0052D9'
      })
    }
  },

  isAnswered(qId) {
    const ans = this.data.answers[qId]
    if (ans === undefined || ans === null || ans === '') return false
    if (Array.isArray(ans)) return ans.length > 0
    return true
  },

  startCountdown() {
    if (this.data.timer) clearInterval(this.data.timer)
    this.data.timer = setInterval(() => {
      if (this.data.totalSeconds <= 0) {
        clearInterval(this.data.timer)
        this.data.timer = null
        this.submitExam(true)  // 自动提交，不弹确认框
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

  jumpTo(e) {
    const newIndex = e.currentTarget.dataset.idx
    this.setData({ currentIndex: newIndex })
    this.syncCurrentQuestion()
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

  async submitExam(isAuto = false) {
    if (this.data.submitting) return
    if (this.data.timer) {
      clearInterval(this.data.timer)
      this.data.timer = null
    }

    // 自动提交不弹确认框
    if (isAuto) {
      await this.doSubmit()
      return
    }

    // 二次确认
    wx.showModal({
      title: '确认交卷',
      content: '确定要提交试卷吗？交卷后将无法修改答案。',
      success: async (res) => {
        if (!res.confirm) {
          // 重新开始倒计时
          if (this.data.totalSeconds > 0) {
            this.startCountdown()
          }
          return
        }
        await this.doSubmit()
      }
    })
  },

  async doSubmit() {
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
      // 重新开始倒计时
      if (this.data.totalSeconds > 0) {
        this.startCountdown()
      }
    }
    this.setData({ submitting: false })
  }
})
