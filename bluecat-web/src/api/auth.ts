import { get, post } from '@/utils/request'

// 认证接口
export const authApi = {
  login: (data: { username: string; password: string }) => post('/auth/login', data),
  logout: () => post('/auth/logout'),
  info: () => get('/auth/info')
}
