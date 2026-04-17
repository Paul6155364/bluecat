<template>
  <div class="heatmap-page">
    <!-- 筛选条件 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-row :gutter="16" align="middle">
        <a-col :span="6">
          <a-select
            v-model:value="selectedShopId"
            placeholder="选择门店 (默认全部)"
            allowClear
            style="width: 100%"
            :options="shopOptions"
            :loading="shopLoading"
          />
        </a-col>
        <a-col :span="6">
          <a-range-picker
            v-model:value="dateRange"
            placeholder="选择日期范围"
            style="width: 100%"
          />
        </a-col>
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="loadHeatmapData" :loading="loading">
              <search-outlined /> 查询
            </a-button>
            <a-tag v-if="dataLoaded" color="blue">数据范围: {{ heatmapData.startDate }} ~ {{ heatmapData.endDate }}</a-tag>
            <a-tag v-if="dataLoaded" color="green">记录数: {{ heatmapData.totalRecords }}</a-tag>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 无数据提示 -->
    <a-empty v-if="!dataLoaded" description="请选择门店和日期范围后点击查询按钮加载数据" />

    <!-- 数据内容 -->
    <template v-else>
    <!-- 小时热力图 -->
    <a-row :gutter="16">
      <a-col :span="16">
        <a-card title="24小时上座率热力图">
          <a-spin :spinning="loading">
            <div ref="hourlyChart" style="height: 400px"></div>
          </a-spin>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="高峰时段排名">
          <a-spin :spinning="loading">
            <div ref="peakRankChart" style="height: 400px"></div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <!-- 日历热力图 -->
    <a-card title="日历视图 (类似GitHub贡献图)" style="margin-top: 16px">
      <div class="calendar-heatmap">
        <div class="calendar-container">
          <div class="month-labels">
            <span v-for="month in monthLabels" :key="month">{{ month }}</span>
          </div>
          <div class="calendar-grid">
            <div class="weekday-labels">
              <span>一</span>
              <span>三</span>
              <span>五</span>
              <span>日</span>
            </div>
            <div class="calendar-body" ref="calendarBody">
              <!-- 日历格子将通过JS渲染 -->
            </div>
          </div>
        </div>
      </div>
      <div class="calendar-legend">
        <span>低</span>
        <div class="legend-blocks">
          <span v-for="level in 6" :key="level" class="legend-block" :class="`level-${level - 1}`"></span>
        </div>
        <span>高</span>
      </div>
    </a-card>

    <!-- 区域热力图 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="区域上座率热力图">
          <a-spin :spinning="loading">
            <div ref="areaHeatmap" style="height: 350px"></div>
          </a-spin>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="区域上座率排名">
          <a-spin :spinning="loading">
            <a-list :data-source="topAreas" :split="false">
              <template #renderItem="{ item, index }">
                <a-list-item>
                  <div class="area-item">
                    <div class="area-rank" :class="`rank-${index + 1}`">
                      {{ index + 1 }}
                    </div>
                    <div class="area-info">
                      <div class="area-name">{{ item.areaName }}</div>
                      <a-progress 
                        :percent="item.rate" 
                        :strokeColor="getHeatColor(item.rate)"
                        :showInfo="false"
                        size="small"
                      />
                    </div>
                    <div class="area-rate" :style="{ color: getHeatColor(item.rate) }">
                      {{ item.rate }}%
                    </div>
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <!-- 日期+小时热力图 -->
    <a-card title="日期-小时 热力图" style="margin-top: 16px">
      <a-spin :spinning="loading">
        <div ref="dayHourHeatmap" style="height: 500px"></div>
      </a-spin>
    </a-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { analysisApi } from '@/api/analysis'

// 门店选择
const shopOptions = ref<{ value: number; label: string }[]>([])
const selectedShopId = ref<number | undefined>()
const shopLoading = ref(false)
const loading = ref(false)

// 数据是否已加载
const dataLoaded = ref(false)

// 日期范围
const dateRange = ref<[dayjs.Dayjs, dayjs.Dayjs]>()

// 热力图数据
const heatmapData = reactive({
  hourlyRate: [] as any[],
  heatmapData: [] as any[],
  dateList: [] as string[],
  areaRate: [] as any[],
  calendarData: [] as any[],
  totalRecords: 0,
  startDate: '',
  endDate: ''
})

