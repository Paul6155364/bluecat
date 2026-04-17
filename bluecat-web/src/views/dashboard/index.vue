<template>
  <div class="dashboard">
    <!-- 统计卡片 - 总览 -->
    <div class="stat-cards-header">
      <a-button v-if="configShopStats.length > 3" type="link" size="small" @click="toggleAllExpand">
        {{ allExpanded ? '全部收起' : '展开全部' }}
      </a-button>
    </div>
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <span>🏪</span>
          </div>
          <div class="stat-content">
            <div class="stat-total">{{ statistics.totalShops }}</div>
            <div class="stat-title">总门店数</div>
            <div class="stat-detail">
              <div v-for="stat in getVisibleStats(configShopStats, 'shops')" :key="stat.configId" class="detail-item">
                <span class="dot" :style="{ background: stat.color }"></span>
                <span>{{ stat.configName }}: {{ stat.count }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <span>💻</span>
          </div>
          <div class="stat-content">
            <div class="stat-total">{{ statistics.totalMachines }}</div>
            <div class="stat-title">总机器数</div>
            <div class="stat-detail">
              <div v-for="stat in getVisibleStats(configMachineStats, 'machines')" :key="stat.configId" class="detail-item">
                <span class="dot" :style="{ background: stat.color }"></span>
                <span>{{ stat.configName }}: {{ stat.count }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <span>✅</span>
          </div>
          <div class="stat-content">
            <div class="stat-total" style="color: #52c41a;">{{ statistics.freeMachines }}</div>
            <div class="stat-title">当前空闲</div>
            <div class="stat-detail">
              <div v-for="stat in getVisibleStats(configFreeStats, 'free')" :key="stat.configId" class="detail-item">
                <span class="dot" :style="{ background: stat.color }"></span>
                <span>{{ stat.configName }}: {{ stat.count }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <span>🔥</span>
          </div>
          <div class="stat-content">
            <div class="stat-total" style="color: #ff4d4f;">{{ statistics.busyMachines }}</div>
            <div class="stat-title">当前占用</div>
            <div class="stat-detail">
              <div v-for="stat in getVisibleStats(configBusyStats, 'busy')" :key="stat.configId" class="detail-item">
                <span class="dot" :style="{ background: stat.color }"></span>
                <span>{{ stat.configName }}: {{ stat.count }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 地图区域 -->
    <a-card title="门店地理分布" class="map-card" :loading="mapLoading">
      <template #extra>
        <a-space wrap>
          <a-switch
            v-model:checked="showHeatmap"
            checked-children="热力图"
            un-checked-children="散点图"
            @change="handleModeChange"
          />
          <a-switch
            v-model:checked="showCluster"
            checked-children="聚合"
            un-checked-children="散点"
            @change="handleModeChange"
          />
          <span
            v-for="config in filteredConfigList"
            :key="config.id"
            class="legend-item"
            @click="filterByConfig(config.id)"
            :class="{ active: selectedConfigId === config.id }"
          >
            <span class="dot" :style="{ background: getConfigColor(config.id) }"></span>
            <span>{{ config.configName || '未分类' }} ({{ getShopCountByConfig(config.id) }}家)</span>
          </span>
        </a-space>
      </template>
      <a-row :gutter="16">
        <a-col :span="18">
          <div ref="mapChart" style="height: 500px; width: 100%;"></div>
        </a-col>
        <a-col :span="6" style="height: 500px; overflow-y: auto;">
          <!-- 区域统计 -->
          <div class="zone-stats" v-if="zoneStats.length > 0">
            <div class="zone-stats-header">
              <span class="title">区域统计</span>
              <a-tag color="blue">{{ zoneStats.length }}个区域</a-tag>
            </div>
            <div class="zone-stats-content">
              <div
                v-for="zone in zoneStats"
                :key="zone.name"
                class="zone-item"
                :class="{ 'zone-selected': selectedZone === zone.name }"
                @click="handleZoneClick(zone.name)"
              >
                <div class="zone-name">{{ zone.name || '未分类' }}</div>
                <div class="zone-data">
                  <span class="zone-count">{{ zone.count }}家</span>
                  <span class="zone-machines">{{ zone.totalMachines }}台</span>
                  <span class="zone-rate" :style="{ color: getRateColor(zone.avgRate) }">
                    {{ zone.avgRate }}%
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 门店列表 -->
          <div class="shop-list">
            <div class="list-header">
              <div v-if="selectedZone" class="selected-zone-tag">
                <span>当前区域: {{ selectedZone }}</span>
                <a-button size="small" type="link" @click="clearZoneFilter">清除</a-button>
              </div>
              <a-select
                v-model:value="selectedConfigId"
                placeholder="选择网咖"
                allowClear
                style="width: 100%"
                @change="handleConfigChange"
              >
                <a-select-option :value="null">全部网咖</a-select-option>
                <a-select-option v-for="config in filteredConfigList" :key="config.id" :value="config.id">
                  {{ config.configName || '未分类' }}
                </a-select-option>
              </a-select>
            </div>
            <div class="list-content">
              <div
                v-for="shop in filteredShops"
                :key="shop.id"
                class="shop-item"
                :style="{ borderLeftColor: getConfigColor(shop.configId) }"
              >
                <div class="shop-header">
                  <span class="shop-name">{{ shop.name }}</span>
                  <span class="shop-tag" :style="{ background: getConfigColor(shop.configId) + '20', color: getConfigColor(shop.configId) }">
                    {{ getConfigName(shop.configId) }}
                  </span>
                </div>
                <div class="shop-info">
                  <span class="info-zone">{{ shop.zoneName || '-' }}</span>
                  <span class="info-status" :class="{ 'status-active': shop.businessStatus === 1 }">
                    {{ shop.businessStatus === 1 ? '营业中' : '暂无会员' }}
                  </span>
                </div>
                <div class="shop-stats">
                  <div class="stat-item">
                    <span class="stat-label">机器</span>
                    <span class="stat-num">{{ shop.totalMachines || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">空闲</span>
                    <span class="stat-num stat-free">{{ shop.freeMachines || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">占用</span>
                    <span class="stat-num stat-busy">{{ shop.busyMachines || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">上座率</span>
                    <span class="stat-num" :style="{ color: getRateColor(shop.occupancyRate) }">
                      {{ shop.occupancyRate || 0 }}%
                    </span>
                  </div>
                </div>
                <div v-if="!getShopCoords(shop)" class="no-coords">⚠️ 缺少经纬度</div>
              </div>
              <a-empty v-if="filteredShops.length === 0" description="暂无数据" />
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statusApi } from '@/api/status'
import { shopApi } from '@/api/shop'
import { configApi } from '@/api/config'

// 统计数据（使用计算属性，自动根据数据权限过滤）
const statistics = computed(() => {
  const list = permissionFilteredShops.value
  const totalMachines = list.reduce((sum, s) => sum + (s.totalMachines || 0), 0)
  const freeMachines = list.reduce((sum, s) => sum + (s.freeMachines || 0), 0)
  const busyMachines = list.reduce((sum, s) => sum + (s.busyMachines || 0), 0)
  return {
    totalShops: list.length,
    totalMachines,
    freeMachines,
    busyMachines
  }
})

// 地图相关
const mapChart = ref<HTMLElement | null>(null)
const mapLoading = ref(false)
const shops = ref<any[]>([])
const configList = ref<any[]>([])
const selectedConfigId = ref<number | null>(null)
const configCollectTime = ref<Record<number, string>>({})
const showHeatmap = ref(false)
const showCluster = ref(false)
const selectedZone = ref<string | null>(null) // 选中的区域

// 统计卡片展开状态
const expandedCards = reactive<Record<string, boolean>>({
  shops: false,
  machines: false,
  free: false,
  busy: false
})

let chartInstance: echarts.ECharts | null = null
let cachedShopsData: any[] | null = null // 缓存门店数据，避免重复计算
let debounceTimer: number | null = null // 防抖定时器，避免频繁渲染

/**
 * 性能优化措施：
 * 1. 数据缓存：cachedShopsData 缓存门店数据，避免每次重新计算
 * 2. 防抖处理：模式切换使用300ms防抖，避免频繁渲染
 * 3. 动态网格：聚合模式根据门店数量动态调整网格大小
 * 4. 内存优化：聚合模式只保留前10家门店信息用于tooltip
 * 5. 标签优化：门店数量>50时自动隐藏标签
 * 6. tooltip限制：tooltip不超出图表区域(confine: true)
 * 7. 图表配置：setOption第二个参数true，不合并配置提升性能
 */

// ==================== 数据权限控制 ====================

// 获取当前用户数据权限
const getUserDataPermission = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return { dataScope: 2, configIds: [] }
  const userInfo = JSON.parse(userInfoStr)
  return {
    dataScope: userInfo.dataScope || 2, // 1=仅授权配置, 2=全部数据
    configIds: userInfo.configIds || []
  }
}

// 根据数据权限过滤配置列表
const filteredConfigList = computed(() => {
  const { dataScope, configIds } = getUserDataPermission()
  if (dataScope === 2 || !configIds.length) {
    return configList.value // 全部数据权限
  }
  // 仅显示授权的配置
  return configList.value.filter(config => configIds.includes(config.id))
})

// 根据数据权限过滤门店列表
const permissionFilteredShops = computed(() => {
  const { dataScope, configIds } = getUserDataPermission()
  if (dataScope === 2 || !configIds.length) {
    return shops.value // 全部数据权限
  }
  // 仅显示授权的门店
  return shops.value.filter(shop => configIds.includes(shop.configId))
})

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 预定义颜色列表
const COLORS = [
  '#ff6b6b', // 红
  '#00d4ff', // 蓝
  '#52c41a', // 绿
  '#faad14', // 橙
  '#722ed1', // 紫
  '#eb2f96', // 粉
  '#13c2c2', // 青
  '#fa8c16', // 橘黄
  '#2f54eb', // 靛蓝
  '#a0d911', // 黄绿
  '#f5222d', // 深红
  '#1890ff', // 天蓝
  '#73d13d', // 浅绿
  '#ff7a45', // 珊瑚橙
  '#9254de', // 淡紫
  '#f759ab', // 玫粉
  '#36cfc9', // 青绿
  '#ffc53d', // 金黄
  '#597ef7', // 蓝紫
  '#95de64'  // 草绿
]

// 获取网咖配置对应的颜色
const getConfigColor = (configId: number | null) => {
  if (!configId) return '#999'
  const index = filteredConfigList.value.findIndex(c => c.id === configId)
  return COLORS[index % COLORS.length]
}

// 获取网咖配置名称
const getConfigName = (configId: number | null) => {
  if (!configId) return '未分类'
  const config = filteredConfigList.value.find(c => c.id === configId)
  return config?.configName || '未分类'
}

// 根据上座率获取颜色
const getRateColor = (rate: number) => {
  if (rate >= 80) return '#52c41a'
  if (rate >= 60) return '#1890ff'
  if (rate >= 40) return '#faad14'
  return '#ff4d4f'
}

// 获取某个网咖配置的门店数量
const getShopCountByConfig = (configId: number) => {
  return permissionFilteredShops.value.filter(s => s.configId === configId).length
}

// 过滤后的门店列表（按数据权限、网咖和区域筛选）
const filteredShops = computed(() => {
  // 先应用数据权限过滤
  let list = permissionFilteredShops.value

  // 按网咖筛选
  if (selectedConfigId.value !== null) {
    list = list.filter(s => s.configId === selectedConfigId.value)
  }

  // 按区域筛选
  if (selectedZone.value !== null) {
    list = list.filter(s => (s.zoneName || '未分类') === selectedZone.value)
  }

  return list
})

// 按网咖配置统计门店数（带数据权限过滤）
const configShopStats = computed(() => {
  return filteredConfigList.value.map((config, index) => ({
    configId: config.id,
    configName: config.configName || '未分类',
    color: COLORS[index % COLORS.length],
    count: permissionFilteredShops.value.filter(s => s.configId === config.id).length
  }))
})

// 按网咖配置统计机器数（带数据权限过滤）
const configMachineStats = computed(() => {
  return filteredConfigList.value.map((config, index) => ({
    configId: config.id,
    configName: config.configName || '未分类',
    color: COLORS[index % COLORS.length],
    count: permissionFilteredShops.value
      .filter(s => s.configId === config.id)
      .reduce((sum, s) => sum + (s.totalMachines || 0), 0)
  }))
})

// 按网咖配置统计空闲机器数（带数据权限过滤）
const configFreeStats = computed(() => {
  return filteredConfigList.value.map((config, index) => ({
    configId: config.id,
    configName: config.configName || '未分类',
    color: COLORS[index % COLORS.length],
    count: permissionFilteredShops.value
      .filter(s => s.configId === config.id)
      .reduce((sum, s) => sum + (s.freeMachines || 0), 0)
  }))
})

// 按网咖配置统计占用机器数（带数据权限过滤）
const configBusyStats = computed(() => {
  return filteredConfigList.value.map((config, index) => ({
    configId: config.id,
    configName: config.configName || '未分类',
    color: COLORS[index % COLORS.length],
    count: permissionFilteredShops.value
      .filter(s => s.configId === config.id)
      .reduce((sum, s) => sum + (s.busyMachines || 0), 0)
  }))
})

// 获取显示的统计项（根据展开状态）
const getVisibleStats = (stats: any[], cardKey: string) => {
  const isExpanded = expandedCards[cardKey]
  return isExpanded ? stats : stats.slice(0, 3)
}


// 判断是否全部展开
const allExpanded = computed(() => {
  return Object.values(expandedCards).every(v => v === true)
})

// 切换全部展开/收起
const toggleAllExpand = () => {
  const newState = !allExpanded.value
  Object.keys(expandedCards).forEach(key => {
    expandedCards[key] = newState
  })
}

// 按区域统计
const zoneStats = computed(() => {
  const zoneMap = new Map<string, any>()

  filteredShops.value.forEach(shop => {
    const zoneName = shop.zoneName || '未分类'
    if (!zoneMap.has(zoneName)) {
      zoneMap.set(zoneName, {
        name: zoneName,
        count: 0,
        totalMachines: 0,
        totalRate: 0,
        avgRate: 0
      })
    }
    const zone = zoneMap.get(zoneName)
    zone.count++
    zone.totalMachines += shop.totalMachines || 0
    zone.totalRate += shop.occupancyRate || 0
  })

  const result = Array.from(zoneMap.values())
  result.forEach(zone => {
    zone.avgRate = zone.count > 0 ? Math.round(zone.totalRate / zone.count) : 0
  })

  // 按门店数量排序
  return result.sort((a, b) => b.count - a.count)
})

// 按网咖配置筛选
const filterByConfig = (configId: number) => {
  selectedConfigId.value = selectedConfigId.value === configId ? null : configId
}

const handleConfigChange = () => {
  // 筛选变化时不需要额外操作
}

// 点击区域统计项
const handleZoneClick = (zoneName: string) => {
  if (selectedZone.value === zoneName) {
    selectedZone.value = null
  } else {
    selectedZone.value = zoneName
  }
}

// 清除区域筛选
const clearZoneFilter = () => {
  selectedZone.value = null
}

// 实时统计数据（用于地图tooltip等）
const realtimeStats = reactive({
  totalShops: 0,
  totalMachines: 0,
  freeMachines: 0,
  busyMachines: 0
})

// 加载统计数据（复用 fetchMapData 的数据）
const updateStatistics = (data: any[]) => {
  let totalMachines = 0
  let freeMachines = 0
  let busyMachines = 0

  // 收集每个 config 的最新采集时间
  const collectTimeMap: Record<number, string> = {}
  data.forEach((item: any) => {
    totalMachines += item.totalMachines || 0
    freeMachines += item.freeMachines || 0
    busyMachines += item.busyMachines || 0

    // 记录每个 configId 的采集时间（取最新的）
    if (item.configId && item.snapshotTime) {
      const existTime = collectTimeMap[item.configId]
      if (!existTime || new Date(item.snapshotTime) > new Date(existTime)) {
        collectTimeMap[item.configId] = item.snapshotTime
      }
    }
  })

  configCollectTime.value = collectTimeMap

  realtimeStats.totalShops = data.length
  realtimeStats.totalMachines = totalMachines
  realtimeStats.freeMachines = freeMachines
  realtimeStats.busyMachines = busyMachines
}

// 加载网咖配置列表
const fetchConfigList = async () => {
  try {
    const res = await configApi.list()
    configList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取门店经纬度（优先使用腾讯经纬度）
const getShopCoords = (shop: any) => {
  const lng = shop.longitudeTencent || shop.longitude
  const lat = shop.latitudeTencent || shop.latitude
  return lng && lat ? { lng, lat } : null
}

// 加载地图数据（同时更新统计数据）
const fetchMapData = async () => {
  mapLoading.value = true
  cachedShopsData = null // 清除缓存
  try {
    // 获取所有门店
    const shopRes = await shopApi.page({ pageNum: 1, pageSize: 1000 })
    const allShops = shopRes.data?.records || []

    // 获取所有门店的实时状态（统一接口）
    const realtimeRes = await statusApi.listRealtime()
    const realtimeData = realtimeRes.data || []

    // 更新统计数据
    updateStatistics(realtimeData)

    // 创建门店 ID -> 实时状态的映射
    const realtimeMap = new Map<number, any>()
    realtimeData.forEach((item: any) => {
      realtimeMap.set(item.shopId, item)
    })

    // 组装门店数据（不过滤，保留所有门店用于统计）
    shops.value = allShops.map((shop: any) => {
      const realtime = realtimeMap.get(shop.id)
      return {
        ...shop,
        businessStatus: realtime ? 1 : 2,
        totalMachines: realtime?.totalMachines || 0,
        freeMachines: realtime?.freeMachines || 0,
        busyMachines: realtime?.busyMachines || 0,
        occupancyRate: realtime?.occupancyRate || 0,
        snapshotTime: realtime?.snapshotTime
      }
    })
  } catch (error) {
    console.error(error)
  } finally {
    mapLoading.value = false
  }

  // 在 loading 结束后初始化图表
  await nextTick()
  initChart()
}

// 刷新地图数据

// 模式切换（带防抖）
const handleModeChange = () => {
  // 清除之前的定时器
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  // 清除缓存，因为显示模式变了
  cachedShopsData = null

  // 300ms防抖
  debounceTimer = window.setTimeout(() => {
    initChart()
  }, 300)
}

// 初始化地图图表
const initChart = async () => {
  if (!mapLoaded.value) {
    console.warn('地图 JSON 未加载完成')
    return
  }

  // 等待 DOM 渲染完成
  await nextTick()

  if (!mapChart.value) {
    console.warn('地图容器不存在，稍后重试')
    setTimeout(() => initChart(), 100)
    return
  }

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(mapChart.value)

  // 使用缓存数据或重新计算
  if (!cachedShopsData || cachedShopsData.length !== filteredShops.value.length) {
    cachedShopsData = filteredShops.value
      .filter(s => getShopCoords(s))
      .map(shop => {
        const coords = getShopCoords(shop) as { lng: number, lat: number }
        return {
          name: shop.name,
          value: [coords.lng, coords.lat, shop.occupancyRate || 0],
          ...shop
        }
      })
  }

  const allShopsData = cachedShopsData
  const series: any[] = []

  // 热力图模式 - 使用散点图模拟热力效果
  if (showHeatmap.value) {
    series.push({
      name: '门店密集度',
      type: 'scatter',
      coordinateSystem: 'geo',
      data: allShopsData.map(d => ({
        ...d,
        value: [d.value[0], d.value[1], d.occupancyRate || 50]
      })),
      symbolSize: 25,
      itemStyle: {
        color: (params: any) => {
          const rate = params.data.occupancyRate || 0
          if (rate >= 80) return 'rgba(255, 77, 79, 0.7)'
          if (rate >= 60) return 'rgba(250, 173, 20, 0.7)'
          if (rate >= 40) return 'rgba(24, 144, 255, 0.7)'
          return 'rgba(82, 196, 26, 0.7)'
        },
        shadowBlur: 30,
        shadowColor: 'rgba(0, 150, 255, 0.5)'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 40,
          shadowColor: 'rgba(0, 200, 255, 0.8)'
        }
      }
    })
  }
  // 聚合模式 - 优化聚合算法
  else if (showCluster.value) {
    // 动态调整网格大小，门店越多网格越大
    const gridSize = allShopsData.length > 100 ? 0.03 : allShopsData.length > 50 ? 0.02 : 0.015
    const clusters = new Map<string, any>()

    allShopsData.forEach(shop => {
      const gridKey = `${Math.floor(shop.value[0] / gridSize)}_${Math.floor(shop.value[1] / gridSize)}`

      if (!clusters.has(gridKey)) {
        clusters.set(gridKey, {
          lng: shop.value[0],
          lat: shop.value[1],
          count: 0,
          shops: [],
          totalRate: 0
        })
      }

      const cluster = clusters.get(gridKey)
      cluster.count++
      // 只保留前10家门店信息用于tooltip，减少内存占用
      if (cluster.shops.length < 10) {
        cluster.shops.push(shop)
      }
      cluster.totalRate += shop.occupancyRate || 0
    })

    const clusterData = Array.from(clusters.values()).map(c => ({
      name: `${c.count}家门店`,
      value: [c.lng, c.lat, c.count, Math.round(c.totalRate / c.count)],
      count: c.count,
      avgRate: Math.round(c.totalRate / c.count),
      shops: c.shops
    }))

    series.push({
      name: '门店聚合',
      type: 'scatter',
      coordinateSystem: 'geo',
      data: clusterData,
      symbolSize: (val: number[]) => {
        return 10 + Math.min(val[2], 20) * 2
      },
      itemStyle: {
        color: (params: any) => {
          const count = params.data.count
          if (count >= 10) return '#ff4d4f'
          if (count >= 5) return '#faad14'
          return '#1890ff'
        },
        shadowBlur: 20,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      },
      label: {
        show: true,
        formatter: (params: any) => params.data.count,
        color: '#fff',
        fontSize: 12,
        fontWeight: 'bold'
      }
    })
  }
  // 默认散点模式 - 按配置分组
  else {
    filteredConfigList.value.forEach((config, index) => {
      const color = COLORS[index % COLORS.length]
      const configShops = allShopsData.filter((s: any) => s.configId === config.id)

      // 营业中 - 限制标签显示
      series.push({
        name: config.configName || '未分类',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: configShops.filter((d: any) => d.businessStatus === 1),
        symbolSize: (val: number[]) => {
          const baseSize = 10
          const rate = val[2] || 0
          return baseSize + rate * 0.1
        },
        rippleEffect: { brushType: 'stroke', scale: 4, period: 4 },
        itemStyle: {
          color: color,
          shadowBlur: 10,
          shadowColor: color + '80'
        },
        label: {
          // 只有门店数量少于等于10个且总数据量少于50个时才显示标签
          show: configShops.length <= 10 && allShopsData.length <= 50,
          position: 'right',
          formatter: '{b}',
          color: color,
          fontSize: 10,
          textShadowColor: '#000',
          textShadowBlur: 2
        }
      })

      // 休息中
      series.push({
        name: (config.configName || '未分类') + '(休息)',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: configShops.filter((d: any) => d.businessStatus !== 1),
        symbolSize: 8,
        itemStyle: { color: color + '80' }
      })
    })

    // 未分类的门店
    const unclassifiedShops = allShopsData.filter((s: any) => !s.configId)

    if (unclassifiedShops.length > 0) {
      series.push({
        name: '未分类',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: unclassifiedShops,
        symbolSize: 8,
        itemStyle: { color: '#999' }
      })
    }
  }

  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10, 20, 40, 0.95)',
      borderColor: '#00d4ff',
      borderWidth: 1,
      textStyle: { color: '#fff' },
      confine: true, // tooltip不超出图表区域
      formatter: (params: any) => {
        // 聚合模式的tooltip
        if (showCluster.value && params.data.shops) {
          const shops = params.data.shops.slice(0, 5)
          let html = `
            <div style="padding: 12px; min-width: 250px;">
              <div style="font-weight: bold; margin-bottom: 10px; font-size: 15px; color: #00d4ff;">
                聚合区域 (${params.data.count}家门店)
              </div>
              <div style="margin-bottom: 8px;">
                <span style="color: #888;">平均上座率：</span>
                <span style="color: #00ff88; font-weight: bold; font-size: 16px;">${params.data.avgRate}%</span>
              </div>
              <div style="border-top: 1px solid #1a3a5c; padding-top: 8px; margin-top: 8px;">
          `

          shops.forEach((shop: any) => {
            const configColor = getConfigColor(shop.configId)
            html += `
              <div style="margin-bottom: 6px; display: flex; justify-content: space-between; align-items: center;">
                <span style="color: ${configColor}; font-size: 12px;">${shop.name}</span>
                <span style="color: #00ff88; font-size: 12px;">${shop.occupancyRate}%</span>
              </div>
            `
          })

          if (params.data.count > 5) {
            html += `<div style="color: #888; font-size: 11px; margin-top: 4px;">...还有${params.data.count - 5}家</div>`
          }

          html += `</div></div>`
          return html
        }

        // 单个门店的tooltip
        const shop = params.data
        const configColor = getConfigColor(shop.configId)
        const statusColor = shop.businessStatus === 1 ? '#00ff88' : '#faad14'
        const rateColor = getRateColor(shop.occupancyRate || 0)

        return `
          <div style="padding: 12px; min-width: 220px;">
            <div style="font-weight: bold; margin-bottom: 10px; font-size: 15px; color: ${configColor};">
              ${shop.name}
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
              <span style="color: #888;">网咖</span>
              <span style="color: ${configColor}; font-weight: bold;">${getConfigName(shop.configId)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
              <span style="color: #888;">区域</span>
              <span style="color: #fff;">${shop.zoneName || '-'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
              <span style="color: #888;">状态</span>
              <span style="color: ${statusColor};">${shop.businessStatus === 1 ? '营业中' : '暂无会员'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
              <span style="color: #888;">机器</span>
              <span style="color: #fff;">
                <span style="color: #52c41a;">${shop.freeMachines || 0}</span> /
                <span style="color: #ff4d4f;">${shop.busyMachines || 0}</span> /
                <span>${shop.totalMachines || 0}台</span>
              </span>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: #888;">上座率</span>
              <span style="color: ${rateColor}; font-weight: bold; font-size: 16px;">${shop.occupancyRate || 0}%</span>
            </div>
            ${shop.snapshotTime ? `
              <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #1a3a5c; color: #666; font-size: 11px;">
                更新时间: ${formatTime(shop.snapshotTime)}
              </div>
            ` : ''}
          </div>
        `
      }
    },
    legend: {
      show: false
    },
    geo: {
      map: 'chengdu',
      roam: true,
      zoom: 11,
      center: [104.06, 30.67],
      scaleLimit: { min: 8, max: 18 },
      selectedMode: 'single',
      itemStyle: {
        areaColor: '#0a1628',
        borderColor: '#1a3a5c',
        borderWidth: 1,
        shadowColor: '#00d4ff',
        shadowBlur: 10
      },
      emphasis: {
        itemStyle: {
          areaColor: '#0d2137',
          borderColor: '#00d4ff',
          borderWidth: 2
        },
        label: { show: true, color: '#00d4ff', fontSize: 12 }
      },
      select: {
        itemStyle: {
          areaColor: '#1a5a8c',
          borderColor: '#00ff88',
          borderWidth: 2
        },
        label: { show: true, color: '#00ff88', fontSize: 14, fontWeight: 'bold' }
      },
      label: { show: true, color: '#3a6a8c', fontSize: 10 }
    },
    series: series
  }

  chartInstance.setOption(option, true) // true表示不合并配置，提升性能

  // 绑定地图点击事件
  chartInstance.off('click') // 先移除旧事件
  chartInstance.on('click', (params: any) => {
    if (params.componentType === 'geo') {
      // 点击的是地图区域
      const zoneName = params.name
      if (selectedZone.value === zoneName) {
        // 再次点击同一区域，取消选中
        selectedZone.value = null
        chartInstance?.dispatchAction({
          type: 'unselect',
          seriesIndex: 0,
          name: zoneName
        })
      } else {
        // 选中新区域
        selectedZone.value = zoneName
        chartInstance?.dispatchAction({
          type: 'select',
          seriesIndex: 0,
          name: zoneName
        })
      }
    } else if (params.componentType === 'series') {
      // 点击的是门店散点，清除区域筛选
      if (selectedZone.value !== null) {
        selectedZone.value = null
      }
    }
  })
}

// 窗口大小变化
const handleResize = () => {
  chartInstance?.resize()
}

// 地图是否加载完成
const mapLoaded = ref(false)

onMounted(async () => {
  // 加载成都地图（从本地加载）
  try {
    const response = await fetch('/chengdu.json')
    const chengduJson = await response.json()
    echarts.registerMap('chengdu', chengduJson)
    mapLoaded.value = true
  } catch (e) {
    console.error('加载成都地图失败', e)
  }

  // 等待 DOM 渲染完成
  await nextTick()

  // 先加载配置列表
  await fetchConfigList()

  // 加载地图数据（同时更新统计数据）
  fetchMapData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
})
</script>

<style scoped lang="less">
.dashboard {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.stat-cards-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.collect-time {
  .collect-time-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .label {
      color: #333;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .time-tags {
    display: flex;
    flex-wrap: wrap;
  }
}

.stat-card {
  position: relative;
  overflow: hidden;

  :deep(.ant-card-body) {
    display: flex;
    align-items: flex-start;
    padding: 20px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    flex-shrink: 0;

    span {
      font-size: 28px;
    }
  }

  .stat-content {
    flex: 1;
    min-width: 0;
  }
}

.stat-total {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.stat-detail {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .detail-item {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #666;
    margin-bottom: 4px;

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      margin-right: 8px;
    }
  }

  .expand-btn {
    margin-top: 8px;
    padding: 4px 0;
    font-size: 12px;
    color: #1890ff;
    cursor: pointer;
    text-align: center;
    transition: all 0.3s;

    &:hover {
      color: #40a9ff;
    }
  }
}

.map-card {
  margin-top: 16px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-right: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
  }

  &.active {
    background: #e6f7ff;
    color: #1890ff;
  }
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.zone-stats {
  margin-bottom: 16px;

  .zone-stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;

    .title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }
  }

  .zone-stats-content {
    max-height: 150px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 2px;
    }
  }

  .zone-item {
    padding: 8px;
    margin-bottom: 6px;
    background: #fafafa;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f0f7ff;
    }

    &.zone-selected {
      background: #e6f7ff;
      border-left: 3px solid #1890ff;
    }
  }

  .zone-name {
    font-size: 13px;
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
  }

  .zone-data {
    display: flex;
    justify-content: space-between;
    font-size: 12px;

    .zone-count {
      color: #666;
    }

    .zone-machines {
      color: #1890ff;
    }

    .zone-rate {
      font-weight: 600;
    }
  }
}

.shop-list {
  display: flex;
  flex-direction: column;
}

.list-header {
  margin-bottom: 12px;

  .selected-zone-tag {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 10px;
    background: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 6px;
    margin-bottom: 8px;
    font-size: 13px;
    color: #1890ff;
  }
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.shop-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
  transition: all 0.3s;

  &:hover {
    background: #f0f7ff;
    transform: translateX(2px);
  }
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.shop-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.shop-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.shop-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;

  .info-zone {
    color: #666;
  }

  .info-status {
    padding: 2px 8px;
    border-radius: 10px;
    background: #f0f0f0;
    color: #999;
    font-size: 11px;

    &.status-active {
      background: #f6ffed;
      color: #52c41a;
    }
  }
}

.shop-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 8px;

  .stat-item {
    text-align: center;

    .stat-label {
      display: block;
      font-size: 11px;
      color: #999;
      margin-bottom: 2px;
    }

    .stat-num {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .stat-free {
      color: #52c41a;
    }

    .stat-busy {
      color: #ff4d4f;
    }
  }
}

.no-coords {
  color: #faad14;
  font-size: 11px;
  padding: 4px 8px;
  background: #fffbe6;
  border-radius: 4px;
  display: inline-block;
}
</style>
