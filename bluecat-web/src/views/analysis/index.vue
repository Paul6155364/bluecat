<template>
  <div class="analysis">
    <!-- 筛选条件 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="网咖配置">
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
        <a-form-item>
          <a-button type="primary" @click="fetchAnalysisData">
            <reload-outlined /> 刷新数据
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 无数据提示 -->
    <a-empty v-if="!selectedShopId" description="请选择门店查看经营分析数据" />

    <!-- 分析内容 -->
    <template v-else>
      <!-- 门店概览 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6">
          <a-card>
            <a-statistic title="机器总数" :value="shopOverview.totalMachines">
              <template #prefix>
                <desktop-outlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="当前空闲"
              :value="shopOverview.freeMachines"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <check-circle-outlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="当前占用"
              :value="shopOverview.busyMachines"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <close-circle-outlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="上座率"
              :value="shopOverview.occupancyRate"
              suffix="%"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <percentage-outlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 区域分析 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="区域上座率排名" :loading="loading">
            <div ref="areaChartRef" style="height: 400px"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="区域详情" :loading="loading">
            <a-table
              :columns="areaColumns"
              :data-source="areaList"
              :pagination="false"
              size="small"
              row-key="areaId"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'occupancyRate'">
                  <a-progress
                    :percent="record.occupancyRate"
                    :stroke-color="getProgressColor(record.occupancyRate)"
                    size="small"
                  />
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.busyMachines > 0 ? 'green' : 'default'">
                    {{ record.busyMachines > 0 ? '有客' : '空闲' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <!-- 区域机器分布 -->
      <a-card title="区域机器分布" style="margin-top: 16px" :loading="loading">
        <div ref="priceChartRef" style="height: 300px"></div>
      </a-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import {
  ReloadOutlined,
  DesktopOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PercentageOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { configApi } from '@/api/config'
import { shopApi } from '@/api/shop'
import { statusApi } from '@/api/status'

// 筛选条件
const configList = ref<any[]>([])
const shopList = ref<any[]>([])
const selectedConfigId = ref<number>()
const selectedShopId = ref<number>()
const loading = ref(false)

// 门店概览
const shopOverview = reactive({
  totalMachines: 0,
  freeMachines: 0,
  busyMachines: 0,
  occupancyRate: 0
})

// 区域数据
const areaList = ref<any[]>([])
const areaColumns = [
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '机器数', dataIndex: 'totalMachines', key: 'totalMachines', width: 80 },
  { title: '空闲', dataIndex: 'freeMachines', key: 'freeMachines', width: 60 },
  { title: '占用', dataIndex: 'busyMachines', key: 'busyMachines', width: 60 },
  { title: '上座率', dataIndex: 'occupancyRate', key: 'occupancyRate', width: 150 },
  { title: '状态', key: 'status', width: 80 }
]

// 图表
const areaChartRef = ref<HTMLElement | null>(null)
const priceChartRef = ref<HTMLElement | null>(null)
let areaChart: echarts.ECharts | null = null
let priceChart: echarts.ECharts | null = null

// 加载网咖配置列表
const fetchConfigList = async () => {
  try {
    const res = await configApi.list()
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
    const res = await shopApi.list(selectedConfigId.value)
    shopList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 处理网咖变更
const handleConfigChange = () => {
  selectedShopId.value = undefined
  fetchShopList()
}

// 处理门店变更
const handleShopChange = () => {
  if (selectedShopId.value) {
    fetchAnalysisData()
  }
}

// 搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 获取分析数据
const fetchAnalysisData = async () => {
  if (!selectedShopId.value) return

  loading.value = true
  try {
    // 获取门店实时状态
    const res = await statusApi.getRealtimeByShopId(selectedShopId.value)
    const snapshot = res.data?.snapshot
    const areas = res.data?.areas || []

    if (snapshot) {
      shopOverview.totalMachines = snapshot.totalMachines || 0
      shopOverview.freeMachines = snapshot.freeMachines || 0
      shopOverview.busyMachines = snapshot.busyMachines || 0
      shopOverview.occupancyRate = snapshot.occupancyRate || 0
    }

    // 处理区域数据
    areaList.value = areas.map((area: any) => ({
      ...area,
      occupancyRate: area.totalMachines > 0
        ? Math.round((area.busyMachines / area.totalMachines) * 100)
        : 0
    })).sort((a: any, b: any) => b.occupancyRate - a.occupancyRate)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }

  // 在 loading 结束后渲染图表
  await nextTick()
  setTimeout(() => {
    renderAreaChart()
    renderPriceChart()
  }, 100)
}

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#ff4d4f'
  if (rate >= 60) return '#faad14'
  if (rate >= 40) return '#52c41a'
  return '#1890ff'
}

// 渲染区域图表
const renderAreaChart = () => {
  if (!areaChartRef.value) return

  if (areaChart) {
    areaChart.dispose()
  }

  areaChart = echarts.init(areaChartRef.value)

  const sortedAreas = [...areaList.value].sort((a, b) => b.occupancyRate - a.occupancyRate)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>上座率: ${data.value}%`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    yAxis: {
      type: 'category',
      data: sortedAreas.map(a => a.areaName).reverse(),
      axisLabel: {
        width: 100,
        overflow: 'truncate'
      }
    },
    series: [
      {
        type: 'bar',
        data: sortedAreas.map(a => a.occupancyRate).reverse(),
        itemStyle: {
          color: (params: any) => {
            const rate = params.value
            if (rate >= 80) return '#ff4d4f'
            if (rate >= 60) return '#faad14'
            if (rate >= 40) return '#52c41a'
            return '#1890ff'
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%'
        }
      }
    ]
  }

  areaChart.setOption(option)
}

// 渲染价格图表 - 改为区域机器分布
const renderPriceChart = () => {
  if (!priceChartRef.value) return

  if (priceChart) {
    priceChart.dispose()
  }

  priceChart = echarts.init(priceChartRef.value)

  // 按区域统计机器数量和占用数
  const sortedAreas = [...areaList.value]
    .sort((a, b) => b.totalMachines - a.totalMachines)
    .slice(0, 15) // 只显示机器数最多的前15个区域

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const totalData = params[0]
        const busyData = params[1]
        const rate = totalData.value > 0 
          ? ((busyData.value / totalData.value) * 100).toFixed(1) 
          : 0
        return `${totalData.name}<br/>
          总机器: ${totalData.value}台<br/>
          占用: ${busyData.value}台<br/>
          上座率: ${rate}%`
      }
    },
    legend: {
      data: ['总机器', '占用']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedAreas.map(a => a.areaName),
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '机器数'
    },
    series: [
      {
        name: '总机器',
        type: 'bar',
        data: sortedAreas.map(a => a.totalMachines),
        itemStyle: { color: '#1890ff', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '占用',
        type: 'bar',
        data: sortedAreas.map(a => a.busyMachines),
        itemStyle: { color: '#ff4d4f', borderRadius: [4, 4, 0, 0] }
      }
    ]
  }

  priceChart.setOption(option)
}

// 窗口大小变化
const handleResize = () => {
  areaChart?.resize()
  priceChart?.resize()
}

onMounted(() => {
  fetchConfigList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  areaChart?.dispose()
  priceChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
.analysis {
  background: #f0f2f5;
  padding: 24px;
  min-height: calc(100vh - 64px);
}
</style>
