import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

// 响应数据接口
export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data

    // code不是200则为错误
    if (res.code !== 200) {
      message.error(res.message || '请求失败')

      // 401 未登录或登录过期
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return response
    }
  },
  (error) => {
    console.error('Response error:', error)
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message.error('未登录或登录已过期')
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 403:
          message.error('无权限访问')
          break
        case 404:
          message.error('请求资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(error.message || '请求失败')
      }
    } else {
      message.error('网络异常,请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// 封装GET请求
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.get(url, { params, ...config }).then((res) => res.data)
}

// 封装POST请求
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.post(url, data, config).then((res) => res.data)
}

// 封装PUT请求
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.put(url, data, config).then((res) => res.data)
}

// 封装DELETE请求
export function del<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.delete(url, { params, ...config }).then((res) => res.data)
}

export default service
