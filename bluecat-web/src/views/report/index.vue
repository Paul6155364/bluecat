<template>
  <div class="business-report">
    <!-- 筛选条件 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="网咖">
          <a-select
            v-model:value="selectedConfigId"
            placeholder="选择网咖"
            style="width: 180px"
            @change="handleConfigChange"
          >
            <a-select-option v-for="config in configList" :key="config.id" :value="config.id">
              {{ config.configName || '未分类' }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="门店">
          <a-select
            v-model:value="selectedShopId"
            placeholder="选择门店"
            style="width: 200px"
            show-search
            :filter-option="filterOption"
            @change="handleShopChange"
          >
            <a-select-option v-for="shop in shopList" :key="shop.id" :value="shop.id">
              {{ shop.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            :placeholder="['开始日期', '结束日期']"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="generateReport">
            <search-outlined /> 生成报告
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 无数据提示 -->
    <a-empty v-if="!reportData" description="请选择门店和时间范围，点击生成报告查看经营分析数据" />

    <!-- 报告内容 -->
    <template v-else>
      <!-- 报告标题 -->
      <div class="report-header">
        <h2>{{ reportData.shopName }} · 上座率分析报告</h2>
        <div class="report-meta">
          数据时间范围：{{ reportData.startDate }} 至 {{ reportData.endDate }}（共 {{ reportData.days }} 天）| 
          生成时间：{{ reportData.generateTime }}
        </div>
      </div>

      <!-- KPI 核心指标 -->
      <div class="section">
        <div class="section-title">核心指标</div>
        <a-row :gutter="16">
          <a-col :span="4" v-for="(item, index) in kpiCards" :key="index">
            <div class="kpi-card" :class="item.color">
              <div class="kpi-label">{{ item.label }}</div>
              <div class="kpi-value">{{ item.value }}</div>
              <div class="kpi-desc">{{ item.desc }}</div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 每日趋势分析 -->
      <div class="section">
        <div class="section-title">每日上座率趋势</div>
        <a-card>
          <div ref="dailyChartRef" style="height: 350px"></div>
        </a-card>
      </div>

      <!-- 时段 & 星期分布 -->
      <div class="section">
        <div class="section-title">时段 & 星期分布</div>
        <a-row :gutter="16">
          <a-col :span="16">
            <a-card title="24小时上座率分布">
              <div ref="hourlyChartRef" style="height: 300px"></div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="星期分布">
              <div ref="weeklyChartRef" style="height: 300px"></div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 区域经营分析 -->
      <div class="section">
        <div class="section-title">区域上座率分析</div>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-card title="各区域上座率排行">
              <div ref="areaRankChartRef" style="height: 360px"></div>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card title="区域详情">
              <a-table
                :columns="areaColumns"
                :data-source="areaList"
                :pagination="false"
                size="small"
                row-key="areaName"
                :scroll="{ y: 320 }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'avgOccupancyRate'">
                    <a-progress
                      :percent="record.avgOccupancyRate"
                      :stroke-color="getProgressColor(record.avgOccupancyRate)"
                      size="small"
                    />
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 座位热力图 -->
      <div class="section" v-if="seatHeatmapData.length > 0">
        <div class="section-title">座位使用热力图</div>
        <a-card title="按区域×座位的使用次数热力图">
          <div ref="seatHeatmapRef" style="height: 500px"></div>
        </a-card>
      </div>

      <!-- 数据洞察 -->
      <div class="section">
        <div class="section-title">数据洞察 & 运营建议</div>
        <a-row :gutter="16">
          <a-col :span="12" v-for="(insight, index) in insights" :key="index">
            <div class="insight-card" :class="insight.type">
              <div class="insight-title">{{ insight.title }}</div>
              <div class="insight-content">{{ insight.content }}</div>
            </div>
          </a-col>
        </a-row>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import dayjs, { Dayjs } from 'dayjs'
import { reportApi } from '@/api/report'

// 筛选条件
const configList = ref<any[]>([])
const shopList = ref<any[]>([])
const selectedConfigId = ref<number>()
const selectedShopId = ref<number>()
const dateRange = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(7, 'day'),
  dayjs()
])
const loading = ref(false)

// 报告数据
const reportData = ref<any>(null)

// 图表引用
const dailyChartRef = ref<HTMLElement | null>(null)
const hourlyChartRef = ref<HTMLElement | null>(null)
const weeklyChartRef = ref<HTMLElement | null>(null)
const areaRankChartRef = ref<HTMLElement | null>(null)
const seatHeatmapRef = ref<HTMLElement | null>(null)

// 图表实例
let dailyChart: echarts.ECharts | null = null
let hourlyChart: echarts.ECharts | null = null
let weeklyChart: echarts.ECharts | null = null
let areaRankChart: echarts.ECharts | null = null
let seatHeatmapChart: echarts.ECharts | null = null

// KPI卡片数据 - 只显示真实数据
const kpiCards = computed(() => {
  if (!reportData.value?.kpi) return []
  const kpi = reportData.value.kpi
  return [
    {
      label: '总座位数',
      value: kpi.totalSeats || 0,
      desc: '门店座位总数',
      color: 'blue'
    },
    {
      label: '平均上座率',
      value: `${(kpi.avgOccupancyRate || 0).toFixed(1)}%`,
      desc: '统计周期内平均',
      color: 'green'
    },
    {
      label: '峰值上座率',
      value: `${(kpi.maxOccupancyRate || 0).toFixed(1)}%`,
      desc: '统计周期内最高',
      color: 'yellow'
    },
    {
      label: '平均占用座位',
      value: (kpi.avgBusyMachines || 0).toFixed(1),
      desc: '平均同时使用数',
      color: 'purple'
    },
    {
      label: '峰值占用座位',
      value: kpi.maxBusyMachines || 0,
      desc: '最高同时使用数',
      color: 'cyan'
    },
    {
      label: '数据记录数',
      value: kpi.recordCount || 0,
      desc: '快照记录总数',
      color: 'orange'
    }
  ]
})

// 区域列表
const areaList = computed(() => {
  return reportData.value?.areaAnalysis?.areas || []
})

// 座位热力图数据
const seatHeatmapData = computed(() => {
  return reportData.value?.seatHeatmap?.data || []
})

// 区域表格列 - 移除消费相关
const areaColumns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', width: 60 },
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '机器数', dataIndex: 'totalMachines', key: 'totalMachines', width: 80 },
  { title: '平均上座率', dataIndex: 'avgOccupancyRate', key: 'avgOccupancyRate', width: 180 },
  { title: '峰值上座率', dataIndex: 'maxOccupancyRate', key: 'maxOccupancyRate', width: 100 },
  { title: '记录数', dataIndex: 'recordCount', key: 'recordCount', width: 80 }
]