// 图表引用
const hourlyChart = ref<HTMLElement | null>(null)
const peakRankChart = ref<HTMLElement | null>(null)
const areaHeatmap = ref<HTMLElement | null>(null)
const dayHourHeatmap = ref<HTMLElement | null>(null)
const calendarBody = ref<HTMLElement | null>(null)

let chartInstances: echarts.ECharts[] = []

// 月份标签
const monthLabels = computed(() => {
  const months = []
  for (let i = 0; i < 12; i++) {
    months.push(`${i + 1}月`)
  }
  return months
})

// 区域排名 (Top 10)
const topAreas = computed(() => {
  return heatmapData.areaRate.slice(0, 10)
})

// 加载门店列表
const loadShops = async () => {
  shopLoading.value = true
  try {
    const res = await analysisApi.listShops()
    shopOptions.value = (res.data || []).map((shop: any) => ({
      value: shop.id,
      label: shop.name
    }))
  } catch (error) {
    console.error(error)
  } finally {
    shopLoading.value = false
  }
}

// 加载热力图数据
const loadHeatmapData = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (selectedShopId.value) {
      params.shopId = selectedShopId.value
    }
    
    // 必须选择日期范围
    if (!dateRange.value || dateRange.value.length !== 2) {
      message.warning('请选择日期范围')
      return
    }
    
    params.startDate = dateRange.value[0].format('YYYY-MM-DD')
    params.endDate = dateRange.value[1].format('YYYY-MM-DD')

    const res = await analysisApi.heatmap(params.shopId, params.startDate, params.endDate)
    const data = res.data || {}
    
    heatmapData.hourlyRate = data.hourlyRate || []
    heatmapData.heatmapData = data.heatmapData || []
    heatmapData.dateList = data.dateList || []
    heatmapData.areaRate = data.areaRate || []
    heatmapData.calendarData = data.calendarData || []
    heatmapData.totalRecords = data.totalRecords || 0
    heatmapData.startDate = data.startDate || ''
    heatmapData.endDate = data.endDate || ''
    
    dataLoaded.value = true

    await nextTick()
    // 延迟渲染确保DOM完全加载
    setTimeout(() => {
      renderCharts()
      renderCalendar()
    }, 100)
  } catch (error) {
    console.error(error)
    message.error('加载热力图数据失败')
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderCharts = () => {
  // 销毁旧图表
  chartInstances.forEach(chart => chart?.dispose())
  chartInstances = []

  renderHourlyChart()
  renderPeakRankChart()
  renderAreaHeatmap()
  renderDayHourHeatmap()
}

// 24小时热力图
const renderHourlyChart = () => {
  if (!hourlyChart.value) {
    console.warn('hourlyChart ref not ready')
    return
  }
  
  // 确保容器有尺寸
  if (hourlyChart.value.offsetWidth === 0 || hourlyChart.value.offsetHeight === 0) {
    console.warn('hourlyChart container has no size')
    return
  }
  
  const chart = echarts.init(hourlyChart.value)
  
  const hours = heatmapData.hourlyRate.map((h: any) => h.hourLabel)
  const rates = heatmapData.hourlyRate.map((h: any) => h.rate)
  
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        const hourData = heatmapData.hourlyRate.find((h: any) => h.hourLabel === data.name)
        if (!hourData) return ''
        return `
          <div style="padding: 10px;">
            <div style="font-weight: bold; margin-bottom: 8px;">${hourData.hourLabel}</div>
            <div>上座率: <span style="color: #1890ff; font-weight: bold;">${hourData.rate}%</span></div>
            <div>占用: ${hourData.busy} | 空闲: ${hourData.free}</div>
            <div>总记录: ${hourData.total}</div>
          </div>
        `
      }
    },
    grid: { top: 30, right: 30, bottom: 50, left: 50 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { rotate: 45 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: rates.map((rate: number) => ({
        value: rate,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: getHeatColor(rate) },
            { offset: 1, color: getHeatColor(rate) + '60' }
          ])
        }
      })),
      barMaxWidth: 30,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      }
    }]
  })
  
  chartInstances.push(chart)
}

