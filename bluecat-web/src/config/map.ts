/**
 * 地图配置
 * 
 * 腾讯地图 Key 申请地址: https://lbs.qq.com/
 * 
 * 申请步骤：
 * 1. 访问腾讯位置服务官网 https://lbs.qq.com/
 * 2. 注册/登录账号
 * 3. 进入控制台 -> 应用管理 -> 我的应用 -> 创建应用
 * 4. 添加 Key，选择 "WebService API" 和 "JavaScript API"
 * 5. 将生成的 Key 复制到下方 TMAP_KEY 配置中
 */

// 您的 Key
export const TMAP_KEY = 'Q5ABZ-HSL34-JTBUE-D2PW4-ZKK76-MCBJA'

// 腾讯官方测试 Key（仅用于开发测试，有配额限制）
// export const TMAP_KEY = 'OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77'

/**
 * 默认地图中心点（成都）
 */
export const DEFAULT_CENTER = {
  lat: 30.67,
  lng: 104.06
}

/**
 * 默认缩放级别
 */
export const DEFAULT_ZOOM = 13