// 洞察列表
const insights = computed(() => {
  return reportData.value?.insights || []
})

// 加载网咖配置
const fetchConfigList = async () => {
  try {
    const res = await reportApi.listConfigs()
    configList.value = res.data || []
    if (configList.value.length > 0) {
      selectedConfigId.value = configList.value[0].id
      fetchShopList()
    }
  } catch (error) {
    console.error(error)
  }
}

// 加载门店列表
const fetchShopList = async () => {
  if (!selectedConfigId.value) return
  try {
    const res = await reportApi.listShops(selectedConfigId.value)
    shopList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 处理网咖变更
const handleConfigChange = () => {
  selectedShopId.value = undefined
  reportData.value = null
  fetchShopList()
}

// 处理门店变更
const handleShopChange = () => {
  reportData.value = null
}

// 搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 生成报告
const generateReport = async () => {
  if (!selectedShopId.value) {
    message.warning('请选择门店')
    return
  }
  if (!dateRange.value || dateRange.value.length !== 2) {
    message.warning('请选择时间范围')
    return
  }

  loading.value = true
  try {
    const res = await reportApi.generateReport(
      selectedShopId.value,
      dateRange.value[0].format('YYYY-MM-DD'),
      dateRange.value[1].format('YYYY-MM-DD')
    )
    reportData.value = res.data
    
    await nextTick()
    setTimeout(() => {
      renderCharts()
    }, 100)
  } catch (error: any) {
    message.error(error.message || '生成报告失败')
  } finally {
    loading.value = false
  }
}

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#52c41a'
  if (rate >= 60) return '#1890ff'
  if (rate >= 40) return '#faad14'
  return '#ff4d4f'
}

// 渲染所有图表
const renderCharts = () => {
  renderDailyChart()
  renderHourlyChart()
  renderWeeklyChart()
  renderAreaRankChart()
  renderSeatHeatmap()
}

// 渲染每日趋势图 - 改为上座率
const renderDailyChart = () => {
  if (!dailyChartRef.value || !reportData.value?.dailyTrend) return
  
  if (dailyChart) dailyChart.dispose()
  dailyChart = echarts.init(dailyChartRef.value)

  const trend = reportData.value.dailyTrend
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['平均上座率', '峰值上座率', '平均占用座位'],
      right: 20,
      top: 5
    },
    grid: {
      left: 60,
      right: 80,
      top: 50,
      bottom: 60
    },
    xAxis: {
      type: 'category',
      data: trend.dates,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: [
      { type: 'value', name: '上座率(%)', max: 100 },
      { type: 'value', name: '座位数' }
    ],
    series: [
      {
        name: '平均上座率',
        type: 'line',
        data: trend.avgOccupancyRates,
        yAxisIndex: 0,
        lineStyle: { color: '#52c41a', width: 2 },
        itemStyle: { color: '#52c41a' },
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82,196,26,0.3)' },
            { offset: 1, color: 'rgba(82,196,26,0)' }
          ])
        }
      },
      {
        name: '峰值上座率',
        type: 'line',
        data: trend.maxOccupancyRates,
        yAxisIndex: 0,
        lineStyle: { color: '#faad14', width: 2, type: 'dashed' },
        itemStyle: { color: '#faad14' },
        smooth: true
      },
      {
        name: '平均占用座位',
        type: 'bar',
        data: trend.avgBusyMachines,
        yAxisIndex: 1,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: '#0d5bbb' }
          ])
        },
        barMaxWidth: 18
      }
    ]
  }
  dailyChart.setOption(option)
}

