<template>
  <div class="site-selection">
    <!-- 操作面板 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-row :gutter="16" align="middle">
        <a-col :span="6">
          <div class="control-item">
            <span class="label">分析半径：</span>
            <a-select v-model:value="radius" style="width: 150px" @change="handleRadiusChange">
              <a-select-option :value="500">500米</a-select-option>
              <a-select-option :value="1000">1公里</a-select-option>
              <a-select-option :value="2000">2公里</a-select-option>
              <a-select-option :value="3000">3公里</a-select-option>
              <a-select-option :value="5000">5公里</a-select-option>
            </a-select>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="control-item">
            <span class="label">统计时间：</span>
            <a-select v-model:value="timeRange" style="width: 150px" @change="handleTimeRangeChange">
              <a-select-option value="7">近7天</a-select-option>
              <a-select-option value="15">近15天</a-select-option>
              <a-select-option value="30">近30天</a-select-option>
              <a-select-option value="custom">自定义</a-select-option>
            </a-select>
          </div>
        </a-col>
        <a-col :span="6" v-if="timeRange === 'custom'">
          <a-range-picker
            v-model:value="customDateRange"
            :placeholder="['开始日期', '结束日期']"
            style="width: 100%"
            @change="handleCustomDateChange"
          />
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="handleAnalyze" :loading="analyzing">
            <search-outlined /> 开始分析
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <!-- 地图和分析结果 -->
    <a-row :gutter="16">
      <!-- 地图区域 -->
      <a-col :span="16">
        <a-card title="地图选点">
          <template #extra>
            <a-space>
              <span style="color: #999; font-size: 12px">点击地图选择位置</span>
              <a-button size="small" @click="resetMap">重置</a-button>
            </a-space>
          </template>
          <div ref="mapContainerRef" id="tmap-container" style="height: 600px; width: 100%; position: relative;">
            <div v-if="!mapLoaded" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);">
              <a-spin size="large" tip="地图加载中..." />
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 分析结果 -->
      <a-col :span="8">
        <a-card title="分析结果" style="margin-bottom: 16px" v-if="analysisResult">
          <!-- 推荐指数 -->
          <div class="recommendation-section">
            <div class="score-circle" :style="{ borderColor: getRecommendationColor(analysisResult.recommendationScore) }">
              <div class="score-value">{{ analysisResult.recommendationScore }}</div>
              <div class="score-label">{{ analysisResult.recommendationLevel }}</div>
            </div>
          </div>

          <!-- 关键指标 -->
          <div class="metrics-grid">
            <div class="metric-item">
              <div class="metric-value">{{ analysisResult.totalShops }}</div>
              <div class="metric-label">附近门店</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ analysisResult.totalMachines }}</div>
              <div class="metric-label">机器总数</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ analysisResult.avgOccupancyRate }}%</div>
              <div class="metric-label">平均上座率</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ analysisResult.competitorCount }}</div>
              <div class="metric-label">竞争品牌</div>
            </div>
          </div>

          <!-- 饱和度和竞争度 -->
          <a-divider style="margin: 16px 0" />
          <div class="level-section">
            <div class="level-item">
              <span class="level-label">市场饱和度</span>
              <a-progress
                :percent="analysisResult.saturationScore"
                :strokeColor="getScoreColor(analysisResult.saturationScore)"
                size="small"
              />
              <span class="level-text">{{ analysisResult.saturationLevel }}</span>
            </div>
            <div class="level-item">
              <span class="level-label">竞争激烈度</span>
              <a-progress
                :percent="analysisResult.competitionScore"
                :strokeColor="getScoreColor(analysisResult.competitionScore)"
                size="small"
              />
              <span class="level-text">{{ analysisResult.competitionLevel }}</span>
            </div>
          </div>

          <!-- 综合建议 -->
          <a-divider style="margin: 16px 0" />
          <div class="suggestion-section">
            <div class="suggestion-title">
              <bulb-outlined /> 综合建议
            </div>
            <div class="suggestion-content">{{ analysisResult.suggestion }}</div>
          </div>
        </a-card>

        <!-- 品牌分布 -->
        <a-card title="品牌分布" v-if="analysisResult && analysisResult.brandDistribution.length > 0">
          <div
            v-for="brand in analysisResult.brandDistribution"
            :key="brand.configId"
            class="brand-item"
          >
            <div class="brand-header">
              <span class="brand-name">{{ brand.configName }}</span>
              <a-tag>{{ brand.shopCount }}家</a-tag>
            </div>
            <div class="brand-stats">
              <span>机器: {{ brand.machineCount }}台</span>
              <span>上座率: {{ brand.avgOccupancyRate }}%</span>
              <span>占比: {{ brand.percentage }}%</span>
            </div>
            <a-progress
              :percent="brand.percentage"
              :showInfo="false"
              strokeColor="#1890ff"
              size="small"
            />
          </div>
        </a-card>

        <!-- 附近门店列表 -->
        <a-card title="附近门店" style="margin-top: 16px" v-if="analysisResult && analysisResult.nearbyShops.length > 0">
          <div class="shop-list">
            <div
              v-for="shop in analysisResult.nearbyShops.slice(0, 10)"
              :key="shop.id"
              class="shop-item"
            >
              <div class="shop-header">
                <span class="shop-name">{{ shop.name }}</span>
                <a-tag :color="shop.occupancyRate > 60 ? 'green' : 'orange'" size="small">
                  上座率 {{ shop.occupancyRate }}%
                </a-tag>
              </div>
              <div class="shop-detail">
                <span>{{ shop.zoneName || '-' }}</span>
                <span>距离: {{ shop.distance }}米</span>
              </div>
              <div class="shop-stats">
                <span>机器: {{ shop.totalMachines }}台</span>
                <span v-if="shop.configName" class="brand-tag">{{ shop.configName }}</span>
              </div>
            </div>
          </div>
          <div v-if="analysisResult.nearbyShops.length > 10" style="text-align: center; margin-top: 8px">
            <a-tag>还有 {{ analysisResult.nearbyShops.length - 10 }} 家门店</a-tag>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { SearchOutlined, BulbOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { siteSelectionApi } from '@/api/site-selection'
import type { Dayjs } from 'dayjs'
import { TMAP_KEY, DEFAULT_CENTER, DEFAULT_ZOOM } from '@/config/map'

// 控制参数
const radius = ref(1000)
const timeRange = ref('30')
const customDateRange = ref<[Dayjs, Dayjs]>()

// 地图相关
const mapLoading = ref(false)
const mapLoaded = ref(false)
const mapContainerRef = ref<HTMLElement | null>(null)
let mapInstance: any = null
let markerLayer: any = null  // MultiMarker 图层
let circle: any = null
let shopMarkerLayer: any = null  // 门店标记图层

// 选址位置
const selectedLocation = reactive({
  longitude: 0,
  latitude: 0
})

// 分析结果
const analysisResult = ref<any>(null)
const analyzing = ref(false)

// 动态加载腾讯地图 SDK
const loadTMapSDK = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if ((window as any).TMap) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${TMAP_KEY}&callback=initTMap`
    
    // 设置超时
    const timeout = setTimeout(() => {
      reject(new Error('地图加载超时，请检查网络连接'))
    }, 15000)

    // 定义回调函数
    ;(window as any).initTMap = () => {
      clearTimeout(timeout)
      console.log('腾讯地图 SDK 加载成功')
      resolve()
    }

    script.onerror = () => {
      clearTimeout(timeout)
      reject(new Error('腾讯地图 SDK 加载失败，请检查网络或 Key 配置'))
    }

    document.head.appendChild(script)
  })
}

// 初始化地图
const initMap = async () => {
  try {
    mapLoading.value = true
    
    console.log('开始加载腾讯地图 SDK...')
    console.log('腾讯地图 Key:', TMAP_KEY)
    
    // 加载腾讯地图 SDK
    await loadTMapSDK()
    
    // 等待 DOM 渲染完成
    await nextTick()
    
    // 延迟确保容器元素完全渲染（移除 loading 后需要时间）
    await new Promise(resolve => setTimeout(resolve, 200))

    // 检查容器元素
    const container = mapContainerRef.value || document.getElementById('tmap-container')
    console.log('容器元素:', container)
    console.log('mapContainerRef.value:', mapContainerRef.value)
    console.log('getElementById:', document.getElementById('tmap-container'))
    
    if (!container) {
      throw new Error('地图容器元素未找到，请刷新页面重试')
    }

    console.log('地图容器元素 ID:', container.id)

    // 检查 TMap 是否存在
    const TMap = (window as any).TMap
    if (!TMap) {
      throw new Error('TMap 对象未定义')
    }

    console.log('开始创建地图实例...')

    // 创建地图实例 - 使用 DOM 元素而不是字符串 ID
    mapInstance = new TMap.Map(container, {
      center: new TMap.LatLng(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng), // 成都中心
      zoom: DEFAULT_ZOOM,
      viewMode: '2D'
    })

    if (!mapInstance) {
      throw new Error('地图实例创建失败')
    }

    console.log('地图实例创建成功')

    // 地图点击事件
    mapInstance.on('click', (evt: any) => {
      const lat = evt.latLng.getLat()
      const lng = evt.latLng.getLng()
      
      selectedLocation.latitude = lat
      selectedLocation.longitude = lng

      message.success(`已选择位置: ${lng.toFixed(4)}, ${lat.toFixed(4)}`)

      // 更新标记
      updateMarker(lat, lng)
    })

    mapLoaded.value = true
    console.log('地图初始化完成')
  } catch (error: any) {
    console.error('地图初始化失败:', error)
    
    let errorMsg = '地图加载失败，'
    
    if (error.message.includes('超时')) {
      errorMsg += '请检查网络连接'
    } else if (error.message.includes('Key')) {
      errorMsg += '请检查 Key 配置是否正确'
    } else if (error.message.includes('SDK')) {
      errorMsg += 'SDK 加载失败，请检查网络或 Key 是否有效'
    } else {
      errorMsg += '请刷新重试或查看控制台错误信息'
    }
    
    message.error({
      content: errorMsg,
      duration: 5
    })
  } finally {
    mapLoading.value = false
  }
}

// 更新标记
const updateMarker = (lat: number, lng: number) => {
  const TMap = (window as any).TMap

  if (!TMap) {
    console.error('TMap 未定义')
    return
  }

  if (!mapInstance) {
    console.error('地图实例未初始化')
    return
  }

  console.log('开始更新标记: lat=', lat, 'lng=', lng, 'radius=', radius.value)

  try {
    // 移除旧标记图层
    if (markerLayer) {
      markerLayer.setMap(null)
      markerLayer = null
    }
    if (circle) {
      circle.setMap(null)
      circle = null
    }

    // 创建标记图层
    markerLayer = new TMap.MultiMarker({
      map: mapInstance,
      styles: {
        // 默认红色标记样式
        'marker': new TMap.MarkerStyle({
          width: 25,
          height: 35,
          anchor: { x: 16, y: 32 }
        })
      },
      geometries: [{
        id: 'selected-point',
        styleId: 'marker',
        position: new TMap.LatLng(lat, lng),
        properties: {
          title: '选中位置'
        }
      }]
    })

    console.log('标记图层创建成功')

    // 添加分析范围圆圈 - 使用 MultiCircle
    circle = new TMap.MultiCircle({
      map: mapInstance,
      styles: {
        'circle': new TMap.CircleStyle({
          color: 'rgba(24, 144, 255, 0.16)',  // 蓝色填充
          showBorder: true,
          borderColor: 'rgba(24, 144, 255, 1)',  // 蓝色边框
          borderWidth: 2
        })
      },
      geometries: [{
        styleId: 'circle',
        center: new TMap.LatLng(lat, lng),
        radius: radius.value
      }]
    })

    console.log('圆形区域创建成功')

    // 调整视野，让圆形完全可见
    const bounds = circle.getBounds()
    if (bounds) {
      mapInstance.fitBounds(bounds, { padding: 50 })
    }
  } catch (error) {
    console.error('创建标记失败:', error)
  }
}

// 更新附近门店标记
const updateShopMarkers = () => {
  const TMap = (window as any).TMap

  // 清除旧标记图层
  if (shopMarkerLayer) {
    shopMarkerLayer.setMap(null)
    shopMarkerLayer = null
  }

  if (!analysisResult.value || !analysisResult.value.nearbyShops) return

  const geometries: any[] = []

  // 创建门店标记数据
  analysisResult.value.nearbyShops.forEach((shop: any) => {
    const color = shop.occupancyRate > 60 ? '#52c41a' : shop.occupancyRate > 30 ? '#faad14' : '#ff4d4f'
    const styleId = color === '#52c41a' ? 'green' : color === '#faad14' ? 'orange' : 'red'

    geometries.push({
      id: `shop-${shop.id}`,
      styleId: styleId,
      position: new TMap.LatLng(shop.latitude, shop.longitude),
      properties: {
        title: shop.name,
        occupancyRate: shop.occupancyRate,
        totalMachines: shop.totalMachines,
        distance: shop.distance,
        color: color
      }
    })
  })

  // 创建 SVG 图标（彩色圆点）
  const createCircleIcon = (color: string) => {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20">
      <circle cx="10" cy="10" r="8" fill="${color}" stroke="#fff" stroke-width="2"/>
    </svg>`
    return 'data:image/svg+xml;base64,' + btoa(svg)
  }

  // 创建门店标记图层
  shopMarkerLayer = new TMap.MultiMarker({
    map: mapInstance,
    styles: {
      'green': new TMap.MarkerStyle({
        width: 20,
        height: 20,
        anchor: { x: 10, y: 10 },
        src: createCircleIcon('#52c41a')
      }),
      'orange': new TMap.MarkerStyle({
        width: 20,
        height: 20,
        anchor: { x: 10, y: 10 },
        src: createCircleIcon('#faad14')
      }),
      'red': new TMap.MarkerStyle({
        width: 20,
        height: 20,
        anchor: { x: 10, y: 10 },
        src: createCircleIcon('#ff4d4f')
      })
    },
    geometries: geometries
  })

  // 添加点击事件
  shopMarkerLayer.on('click', (evt: any) => {
    const shop = evt.geometry.properties
    message.info(`${shop.title}\n上座率: ${shop.occupancyRate}%\n机器: ${shop.totalMachines}台\n距离: ${shop.distance}米`)
  })

  console.log('门店标记创建成功，共', geometries.length, '个')
}

