// app.js
App({
  globalData: {
    baseUrl: 'http://127.0.0.1:8088/api', // 开发环境，真机调试需改为局域网IP
    token: '',
    userInfo: null,
    approvalChecked: false,
    currentApprovalStatus: null,
    hasProfile: false
  },

  onLaunch() {
    // 恢复登录态
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
  },

  // 检查审核状态
  async checkApprovalStatus() {
    if (this.globalData.approvalChecked) {
      return {
        approvalStatus: this.globalData.currentApprovalStatus,
        hasProfile: this.globalData.hasProfile
      }
    }
    try {
      const res = await this.request({
        url: '/user/check-status',
        method: 'GET'
      })
      this.globalData.currentApprovalStatus = res.data?.approvalStatus
      this.globalData.hasProfile = res.data?.hasProfile
      this.globalData.approvalChecked = true
      return {
        approvalStatus: res.data?.approvalStatus,
        hasProfile: res.data?.hasProfile
      }
    } catch (e) {
      return { approvalStatus: null, hasProfile: false }
    }
  },

  // 封装请求
  request(options) {
    const self = this
    const skipGlobalError = options.skipGlobalError || false
    return new Promise((resolve, reject) => {
      const header = {
        'Content-Type': 'application/json'
      }
      if (self.globalData.token) {
        header['Authorization'] = 'Bearer ' + self.globalData.token
      }

      wx.request({
        url: self.globalData.baseUrl + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        header,
        timeout: 15000,
        success(res) {
          // HTTP 401 是真正的认证过期，强制登出
          if (res.statusCode === 401) {
            wx.removeStorageSync('token')
            wx.removeStorageSync('userInfo')
            self.globalData.token = ''
            wx.showToast({ title: '登录已过期', icon: 'none' })
            setTimeout(() => {
              wx.reLaunch({ url: '/pages/login/login' })
            }, 800)
            reject(new Error('登录已过期'))
            return
          }

          // HTTP 403 可能是付费验证，不强制登出，交给业务层处理
          if (res.statusCode === 403) {
            const data = res.data
            const msg = (data && data.message) || '无权限访问'
            if (!skipGlobalError) {
              wx.showToast({ title: msg, icon: 'none' })
            }
            reject(new Error(msg))
            return
          }

          const data = res.data
          // 业务层401
          if (data && data.code === 401) {
            wx.removeStorageSync('token')
            wx.removeStorageSync('userInfo')
            self.globalData.token = ''
            wx.showToast({ title: data.message || '登录已过期', icon: 'none' })
            setTimeout(() => {
              wx.reLaunch({ url: '/pages/login/login' })
            }, 800)
            reject(new Error(data.message || '登录已过期'))
            return
          }

          if (data && data.code !== undefined && data.code !== 200 && data.code !== 0) {
            if (!skipGlobalError) {
              wx.showToast({ title: data.message || '请求失败', icon: 'none' })
            }
            reject(new Error(data.message || '请求失败'))
            return
          }

          resolve(data)
        },
        fail(err) {
          if (err.errMsg && err.errMsg.includes('timeout')) {
            // 超时不提示
          } else {
            wx.showToast({ title: '网络连接失败', icon: 'none' })
          }
          reject(err)
        }
      })
    })
  },

  // GET 请求
  get(url, params = {}) {
    const queryString = Object.keys(params)
      .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&')
    return this.request({
      url: queryString ? `${url}?${queryString}` : url,
      method: 'GET'
    })
  },

  // POST 请求
  post(url, data = {}) {
    return this.request({
      url,
      method: 'POST',
      data
    })
  },

  // PUT 请求
  put(url, data = {}) {
    return this.request({
      url,
      method: 'PUT',
      data
    })
  },

  // DELETE 请求
  del(url, params = {}) {
    const queryString = Object.keys(params)
      .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&')
    return this.request({
      url: queryString ? `${url}?${queryString}` : url,
      method: 'DELETE'
    })
  }
})