// 高峰时段排名
const renderPeakRankChart = () => {
  if (!peakRankChart.value) {
    console.warn('peakRankChart ref not ready')
    return
  }
  
  if (peakRankChart.value.offsetWidth === 0 || peakRankChart.value.offsetHeight === 0) {
    console.warn('peakRankChart container has no size')
    return
  }
  
  const chart = echarts.init(peakRankChart.value)
  
  // 按上座率排序，取前10
  const sortedHours = [...heatmapData.hourlyRate]
    .filter((h: any) => h.total > 0)
    .sort((a: any, b: any) => b.rate - a.rate)
    .slice(0, 10)
  
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 10, right: 30, bottom: 30, left: 60 },
    xAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: sortedHours.map((h: any) => h.hourLabel).reverse(),
      axisLine: { show: false }
    },
    series: [{
      type: 'bar',
      data: sortedHours.map((h: any) => ({
        value: h.rate,
        itemStyle: {
          color: h.rate >= 60 ? '#52c41a' : h.rate >= 40 ? '#faad14' : '#ff4d4f',
          borderRadius: [0, 4, 4, 0]
        }
      })).reverse(),
      label: {
        show: true,
        position: 'right',
        formatter: '{c}%'
      }
    }]
  })
  
  chartInstances.push(chart)
}

// 区域热力图
const renderAreaHeatmap = () => {
  if (!areaHeatmap.value) {
    console.warn('areaHeatmap ref not ready')
    return
  }
  
  if (areaHeatmap.value.offsetWidth === 0 || areaHeatmap.value.offsetHeight === 0) {
    console.warn('areaHeatmap container has no size')
    return
  }
  
  const chart = echarts.init(areaHeatmap.value)
  
  const areas = heatmapData.areaRate.slice(0, 15).map((a: any) => a.areaName)
  const rates = heatmapData.areaRate.slice(0, 15).map((a: any) => a.rate)
  
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 20, right: 30, bottom: 80, left: 100 },
    xAxis: {
      type: 'category',
      data: areas,
      axisLabel: { rotate: 45, fontSize: 10 },
      axisLine: { lineStyle: { color: '#ddd' } }
    },
    yAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: rates.map((rate: number) => ({
        value: rate,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: getHeatColor(rate) },
            { offset: 1, color: getHeatColor(rate) + '40' }
          ])
        }
      })),
      barMaxWidth: 40,
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.3)' }
      }
    }]
  })
  
  chartInstances.push(chart)
}

// 日期-小时热力图
const renderDayHourHeatmap = () => {
  if (!dayHourHeatmap.value) {
    console.warn('dayHourHeatmap ref not ready')
    return
  }
  
  if (dayHourHeatmap.value.offsetWidth === 0 || dayHourHeatmap.value.offsetHeight === 0) {
    console.warn('dayHourHeatmap container has no size')
    return
  }
  
  const chart = echarts.init(dayHourHeatmap.value)
  
  const hours = Array.from({ length: 24 }, (_, i) => `${String(i).padStart(2, '0')}:00`)
  const dates = heatmapData.dateList.map((d: string) => d.substring(5)) // MM-DD
  
  chart.setOption({
    tooltip: {
      position: 'top',
      formatter: (params: any) => {
        return `${dates[params.data[0]]} ${hours[params.data[1]]}<br/>上座率: ${params.data[2]}%`
      }
    },
    grid: { top: 30, right: 30, bottom: 60, left: 80 },
    xAxis: {
      type: 'category',
      data: dates,
      splitArea: { show: true },
      axisLabel: { rotate: 45 }
    },
    yAxis: {
      type: 'category',
      data: hours,
      splitArea: { show: true }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: {
        color: ['#f0f0f0', '#bae7ff', '#69c0ff', '#1890ff', '#0050b3']
      }
    },
    series: [{
      type: 'heatmap',
      data: heatmapData.heatmapData,
      label: { show: false },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      }
    }]
  })
  
  chartInstances.push(chart)
}