// 重置地图
const resetMap = () => {
  selectedLocation.longitude = 0
  selectedLocation.latitude = 0
  analysisResult.value = null

  // 移除标记图层
  if (markerLayer) {
    markerLayer.setMap(null)
    markerLayer = null
  }
  if (circle) {
    circle.setMap(null)
    circle = null
  }

  // 移除门店标记图层
  if (shopMarkerLayer) {
    shopMarkerLayer.setMap(null)
    shopMarkerLayer = null
  }

  message.info('已重置')
}

// 处理半径变化
const handleRadiusChange = () => {
  if (selectedLocation.longitude && selectedLocation.latitude) {
    updateMarker(selectedLocation.latitude, selectedLocation.longitude)
  }
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  // 时间范围改变时不需要立即处理
}

// 处理自定义日期变化
const handleCustomDateChange = () => {
  // 日期改变时不需要立即处理
}

// 开始分析
const handleAnalyze = async () => {
  if (!selectedLocation.longitude || !selectedLocation.latitude) {
    message.warning('请先在地图上选择位置')
    return
  }

  analyzing.value = true

  try {
    let startTime: string | undefined
    let endTime: string | undefined

    if (timeRange.value === 'custom' && customDateRange.value) {
      startTime = customDateRange.value[0].startOf('day').format('YYYY-MM-DD HH:mm:ss')
      endTime = customDateRange.value[1].endOf('day').format('YYYY-MM-DD HH:mm:ss')
    } else if (timeRange.value !== 'custom') {
      const days = parseInt(timeRange.value)
      const end = new Date()
      const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000)
      startTime = start.toISOString().slice(0, 19).replace('T', ' ')
      endTime = end.toISOString().slice(0, 19).replace('T', ' ')
    }

    const res = await siteSelectionApi.analyze({
      longitude: selectedLocation.longitude,
      latitude: selectedLocation.latitude,
      radius: radius.value,
      startTime,
      endTime
    })

    analysisResult.value = res.data

    // 更新地图标记
    updateShopMarkers()

    message.success('分析完成')
  } catch (error: any) {
    message.error(error.message || '分析失败')
  } finally {
    analyzing.value = false
  }
}

