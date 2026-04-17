import { get, post, put, del } from '@/utils/request'

// 机器接口
export const machineApi = {
  page: (params: any) => get('/machine/page', params),
  listByShop: (shopId: number) => get(`/machine/list/${shopId}`),
  getById: (id: number) => get(`/machine/${id}`),
  save: (data: any) => post('/machine', data),
  update: (data: any) => put('/machine', data),
  delete: (id: number) => del(`/machine/${id}`)
}