// 渲染时段分布图 - 改为上座率
const renderHourlyChart = () => {
  if (!hourlyChartRef.value || !reportData.value?.hourlyDistribution) return
  
  if (hourlyChart) hourlyChart.dispose()
  hourlyChart = echarts.init(hourlyChartRef.value)

  const dist = reportData.value.hourlyDistribution
  const maxRate = Math.max(...dist.avgOccupancyRates) || 100
  
  const option: echarts.EChartsOption = {
    tooltip: { 
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>平均上座率: ${data.value}%<br/>记录数: ${dist.recordCounts[data.dataIndex]}`
      }
    },
    grid: { left: 60, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: dist.hours.map((h: number) => `${h}时`)
    },
    yAxis: { type: 'value', name: '上座率(%)', max: 100 },
    visualMap: {
      show: false,
      min: 0,
      max: maxRate,
      inRange: {
        color: ['#ff4d4f', '#faad14', '#52c41a', '#1890ff']
      }
    },
    series: [{
      type: 'bar',
      data: dist.avgOccupancyRates,
      barMaxWidth: 20
    }]
  }
  hourlyChart.setOption(option)
}

// 渲染星期分布图 - 改为上座率
const renderWeeklyChart = () => {
  if (!weeklyChartRef.value || !reportData.value?.weeklyDistribution) return
  
  if (weeklyChart) weeklyChart.dispose()
  weeklyChart = echarts.init(weeklyChartRef.value)

  const dist = reportData.value.weeklyDistribution
  const maxValue = Math.max(...dist.avgOccupancyRates) * 1.1 || 100
  
  const option: echarts.EChartsOption = {
    tooltip: {},
    radar: {
      indicator: dist.days.map((d: string) => ({
        name: d,
        max: maxValue
      })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#666', fontSize: 12 }
    },
    series: [{
      type: 'radar',
      data: [{
        value: dist.avgOccupancyRates,
        name: '平均上座率',
        lineStyle: { color: '#1890ff', width: 2 },
        areaStyle: { color: 'rgba(24,144,255,0.2)' },
        itemStyle: { color: '#1890ff' }
      }]
    }]
  }
  weeklyChart.setOption(option)
}

// 渲染区域排名图 - 改为上座率
const renderAreaRankChart = () => {
  if (!areaRankChartRef.value || !reportData.value?.areaAnalysis) return
  
  if (areaRankChart) areaRankChart.dispose()
  areaRankChart = echarts.init(areaRankChartRef.value)

  const areas = reportData.value.areaAnalysis.areas || []
  const sortedAreas = [...areas].sort((a: any, b: any) => a.avgOccupancyRate - b.avgOccupancyRate)

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: 180, right: 80, top: 20, bottom: 20 },
    xAxis: { type: 'value', name: '上座率(%)', max: 100 },
    yAxis: {
      type: 'category',
      data: sortedAreas.map((a: any) => a.areaName)
    },
    series: [{
      type: 'bar',
      data: sortedAreas.map((a: any) => a.avgOccupancyRate),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#52c41a' }
        ])
      },
      label: {
        show: true,
        position: 'right',
        formatter: (params: any) => `${params.value.toFixed(1)}%`
      }
    }]
  }
  areaRankChart.setOption(option)
}

// 渲染座位热力图
const renderSeatHeatmap = () => {
  if (!seatHeatmapRef.value || !reportData.value?.seatHeatmap) return
  if (seatHeatmapData.value.length === 0) return
  
  if (seatHeatmapChart) seatHeatmapChart.dispose()
  seatHeatmapChart = echarts.init(seatHeatmapRef.value)

  const heatmap = reportData.value.seatHeatmap
  const maxValue = Math.max(...heatmap.data.map((d: any[]) => d[2])) || 1

  const option: echarts.EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        return `区域: ${heatmap.areas[params.value[1]]}<br/>
                座位: ${heatmap.seats[params.value[0]]}<br/>
                占用次数: ${params.value[2]}`
      }
    },
    grid: { left: 180, right: 60, top: 20, bottom: 100 },
    xAxis: {
      type: 'category',
      data: heatmap.seats,
      splitArea: { show: true },
      axisLabel: { rotate: 60, fontSize: 9 }
    },
    yAxis: {
      type: 'category',
      data: heatmap.areas,
      splitArea: { show: true }
    },
    visualMap: {
      min: 0,
      max: maxValue,
      calculable: true,
      orient: 'horizontal',
      right: 20,
      bottom: 10,
      inRange: {
        color: ['#f0f0f0', '#bae7ff', '#69c0ff', '#1890ff', '#003a8c']
      }
    },
    series: [{
      type: 'heatmap',
      data: heatmap.data,
      label: { show: false },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.5)'
        }
      }
    }]
  }
  seatHeatmapChart.setOption(option)
}

// 窗口大小变化
const handleResize = () => {
  dailyChart?.resize()
  hourlyChart?.resize()
  weeklyChart?.resize()
  areaRankChart?.resize()
  seatHeatmapChart?.resize()
}

onMounted(() => {
  fetchConfigList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  dailyChart?.dispose()
  hourlyChart?.dispose()
  weeklyChart?.dispose()
  areaRankChart?.dispose()
  seatHeatmapChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
.business-report {
  background: #f0f2f5;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.report-header {
  background: linear-gradient(135deg, #0d1117 0%, #1a2332 50%, #0d2137 100%);
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 20px;
  color: #fff;

  h2 {
    margin: 0 0 8px;
    font-size: 20px;
    background: linear-gradient(90deg, #58a6ff, #bc8cff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .report-meta {
    font-size: 12px;
    color: #8b949e;
  }
}

.section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #1890ff;
}

.kpi-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  &.blue {
    border-top: 3px solid #1890ff;
    .kpi-value { color: #1890ff; }
  }
  &.green {
    border-top: 3px solid #52c41a;
    .kpi-value { color: #52c41a; }
  }
  &.yellow {
    border-top: 3px solid #faad14;
    .kpi-value { color: #faad14; }
  }
  &.purple {
    border-top: 3px solid #722ed1;
    .kpi-value { color: #722ed1; }
  }
  &.cyan {
    border-top: 3px solid #13c2c2;
    .kpi-value { color: #13c2c2; }
  }
  &.orange {
    border-top: 3px solid #fa8c16;
    .kpi-value { color: #fa8c16; }
  }

  .kpi-label {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 8px;
  }

  .kpi-value {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .kpi-desc {
    font-size: 11px;
    color: #bfbfbf;
  }
}

.insight-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  border-left: 4px solid #1890ff;

  &.good {
    border-left-color: #52c41a;
    background: linear-gradient(90deg, rgba(82, 196, 26, 0.05), transparent);
  }
  &.warn {
    border-left-color: #faad14;
    background: linear-gradient(90deg, rgba(250, 173, 20, 0.05), transparent);
  }
  &.info {
    border-left-color: #1890ff;
    background: linear-gradient(90deg, rgba(24, 144, 255, 0.05), transparent);
  }

  .insight-title {
    font-weight: 600;
    margin-bottom: 6px;
    color: #262626;
  }

  .insight-content {
    font-size: 13px;
    color: #595959;
    line-height: 1.6;
  }
}
</style>
