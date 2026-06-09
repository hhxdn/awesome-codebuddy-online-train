import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 防止多个 401 响应同时触发重复跳转
let redirecting = false

function handleAuthExpired(message) {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('permissions')
  localStorage.removeItem('menus')
  ElMessage.error(message || '登录已过期，请重新登录')
  setTimeout(() => {
    window.location.href = '/#/login'
  }, 500)
}

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    // 业务层 401（Token无效或已过期）→ 清空认证信息并跳转登录
    if (res.code === 401) {
      handleAuthExpired(res.message)
      return Promise.reject(new Error(res.message || '登录已过期'))
    }
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      handleAuthExpired()
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export function get(url, params) {
  return service.get(url, { params })
}

export function post(url, data, config) {
  return service.post(url, data, config)
}

export function put(url, data) {
  return service.put(url, data)
}

export function del(url, params) {
  return service.delete(url, { params })
}

export default service
