<template>
  <div class="competitor-dashboard">
    <!-- 顶部标题 -->
    <a-card size="small" style="margin-bottom: 16px">
      <div class="dashboard-header">
        <h2>竞品监控大屏</h2>
        <div class="header-right">
          <a-select
            v-model:value="selectedConfigId"
            placeholder="选择我方品牌"
            style="width: 200px; margin-right: 12px"
            @change="handleConfigChange"
          >
            <a-select-option v-for="config in configList" :key="config.id" :value="config.id">
              {{ config.configName }}
            </a-select-option>
          </a-select>
          <a-button type="primary" size="small" @click="refreshData" :loading="loading">
            <reload-outlined /> 刷新数据
          </a-button>
          <span class="time">{{ currentTime }}</span>
        </div>
      </div>
    </a-card>

    <!-- 核心指标卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic :title="selectedBrandName + '上座率'" :value="myBrandStats.occupancyRate" suffix="%" :value-style="{ color: '#ff6b6b' }">
            <template #footer>
              <span style="font-size: 12px; color: #666">{{ myBrandStats.totalMachines }}台 / {{ myBrandStats.storeCount }}家店</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="竞争品牌数" :value="competitorBrands.length">
            <template #footer>
              <span style="font-size: 12px; color: #666">平均上座率 {{ avgCompetitorRate }}%</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="行业总机器" :value="industryStats.totalMachines">
            <template #footer>
              <span style="font-size: 12px; color: #666">使用中 {{ industryStats.busyMachines }}台</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="竞争力排名" :value="myRank" suffix="名" :value-style="{ color: myRank <= 2 ? '#52c41a' : '#faad14' }">
            <template #footer>
              <span style="font-size: 12px; color: #666">共 {{ allBrands.length }} 个品牌</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16">
      <!-- 品牌排名 -->
      <a-col :span="16">
        <a-card title="品牌实时排名">
          <div ref="rankChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
      <!-- 市场占有率 -->
      <a-col :span="8">
        <a-card title="市场占有率">
          <div ref="pieChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <!-- 品牌竞争力对比 -->
      <a-col :span="8">
        <a-card title="品牌竞争力对比">
          <div ref="radarChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
      <!-- 各品牌门店分布 -->
      <a-col :span="16">
        <a-card title="各品牌门店分布">
          <div ref="barChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 我方门店详情 -->
    <a-card :title="selectedBrandName + '门店详情'" style="margin-top: 16px">
      <a-table
        :columns="storeColumns"
        :data-source="myStores"
        :pagination="{ pageSize: 5 }"
        size="small"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'occupancyRate'">
            <a-progress
              :percent="record.occupancyRate || 0"
              :stroke-color="record.occupancyRate >= 80 ? '#ff4d4f' : record.occupancyRate >= 50 ? '#faad14' : '#52c41a'"
              size="small"
            />
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { statusApi } from '@/api/status'
import { configApi } from '@/api/config'

const currentTime = ref('')
const loading = ref(false)
const configList = ref<any[]>([])
const selectedConfigId = ref<number | null>(null)
const realtimeData = ref<any[]>([])

// 图表DOM引用
const rankChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
const radarChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)

// 图表实例
let timer: any = null
let rankChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

// 预定义颜色
const COLORS = ['#ff6b6b', '#1890ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96', '#13c2c2', '#fa8c16', '#2f54eb', '#a0d911']

// 门店表格列定义
const storeColumns = [
  { title: '门店名称', dataIndex: 'name', key: 'name' },
  { title: '总机器', dataIndex: 'totalMachines', key: 'totalMachines', width: 80 },
  { title: '使用中', dataIndex: 'busyMachines', key: 'busyMachines', width: 80 },
  { title: '上座率', dataIndex: 'occupancyRate', key: 'occupancyRate', width: 150 }
]

// 当前选中的品牌名称
const selectedBrandName = computed(() => {
  if (!selectedConfigId.value) return '请选择品牌'
  const config = configList.value.find(c => c.id === selectedConfigId.value)
  return config?.configName || ''
})

// 按 config 分组的数据（包含所有启用的配置）
const brandMap = computed(() => {
  const map = new Map<number, any>()
  
  // 先添加所有启用的配置
  configList.value.forEach(config => {
    map.set(config.id, {
      id: config.id,
      name: config.configName,
      stores: [],
      totalMachines: 0,
      busyMachines: 0,
      occupancyRate: 0,
      storeCount: 0
    })
  })
  
  // 再根据实时数据更新
  realtimeData.value.forEach(item => {
    const configId = item.configId
    if (configId && map.has(configId)) {
      const brand = map.get(configId)
      brand.stores.push({
        ...item,
        name: item.shop?.name || '-'
      })
      brand.totalMachines += item.totalMachines || 0
      brand.busyMachines += item.busyMachines || 0
      brand.storeCount += 1
    }
  })
  
  // 计算上座率
  map.forEach(brand => {
    brand.occupancyRate = brand.totalMachines > 0
      ? Number(((brand.busyMachines / brand.totalMachines) * 100).toFixed(1))
      : 0
  })
  
  return map
})

