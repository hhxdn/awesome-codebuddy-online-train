import axios from 'axios'
import { getToken, removeToken } from '../utils/auth'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

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
    // If the response has a code field and it's not 200, treat as error
    if (res.code !== undefined && res.code !== 200 && res.code !== 0) {
      showToast(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        removeToken()
        showToast('登录已过期，请重新登录')
        setTimeout(() => {
          window.location.href = '/#/login'
          window.location.reload()
        }, 1000)
      } else {
        showToast(error.response.data?.message || '网络异常')
      }
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