// 获取推荐颜色
const getRecommendationColor = (score: number) => {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#1890ff'
  if (score >= 40) return '#faad14'
  return '#ff4d4f'
}

// 获取分数颜色（饱和度、竞争度）
const getScoreColor = (score: number) => {
  if (score < 25) return '#52c41a'
  if (score < 50) return '#1890ff'
  if (score < 75) return '#faad14'
  return '#ff4d4f'
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
  }
})
</script>

<style scoped lang="less">
.site-selection {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.control-item {
  display: flex;
  align-items: center;

  .label {
    margin-right: 8px;
    color: #666;
    font-size: 14px;
  }
}

.recommendation-section {
  text-align: center;
  margin-bottom: 20px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 6px solid #1890ff;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.metric-item {
  text-align: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  line-height: 1;
}

.metric-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.level-section {
  .level-item {
    margin-bottom: 12px;

    .level-label {
      display: inline-block;
      width: 80px;
      font-size: 13px;
      color: #666;
    }

    .ant-progress {
      display: inline-block;
      width: calc(100% - 140px);
      margin: 0 8px;
    }

    .level-text {
      display: inline-block;
      width: 40px;
      text-align: right;
      font-size: 13px;
      font-weight: 500;
    }
  }
}

.suggestion-section {
  background: #f0f7ff;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.suggestion-title {
  font-weight: 500;
  color: #1890ff;
  margin-bottom: 8px;
  font-size: 14px;
}

.suggestion-content {
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}

.brand-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    margin-bottom: 0;
    border-bottom: none;
  }
}

.brand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.brand-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.brand-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.shop-list {
  max-height: 400px;
  overflow-y: auto;
}

.shop-item {
  padding: 10px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #1890ff;
  transition: all 0.3s;

  &:hover {
    background: #f0f7ff;
  }
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.shop-name {
  font-weight: 500;
  color: #333;
  font-size: 13px;
}

.shop-detail {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.shop-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.brand-tag {
  padding: 2px 6px;
  background: #e6f7ff;
  border-radius: 4px;
  color: #1890ff;
}

#tmap-container {
  border-radius: 8px;
  overflow: hidden;
}
</style>