// 所有品牌列表
const allBrands = computed(() => Array.from(brandMap.value.values()))

// 我方品牌数据
const myBrandStats = computed(() => {
  if (!selectedConfigId.value) return { totalMachines: 0, busyMachines: 0, occupancyRate: 0, storeCount: 0 }
  const myBrand = brandMap.value.get(selectedConfigId.value)
  return myBrand ? {
    totalMachines: myBrand.totalMachines,
    busyMachines: myBrand.busyMachines,
    occupancyRate: myBrand.occupancyRate,
    storeCount: myBrand.storeCount
  } : { totalMachines: 0, busyMachines: 0, occupancyRate: 0, storeCount: 0 }
})

// 我方所有门店
const myStores = computed(() => {
  if (!selectedConfigId.value) return []
  const myBrand = brandMap.value.get(selectedConfigId.value)
  return myBrand ? myBrand.stores : []
})

// 竞争对手品牌
const competitorBrands = computed(() => {
  if (!selectedConfigId.value) return []
  return allBrands.value.filter(b => b.id !== selectedConfigId.value)
})

// 竞争对手平均上座率
const avgCompetitorRate = computed(() => {
  if (competitorBrands.value.length === 0) return '0.0'
  const total = competitorBrands.value.reduce((sum, b) => sum + b.occupancyRate, 0)
  return (total / competitorBrands.value.length).toFixed(1)
})

// 行业整体数据
const industryStats = computed(() => {
  const totalMachines = allBrands.value.reduce((sum, b) => sum + b.totalMachines, 0)
  const busyMachines = allBrands.value.reduce((sum, b) => sum + b.busyMachines, 0)
  return { totalMachines, busyMachines }
})

// 我方排名
const myRank = computed(() => {
  if (!selectedConfigId.value) return 0
  const sorted = [...allBrands.value].sort((a, b) => b.occupancyRate - a.occupancyRate)
  const index = sorted.findIndex(b => b.id === selectedConfigId.value)
  return index + 1
})

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 加载网咖配置列表
const loadConfigList = async () => {
  try {
    const res = await configApi.listEnabled()
    configList.value = res.data || []
    // 默认选中第一个
    if (configList.value.length > 0 && !selectedConfigId.value) {
      selectedConfigId.value = configList.value[0].id
    }
  } catch (error) {
    console.error('加载配置列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 获取所有门店实时状态
    const res = await statusApi.listRealtime()
    realtimeData.value = res.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 配置切换
const handleConfigChange = () => {
  initAllCharts()
}

// 刷新数据
const refreshData = async () => {
  await loadData()
  setTimeout(() => {
    initAllCharts()
  }, 100)
}

// 初始化品牌排名图
const initRankChart = () => {
  if (!rankChartRef.value) return
  
  if (rankChart) rankChart.dispose()
  rankChart = echarts.init(rankChartRef.value)

  const sortedBrands = [...allBrands.value].sort((a, b) => b.occupancyRate - a.occupancyRate)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '15%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    yAxis: {
      type: 'category',
      data: sortedBrands.map(b => b.name).reverse(),
      axisLabel: {
        fontWeight: 'bold',
        color: (value?: string | number) => {
          const brand = sortedBrands.find(b => b.name === value)
          return brand?.id === selectedConfigId.value ? '#ff6b6b' : '#333'
        }
      }
    },
    series: [{
      name: '上座率',
      type: 'bar',
      data: sortedBrands.map(brand => ({
        value: brand.occupancyRate,
        itemStyle: {
          color: brand.id === selectedConfigId.value ? '#ff6b6b' : '#1890ff',
          borderRadius: [0, 4, 4, 0]
        }
      })).reverse(),
      barWidth: '50%',
      label: {
        show: true,
        position: 'right',
        formatter: '{c}%'
      }
    }]
  }

  rankChart.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  // 按机器数排序，只显示前10名
  const sortedBrands = [...allBrands.value].sort((a, b) => b.totalMachines - a.totalMachines)
  const topBrands = sortedBrands.slice(0, 10)
  const otherBrands = sortedBrands.slice(10)
  
  // 其他品牌合并
  const pieData = topBrands.map((brand, index) => ({
    value: brand.totalMachines,
    name: brand.name,
    itemStyle: {
      color: brand.id === selectedConfigId.value ? COLORS[0] : COLORS[(index % (COLORS.length - 1)) + 1]
    }
  }))
  
  if (otherBrands.length > 0) {
    const otherTotal = otherBrands.reduce((sum, b) => sum + b.totalMachines, 0)
    pieData.push({
      value: otherTotal,
      name: '其他',
      itemStyle: { color: '#999' }
    })
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>机器数: {c}台 ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: '2%',
      top: 'middle',
      height: '80%',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 6,
      textStyle: {
        fontSize: 11
      },
      formatter: (name: string) => {
        if (name === '其他') return '其他'
        const brand = allBrands.value.find(b => b.name === name)
        if (!brand) return name
        // 名称过长时截断
        const displayName = brand.name.length > 5 ? brand.name.substring(0, 5) + '...' : brand.name
        return `${displayName} ${brand.storeCount}家`
      }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 12, fontWeight: 'bold' }
      },
      data: pieData
    }]
  }

  pieChart.setOption(option)
}

