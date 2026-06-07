import axios from 'axios'
import { getToken } from '../utils/auth'

const uploadInstance = axios.create({
  baseURL: '/api',
  timeout: 60000
})

uploadInstance.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return uploadInstance.post('/qa/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data)
}
