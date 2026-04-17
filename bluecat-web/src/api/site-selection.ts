import { get, post } from '@/utils/request'

/**
 * 新店选址 API
 */

// 分析选址位置
export const siteSelectionApi = {
  analyze: (data: {
    longitude: number
    latitude: number
    radius: number
    startTime?: string
    endTime?: string
  }) => post('/site-selection/analyze', data),

  quickAnalyze: (longitude: number, latitude: number, radius: number = 1000) =>
    get('/site-selection/quick-analyze', { longitude, latitude, radius })
}