// 渲染日历视图
const renderCalendar = () => {
  if (!calendarBody.value) {
    console.warn('calendarBody ref not ready')
    return
  }
  
  calendarBody.value.innerHTML = ''
  
  const dataMap = new Map<string, number>()
  heatmapData.calendarData.forEach((d: any) => {
    dataMap.set(d.date, d.rate)
  })
  
  // 如果没有数据，显示提示
  if (heatmapData.calendarData.length === 0) {
    calendarBody.value.innerHTML = '<div style="color: #999; padding: 20px; text-align: center;">暂无数据</div>'
    return
  }
  
  // 根据数据范围生成日历
  const startDate = dateRange.value ? dateRange.value[0].toDate() : new Date()
  const endDate = dateRange.value ? dateRange.value[1].toDate() : new Date()
  
  const grid = document.createElement('div')
  grid.className = 'calendar-grid-inner'
  grid.style.display = 'grid'
  grid.style.gridTemplateColumns = 'repeat(7, 11px)' // 7天一周
  grid.style.gap = '3px'
  
  const current = new Date(startDate)
  
  // 填充第一天之前的空白
  const firstDayOfWeek = current.getDay() === 0 ? 6 : current.getDay() - 1
  for (let i = 0; i < firstDayOfWeek; i++) {
    const emptyDiv = document.createElement('div')
    emptyDiv.className = 'calendar-cell empty'
    grid.appendChild(emptyDiv)
  }
  
  while (current <= endDate) {
    const dateStr = current.toISOString().split('T')[0]
    const rate = dataMap.get(dateStr) || 0
    const level = getLevel(rate)
    
    const cell = document.createElement('div')
    cell.className = `calendar-cell level-${level}`
    cell.title = `${dateStr}: ${rate}%`
    
    grid.appendChild(cell)
    
    current.setDate(current.getDate() + 1)
  }
  
  calendarBody.value.appendChild(grid)
}

// 辅助函数
const getHeatColor = (rate: number) => {
  if (rate >= 80) return '#52c41a'
  if (rate >= 60) return '#1890ff'
  if (rate >= 40) return '#faad14'
  if (rate >= 20) return '#ff7a45'
  return '#ff4d4f'
}

const getLevel = (rate: number) => {
  if (rate === 0) return 0
  if (rate < 20) return 1
  if (rate < 40) return 2
  if (rate < 60) return 3
  if (rate < 80) return 4
  return 5
}

// 窗口大小变化
const handleResize = () => {
  chartInstances.forEach(chart => chart?.resize())
}

onMounted(() => {
  loadShops()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstances.forEach(chart => chart?.dispose())
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
.heatmap-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.calendar-heatmap {
  overflow-x: auto;
  
  .calendar-container {
    display: flex;
    flex-direction: column;
    padding: 20px;
  }
  
  .month-labels {
    display: flex;
    justify-content: space-between;
    padding-left: 40px;
    margin-bottom: 10px;
    font-size: 12px;
    color: #666;
  }
  
  .calendar-grid {
    display: flex;
    align-items: flex-start;
  }
  
  .weekday-labels {
    display: flex;
    flex-direction: column;
    gap: 3px;
    margin-right: 5px;
    font-size: 10px;
    color: #999;
    
    span {
      height: 11px;
      line-height: 11px;
    }
  }
  
  .calendar-body {
    flex: 1;
    overflow-x: auto;
    min-height: 100px;
  }
  
  .calendar-grid-inner {
    display: grid;
    grid-template-columns: repeat(7, 11px);
    gap: 3px;
  }
  
  .calendar-cell {
    width: 11px;
    height: 11px;
    border-radius: 2px;
    
    &.empty {
      background: transparent;
    }
    
    &.level-0 { background: #f0f0f0; }
    &.level-1 { background: #bae7ff; }
    &.level-2 { background: #69c0ff; }
    &.level-3 { background: #1890ff; }
    &.level-4 { background: #096dd9; }
    &.level-5 { background: #003a8c; }
    
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.3);
    }
  }
}

.calendar-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  font-size: 12px;
  color: #666;
  
  .legend-blocks {
    display: flex;
    gap: 3px;
  }
  
  .legend-block {
    width: 11px;
    height: 11px;
    border-radius: 2px;
    
    &.level-0 { background: #f0f0f0; }
    &.level-1 { background: #bae7ff; }
    &.level-2 { background: #69c0ff; }
    &.level-3 { background: #1890ff; }
    &.level-4 { background: #096dd9; }
    &.level-5 { background: #003a8c; }
  }
}

.area-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 8px 0;
  
  .area-rank {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 12px;
    background: #f0f0f0;
    color: #666;
    
    &.rank-1 {
      background: linear-gradient(135deg, #ffd700, #ffec8b);
      color: #fff;
    }
    
    &.rank-2 {
      background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
      color: #fff;
    }
    
    &.rank-3 {
      background: linear-gradient(135deg, #cd7f32, #daa520);
      color: #fff;
    }
  }
  
  .area-info {
    flex: 1;
    
    .area-name {
      font-size: 13px;
      margin-bottom: 4px;
      color: #333;
    }
  }
  
  .area-rate {
    font-size: 16px;
    font-weight: bold;
    min-width: 50px;
    text-align: right;
  }
}
</style>
