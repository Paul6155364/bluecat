import { get, post, put, del } from '@/utils/request'

// 网吧配置接口
export const configApi = {
  page: (params: any) => get('/config/page', params),
  list: () => get('/config/list/enabled'),
  listEnabled: () => get('/config/list/enabled'),
  getById: (id: number) => get(`/config/${id}`),
  save: (data: any) => post('/config', data),
  update: (data: any) => put('/config', data),
  delete: (id: number) => del(`/config/${id}`),
  updateStatus: (id: number, status: number) => put(`/config/status/${id}?status=${status}`),
  testToken: (id: number) => post(`/config/test-token/${id}`),
  refreshToken: (id: number) => post(`/config/refresh-token/${id}`)
}
