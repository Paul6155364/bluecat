import { get } from '@/utils/request'

// 状态快照接口
export const statusApi = {
  // 获取所有门店最新状态
  listRealtime: (configId?: number, shopId?: number) => get('/status/realtime', { configId, shopId }),
  // 获取门店最新状态
  getRealtimeByShopId: (shopId: number) => get(`/status/realtime/${shopId}`),
  // 分页查询快照
  pageSnapshot: (params: any) => get('/status/snapshot/page', params),
  // 获取快照详情
  getSnapshotDetail: (snapshotId: number) => get(`/status/snapshot/${snapshotId}`),
  // 分页查询机器状态历史
  pageMachineHistory: (params: any) => get('/status/machine/history', params)
}
