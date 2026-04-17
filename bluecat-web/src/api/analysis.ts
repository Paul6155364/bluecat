import { get } from '@/utils/request'

// 数据分析接口
export const analysisApi = {
  // 门店PK对比数据
  pkShops: (shopIds: number[], startTime?: string, endTime?: string) => 
    get('/analysis/pk/shops', { shopIds: shopIds.join(','), startTime, endTime }),
  // 高峰时段热力图数据
  heatmap: (shopId?: number, startDate?: string, endDate?: string) => 
    get('/analysis/heatmap', { shopId, startDate, endDate }),
  // 门店列表(简略)
  listShops: () => get('/analysis/shops')
}
