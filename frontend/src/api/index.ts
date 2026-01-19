import axios from 'axios'
import { ElMessage } from 'element-plus'

// 扩展 axios 配置类型，支持静默错误处理
declare module 'axios' {
  export interface AxiosRequestConfig {
    silentError?: boolean // 设置为 true 时不显示错误提示
  }
}

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const config = error.config
    const silentError = config?.silentError

    // 认证错误始终处理（清除 token 并跳转登录）
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/admin/login'
      return Promise.reject(error)
    }

    // 静默模式：不显示错误提示
    if (silentError) {
      console.warn('API Silent Error:', error.response?.data?.message || error.message)
      return Promise.reject(error)
    }

    // 根据错误类型显示不同的提示
    const status = error.response?.status
    const message = error.response?.data?.message || '请求失败'

    if (status >= 500) {
      ElMessage.error(`服务器错误：${message}`)
    } else if (status === 404) {
      ElMessage.warning('请求的资源不存在')
    } else if (status === 403) {
      ElMessage.warning('没有权限访问该资源')
    } else if (!error.response) {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default api
