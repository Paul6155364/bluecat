import { get } from '@/utils/request'

export const reportApi = {
  /**
   * 获取网咖配置列表
   */
  listConfigs() {
    return get('/business-report/configs')
  },

  /**
   * 获取门店列表
   */
  listShops(configId: number) {
    return get('/business-report/shops', { configId })
  },

  /**
   * 生成经营数据分析报告（设置更长的超时时间）
   */
  generateReport(shopId: number, startDate: string, endDate: string) {
    return get('/business-report/generate', { shopId, startDate, endDate }, { timeout: 60000 })
  }
}
