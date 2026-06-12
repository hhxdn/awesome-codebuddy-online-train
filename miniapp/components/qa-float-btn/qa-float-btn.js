Component({
  properties: {
    show: {
      type: Boolean,
      value: true
    }
  },
  methods: {
    goQaSubmit() {
      wx.navigateTo({
        url: '/pages/qa-submit/qa-submit'
      })
    }
  }
})
