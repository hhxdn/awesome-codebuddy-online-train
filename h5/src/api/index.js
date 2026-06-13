import axios from 'axios'
import { getToken, clearAuth } from '../utils/auth'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Token过期处理（防重复跳转）
let isRedirecting = false
function handleAuthExpired(message) {
  if (isRedirecting) return
  isRedirecting = true
  clearAuth()
  showToast(message || '登录已过期，请重新登录')
  setTimeout(() => {
    // 保存当前路径，登录后可跳回
    const currentPath = window.location.hash.replace('#', '') || '/'
    if (currentPath !== '/login') {
      sessionStorage.setItem('redirect_path', currentPath)
    }
    window.location.href = '/#/login'
  }, 800)
}

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回 code: 401 表示 token 过期/无效
    if (res.code === 401) {
      handleAuthExpired(res.message || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.message || '登录已过期'))
    }
    // 业务错误码（非 200 非 0）— 不在此处弹Toast，交给业务页面自行处理
    if (res.code !== undefined && res.code !== 200 && res.code !== 0) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      // HTTP 401（理论上后端返回了 code:401 但 status 可能是 200，这里做兼容）
      if (status === 401 || status === 403) {
        handleAuthExpired('登录已过期，请重新登录')
      } else if (status >= 500) {
        showToast('服务器异常，请稍后重试')
      } else {
        showToast(error.response.data?.message || '网络异常')
      }
    } else if (error.code === 'ECONNABORTED') {
      // 超时不提示
    } else {
      showToast('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export function get(url, params = {}) {
  return request.get(url, { params })
}

export function post(url, data = {}) {
  return request.post(url, data)
}

export function put(url, data = {}) {
  return request.put(url, data)
}

export function del(url, params = {}) {
  return request.delete(url, { params })
}

export default request
