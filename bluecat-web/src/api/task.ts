import { get, post } from '@/utils/request'

// 采集任务接口
export const taskApi = {
  // 手动触发所有配置采集
  executeAll: () => post('/task/execute'),
  // 手动触发指定配置采集
  executeByConfigId: (configId: number) => post(`/task/execute/${configId}`),
  // 分页查询采集任务
  page: (params: any) => get('/task/page', params),
  // 查询任务详情
  getById: (id: number) => get(`/task/${id}`),
  // 分页查询API调用日志
  logPage: (params: any) => get('/task/log/page', params),
  // 查询API调用日志详情
  getLogById: (id: number) => get(`/task/log/${id}`)
}
