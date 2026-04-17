import { get, post, put, del } from '@/utils/request'

// 门店接口
export const shopApi = {
  page: (params: any) => get('/shop/page', params),
  list: (configId?: number) => get('/shop/list', { configId }),
  getById: (id: number) => get(`/shop/${id}`),
  listAreas: (shopId: number) => get(`/shop/area/${shopId}`),
  save: (data: any) => post('/shop', data),
  update: (data: any) => put('/shop', data),
  delete: (id: number) => del(`/shop/${id}`)
}

// 门店PK关系接口
export const shopPkRelationApi = {
  // 获取PK关系列表
  list: () => get('/shop/pk/relation/list'),
  // 获取PK关系详情
  getById: (id: number) => get(`/shop/pk/relation/${id}`),
  // 新增PK关系
  add: (data: any) => post('/shop/pk/relation', data),
  // 编辑PK关系
  update: (id: number, data: any) => put(`/shop/pk/relation/${id}`, data),
  // 删除PK关系
  delete: (id: number) => del(`/shop/pk/relation/${id}`)
}