// 初始化雷达图
const initRadarChart = () => {
  if (!radarChartRef.value) return
  
  if (radarChart) radarChart.dispose()
  radarChart = echarts.init(radarChartRef.value)

  const myBrand = selectedConfigId.value ? brandMap.value.get(selectedConfigId.value) : null
  const avgCompetitor = {
    occupancyRate: parseFloat(avgCompetitorRate.value as string) || 0,
    totalMachines: competitorBrands.value.length > 0
      ? competitorBrands.value.reduce((sum, b) => sum + b.totalMachines, 0) / competitorBrands.value.length
      : 0,
    storeCount: competitorBrands.value.length > 0
      ? competitorBrands.value.reduce((sum, b) => sum + b.storeCount, 0) / competitorBrands.value.length
      : 0
  }

  const maxMachines = Math.max(myBrand?.totalMachines || 0, avgCompetitor.totalMachines, 200)
  const maxStores = Math.max(myBrand?.storeCount || 0, avgCompetitor.storeCount, 10)

  const option: echarts.EChartsOption = {
    tooltip: {},
    legend: {
      data: [selectedBrandName.value, '竞争对手平均'],
      bottom: '3%'
    },
    radar: {
      indicator: [
        { name: '上座率', max: 100 },
        { name: '机器数量', max: maxMachines },
        { name: '门店数量', max: maxStores },
        { name: '运营效率', max: 100 }
      ],
      radius: '60%'
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: [myBrand?.occupancyRate || 0, myBrand?.totalMachines || 0, myBrand?.storeCount || 0, (myBrand?.occupancyRate || 0) * 0.9],
          name: selectedBrandName.value,
          lineStyle: { color: '#ff6b6b', width: 2 },
          areaStyle: { color: 'rgba(255, 107, 107, 0.3)' },
          itemStyle: { color: '#ff6b6b' }
        },
        {
          value: [avgCompetitor.occupancyRate, avgCompetitor.totalMachines, avgCompetitor.storeCount, avgCompetitor.occupancyRate * 0.85],
          name: '竞争对手平均',
          lineStyle: { color: '#1890ff', width: 2 },
          areaStyle: { color: 'rgba(24, 144, 255, 0.3)' },
          itemStyle: { color: '#1890ff' }
        }
      ]
    }]
  }

  radarChart.setOption(option)
}

// 初始化柱状图
const initBarChart = () => {
  if (!barChartRef.value) return
  
  if (barChart) barChart.dispose()
  barChart = echarts.init(barChartRef.value)

  const sortedBrands = [...allBrands.value].sort((a, b) => b.storeCount - a.storeCount)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['门店数', '机器数'],
      top: 5
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedBrands.map(b => b.name),
      axisLabel: { rotate: 15 }
    },
    yAxis: [
      {
        type: 'value',
        name: '门店数'
      },
      {
        type: 'value',
        name: '机器数',
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '门店数',
        type: 'bar',
        data: sortedBrands.map((brand, index) => ({
          value: brand.storeCount,
          itemStyle: {
            color: brand.id === selectedConfigId.value ? COLORS[0] : COLORS[(index % (COLORS.length - 1)) + 1],
            borderRadius: [4, 4, 0, 0]
          }
        }))
      },
      {
        name: '机器数',
        type: 'bar',
        yAxisIndex: 1,
        data: sortedBrands.map((brand, index) => ({
          value: brand.totalMachines,
          itemStyle: {
            color: brand.id === selectedConfigId.value ? 'rgba(255,107,107,0.6)' : `rgba(${24 + index * 20}, ${144 - index * 10}, ${255 - index * 15}, 0.6)`,
            borderRadius: [4, 4, 0, 0]
          }
        }))
      }
    ]
  }

  barChart.setOption(option)
}

// 初始化所有图表
const initAllCharts = () => {
  initRankChart()
  initPieChart()
  initRadarChart()
  initBarChart()
}

// 窗口大小变化
const handleResize = () => {
  rankChart?.resize()
  pieChart?.resize()
  radarChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)

  await loadConfigList()
  await loadData()

  await nextTick()
  setTimeout(initAllCharts, 100)

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  clearInterval(timer)
  rankChart?.dispose()
  pieChart?.dispose()
  radarChart?.dispose()
  barChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
.competitor-dashboard {
  background: #f0f2f5;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h2 {
    margin: 0;
    font-size: 18px;
    color: #333;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .time {
      color: #666;
      font-size: 14px;
    }
  }
}
</style>
